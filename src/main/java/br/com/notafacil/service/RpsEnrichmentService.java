package br.com.notafacil.service;

import br.com.notafacil.dto.RpsMinRequestDto;
import br.com.notafacil.entity.EmpresaEntity;
import br.com.notafacil.entity.RpsEntity;
import br.com.notafacil.repository.RpsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Responsável por persistir e enriquecer RPS entities a partir de DTOs mínimos.
 * Extraído de NfseService1 para uso com a arquitetura multi-provedor.
 */
@Service
public class RpsEnrichmentService {

    private static final Logger log = LoggerFactory.getLogger(RpsEnrichmentService.class);

    private final RpsRepository rpsRepository;

    public RpsEnrichmentService(RpsRepository rpsRepository) {
        this.rpsRepository = rpsRepository;
    }

    /**
     * Persiste RPS como PENDENTE com deduplicação por idCobranca.
     * Retorna somente os RPS novos (não duplicados).
     */
    @Transactional
    public List<RpsEntity> persistirRps(EmpresaEntity empresa, List<RpsMinRequestDto> listaMin) {
        // Deduplicação
        List<Long> idsCobranca = listaMin.stream()
                .map(RpsMinRequestDto::idCobranca)
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());

        Set<Long> jaExistem = idsCobranca.isEmpty()
                ? Collections.emptySet()
                : new HashSet<>(rpsRepository.findIdCobrancasByEmpresaAndIds(empresa.getId(), idsCobranca));

        List<RpsMinRequestDto> novos = listaMin.stream()
                .filter(min -> min.idCobranca() == null || !jaExistem.contains(min.idCobranca()))
                .collect(Collectors.toList());

        if (novos.isEmpty()) {
            return Collections.emptyList();
        }

        List<RpsEntity> salvos = new ArrayList<>(novos.size());
        for (RpsMinRequestDto min : novos) {
            RpsEntity rps = new RpsEntity();
            rps.setEmpresa(empresa);
            rps.setRequestId(min.id());
            rps.setNumero(gerarNumeroRps());
            rps.setSerie("A");
            rps.setTipo(1);
            rps.setDataEmissao(LocalDateTime.now().minusMinutes(1));
            rps.setNaturezaOperacao(1);
            rps.setRegimeEspecialTributacao(empresa.getRegimeEspecialTributacao());
            rps.setOptanteSimplesNacional(empresa.getOptanteSimplesNacional());
            rps.setIncentivadorCultural(empresa.getIncentivadorCultural());
            rps.setValorServicos(min.servico().valorServicos());
            rps.setDiscriminacao(min.servico().discriminacao());
            rps.setTomadorCpf(onlyDigits(min.tomador().cpf()));
            rps.setTomadorRazaoSocial(min.tomador().razaoSocial());
            rps.setIdCobranca(min.idCobranca());
            rps.setMesCobranca(min.mesCobranca());
            rps.setAnoCobranca(min.anoCobranca());
            rps.setStatus(RpsEntity.Status.PENDENTE);
            rps.setTentativasEnvio(0);

            salvos.add(rps);
        }

        return rpsRepository.saveAll(salvos);
    }

    /**
     * Marca RPS com provider, regime e versão de schema antes do envio.
     */
    @Transactional
    public void marcarParaEnvio(List<RpsEntity> rpsList, String providerId, String regimeTributario, String versaoSchema) {
        for (RpsEntity rps : rpsList) {
            rps.setProviderId(providerId);
            rps.setRegimeTributario(regimeTributario);
            rps.setVersaoSchema(versaoSchema);
            rps.setStatus(RpsEntity.Status.ENVIANDO);
            rps.setTentativasEnvio(rps.getTentativasEnvio() != null ? rps.getTentativasEnvio() + 1 : 1);
        }
        rpsRepository.saveAll(rpsList);
    }

    /**
     * Atualiza status pós-envio com protocolo.
     */
    @Transactional
    public void atualizarStatusEnviado(List<RpsEntity> rpsList, String protocolo) {
        for (RpsEntity rps : rpsList) {
            rps.setStatus(RpsEntity.Status.ENVIADO);
            rps.setProtocolo(protocolo);
            rps.setMensagemErro(null);
        }
        rpsRepository.saveAll(rpsList);
    }

    /**
     * Marca RPS com erro de envio.
     */
    @Transactional
    public void marcarErroEnvio(List<RpsEntity> rpsList, String motivo) {
        for (RpsEntity rps : rpsList) {
            rps.setStatus(RpsEntity.Status.FALHA);
            rps.setMensagemErro(motivo != null ? motivo : "Erro desconhecido no envio");
        }
        rpsRepository.saveAll(rpsList);
    }

    /**
     * Busca RPS pendentes ou com falha sem protocolo para uma empresa.
     */
    public List<RpsEntity> findPendentesOuFalhos(Long empresaId) {
        return rpsRepository.findPendentesOuFalhosByEmpresa(empresaId);
    }

    private static String gerarNumeroRps() {
        return String.valueOf(System.currentTimeMillis());
    }

    private static String onlyDigits(String s) {
        return s == null ? null : s.replaceAll("\\D+", "");
    }
}
