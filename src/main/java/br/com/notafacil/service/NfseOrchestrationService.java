package br.com.notafacil.service;

import br.com.notafacil.config.NfseProviderProperties;
import br.com.notafacil.dto.CabecalhoDto;
import br.com.notafacil.dto.EmitirRpsResponse;
import br.com.notafacil.dto.RpsMinRequestDto;
import br.com.notafacil.entity.EmpresaEntity;
import br.com.notafacil.entity.RpsEntity;
import br.com.notafacil.provider.NfseProvider;
import br.com.notafacil.provider.ProviderResolver;
import br.com.notafacil.provider.model.EnviarLoteRequest;
import br.com.notafacil.provider.model.ProviderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço de orquestração unificado para emissão de NFS-e.
 * Substitui NfseService1 com suporte multi-provedor.
 *
 * Fluxo:
 * 1. Validate — empresa existe, lista não vazia
 * 2. Persist (cache local) — RPS salvo no banco ANTES de qualquer chamada externa
 * 3. Resolve provider — via ProviderResolver (código município)
 * 4. Determine regime — ISS ou IBS/CBS (via TaxRegimeResolver)
 * 5. Dispatch — batches de N RPS, chama provider.enviarLote()
 * 6. Track — atualiza status + protocolo + providerId no RpsEntity
 */
