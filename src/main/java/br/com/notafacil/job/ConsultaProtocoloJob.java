package br.com.notafacil.job;

import br.com.notafacil.entity.RpsEntity;
import br.com.notafacil.provider.NfseProvider;
import br.com.notafacil.provider.ProviderResolver;
import br.com.notafacil.provider.model.ConsultaSituacaoRequest;
import br.com.notafacil.provider.model.ConsultaSituacaoResponse;
import br.com.notafacil.repository.RpsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ConsultaProtocoloJob {

    private static final Logger log = LoggerFactory.getLogger(ConsultaProtocoloJob.class);

    private final RpsRepository rpsRepository;
    private final JobStatusHolder jobStatus;
    private final ProviderResolver providerResolver;

    public ConsultaProtocoloJob(RpsRepository rpsRepository,
                                 JobStatusHolder jobStatus,
                                 ProviderResolver providerResolver) {
        this.rpsRepository = rpsRepository;
        this.jobStatus = jobStatus;
        this.providerResolver = providerResolver;
    }

    @Scheduled(fixedDelay = 180_000, initialDelay = 60_000)
    @Transactional
    public void consultarProtocolosPendentes() {
        List<String> pendentes = rpsRepository.findPendingProtocols();
        jobStatus.setUltimaExecucao(java.time.LocalDateTime.now());
        jobStatus.setProtocolosPendentes(pendentes.size());
        if (pendentes.isEmpty()) return;

        log.info("[Job] {} protocolo(s) pendente(s)", pendentes.size());

        for (String protocolo : pendentes) {
            try {
                consultarSituacao(protocolo);
            } catch (Exception e) {
                log.warn("[Job] Falha protocolo {}: {}", protocolo, e.getMessage());
            }
        }

        // Atualizar contagem após processar
        List<String> restantes = rpsRepository.findPendingProtocols();
        jobStatus.setProtocolosPendentes(restantes.size());
    }

    private void consultarSituacao(String protocolo) {
        // Encontra um RPS de exemplo para obter empresa e providerId
        List<RpsEntity> rpsDoProtocolo = rpsRepository.findByProtocolo(protocolo);
        if (rpsDoProtocolo.isEmpty()) return;

        RpsEntity sample = rpsDoProtocolo.get(0);
        var empresa = sample.getEmpresa();

        // Resolver provider: usa providerId salvo no RPS, fallback por município
        NfseProvider provider;
        try {
            if (sample.getProviderId() != null && !sample.getProviderId().isBlank()) {
                provider = providerResolver.resolveById(sample.getProviderId());
            } else {
                // Fallback para RPS antigos sem providerId (assume resolução por município)
                provider = providerResolver.resolve(empresa.getCodigoMunicipio());
            }
        } catch (Exception e) {
            log.warn("[Job] Não foi possível resolver provider para protocolo {}: {}", protocolo, e.getMessage());
            return;
        }

        String certAlias = "CNPJ" + empresa.getCnpj();
        ConsultaSituacaoRequest request = new ConsultaSituacaoRequest(empresa, protocolo, certAlias);
        ConsultaSituacaoResponse response = provider.consultarSituacaoLote(request);

        log.info("[Job] Protocolo {} via {} → situacao={}", protocolo, provider.getProviderId(), response.situacao());

        switch (response.situacao()) {
            case 1, 2 -> {} // ainda pendente
            case 3 -> {
                String erro = response.mensagemErro() != null ? response.mensagemErro() : "Erro no processamento";
                log.warn("[Job] Protocolo {} ERRO: {}", protocolo, erro);
                rpsRepository.updateStatusByProtocolo(protocolo, RpsEntity.Status.FALHA, erro);
            }
            case 4 -> {
                log.info("[Job] Protocolo {} SUCESSO", protocolo);
                rpsRepository.updateStatusByProtocolo(protocolo, RpsEntity.Status.PROCESSADO, null);
            }
            default -> {
                if (response.mensagemErro() != null) {
                    log.warn("[Job] Protocolo {} rejeitado: {}", protocolo, response.mensagemErro());
                    rpsRepository.updateStatusByProtocolo(protocolo, RpsEntity.Status.FALHA, response.mensagemErro());
                } else {
                    log.warn("[Job] Protocolo {} situação desconhecida: {}", protocolo, response.situacao());
                }
            }
        }
    }
}