@Service
public class NfseOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(NfseOrchestrationService.class);

    private final EmpresaService empresaService;
    private final RpsEnrichmentService enrichmentService;
    private final ProviderResolver providerResolver;
    private final TaxRegimeResolver taxRegimeResolver;
    private final NfseProviderProperties properties;

    public NfseOrchestrationService(EmpresaService empresaService,
                                     RpsEnrichmentService enrichmentService,
                                     ProviderResolver providerResolver,
                                     TaxRegimeResolver taxRegimeResolver,
                                     NfseProviderProperties properties) {
        this.empresaService = empresaService;
        this.enrichmentService = enrichmentService;
        this.providerResolver = providerResolver;
        this.taxRegimeResolver = taxRegimeResolver;
        this.properties = properties;
    }

    /**
     * Fluxo principal de emissão de NFS-e via arquitetura multi-provedor.
     */
    public EmitirRpsResponse emitir(String empresaCnpj, List<RpsMinRequestDto> listaMin) {
        // 1. Validate
        if (listaMin == null || listaMin.isEmpty()) {
            return new EmitirRpsResponse(List.of(), List.of());
        }

        EmpresaEntity empresa = empresaService.getByCnpjOrThrow(empresaCnpj);

        // 2. Persist (cache local) — RPS salvo ANTES de qualquer chamada externa
        List<RpsEntity> entidades = enrichmentService.persistirRps(empresa, listaMin);
        if (entidades.isEmpty()) {
            log.info("[Orchestration] Nenhum RPS novo para emitir (todos duplicados)");
            return new EmitirRpsResponse(List.of(), List.of());
        }

        List<Long> rpsIds = entidades.stream().map(RpsEntity::getId).collect(Collectors.toList());
        log.info("[Orchestration] {} RPS persistidos para empresa {}", rpsIds.size(), empresaCnpj);

        // 3. Resolve provider
        NfseProvider provider = providerResolver.resolve(empresa.getCodigoMunicipio());
        log.info("[Orchestration] Provider resolvido: {} para município {}", provider.getProviderId(), empresa.getCodigoMunicipio());

        // 4. Determine regime
        String regime = taxRegimeResolver.resolve(empresa, provider);
        String versaoSchema = taxRegimeResolver.resolveSchemaVersion(regime);
        log.info("[Orchestration] Regime: {}, Schema: V{}", regime, versaoSchema);

        // 5. Dispatch em batches
        int batchSize = Math.max(1, properties.getLote().getTamanho());
        List<String> protocolos = new ArrayList<>();
        String certAlias = "CNPJ" + empresa.getCnpj();

        for (int i = 0; i < entidades.size(); i += batchSize) {
            int end = Math.min(i + batchSize, entidades.size());
            List<RpsEntity> batch = entidades.subList(i, end);

            // Marcar como ENVIANDO
            enrichmentService.marcarParaEnvio(batch, provider.getProviderId(), regime, versaoSchema);

            try {
                EnviarLoteRequest request = new EnviarLoteRequest(empresa, batch, certAlias, versaoSchema);
                ProviderResponse response = provider.enviarLote(request);

                if (response.success()) {
                    // 6. Track — atualiza status + protocolo
                    enrichmentService.atualizarStatusEnviado(batch, response.protocolo());
                    protocolos.add(response.protocolo());
                    log.info("[Orchestration] Lote enviado com sucesso. Provider={}, protocolo={}", provider.getProviderId(), response.protocolo());
                } else {
                    enrichmentService.marcarErroEnvio(batch, response.errorMessage());
                    log.error("[Orchestration] Falha no envio do lote: {}", response.errorMessage());
                    throw new RuntimeException("Falha ao enviar lote NFSe: " + response.errorMessage());
                }
            } catch (RuntimeException e) {
                // Se já tratado acima, re-throw; senão marca erro
                if (batch.get(0).getStatus() != RpsEntity.Status.FALHA) {
                    enrichmentService.marcarErroEnvio(batch, e.getMessage());
                }
                throw e;
            }
        }

        return new EmitirRpsResponse(rpsIds, protocolos);
    }

    /**
     * Reenviar RPS pendentes/falhos de uma empresa.
     */
    public EmitirRpsResponse reenviarPendentes(String empresaCnpj) {
        EmpresaEntity empresa = empresaService.getByCnpjOrThrow(empresaCnpj);

        List<RpsEntity> pendentes = findPendentesOuFalhos(empresa);
        if (pendentes.isEmpty()) {
            return new EmitirRpsResponse(List.of(), List.of());
        }

        // Reset status
        List<Long> rpsIds = new ArrayList<>();
        for (RpsEntity rps : pendentes) {
            rps.setStatus(RpsEntity.Status.PENDENTE);
            rps.setMensagemErro(null);
            rpsIds.add(rps.getId());
        }

        NfseProvider provider = providerResolver.resolve(empresa.getCodigoMunicipio());
        String regime = taxRegimeResolver.resolve(empresa, provider);
        String versaoSchema = taxRegimeResolver.resolveSchemaVersion(regime);
        String certAlias = "CNPJ" + empresa.getCnpj();

        int batchSize = Math.max(1, properties.getLote().getTamanho());
        List<String> protocolos = new ArrayList<>();

        for (int i = 0; i < pendentes.size(); i += batchSize) {
            int end = Math.min(i + batchSize, pendentes.size());
            List<RpsEntity> batch = pendentes.subList(i, end);

            enrichmentService.marcarParaEnvio(batch, provider.getProviderId(), regime, versaoSchema);

            try {
                EnviarLoteRequest request = new EnviarLoteRequest(empresa, batch, certAlias, versaoSchema);
                ProviderResponse response = provider.enviarLote(request);

                if (response.success()) {
                    enrichmentService.atualizarStatusEnviado(batch, response.protocolo());
                    protocolos.add(response.protocolo());
                } else {
                    enrichmentService.marcarErroEnvio(batch, response.errorMessage());
                    throw new RuntimeException("Falha ao reenviar lote: " + response.errorMessage());
                }
            } catch (RuntimeException e) {
                if (batch.get(0).getStatus() != RpsEntity.Status.FALHA) {
                    enrichmentService.marcarErroEnvio(batch, e.getMessage());
                }
                throw e;
            }
        }

        return new EmitirRpsResponse(rpsIds, protocolos);
    }

    private List<RpsEntity> findPendentesOuFalhos(EmpresaEntity empresa) {
        return enrichmentService.findPendentesOuFalhos(empresa.getId());
    }
}
