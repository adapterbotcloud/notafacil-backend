package br.com.notafacil.job;

import br.com.notafacil.entity.RpsEntity;
import br.com.notafacil.repository.RpsRepository;
import br.com.notafacil.wsdl.ServiceGinfes;
import br.com.notafacil.wsdl.ServiceGinfesImplServiceService;
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
    private ServiceGinfes servicePort;

    public ConsultaProtocoloJob(RpsRepository rpsRepository, JobStatusHolder jobStatus) {
        this.rpsRepository = rpsRepository;
        this.jobStatus = jobStatus;
        try {
            this.servicePort = new ServiceGinfesImplServiceService().getServiceGinfes();
        } catch (Exception e) {
            log.warn("[Job] SOAP port indisponivel: {}", e.getMessage());
            this.servicePort = null;
        }
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
    }

    private void consultarSituacao(String protocolo) {
        if (servicePort == null) {
            log.debug("[Job] SOAP port indisponivel, pulando protocolo {}", protocolo);
            return;
        }

        RpsEntity sample = rpsRepository.findAll().stream()
                .filter(r -> protocolo.equals(r.getProtocolo()))
                .findFirst().orElse(null);
        if (sample == null) return;

        String cnpj = sample.getEmpresa().getCnpj();
        String im = sample.getEmpresa().getInscricaoMunicipal();

        String xmlConsulta =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<ConsultarSituacaoLoteRpsEnvio xmlns=\"http://www.ginfes.com.br/servico_consultar_situacao_lote_rps_envio_v03.xsd\">" +
            "<Prestador><Cnpj>" + cnpj + "</Cnpj>" +
            "<InscricaoMunicipal>" + im + "</InscricaoMunicipal></Prestador>" +
            "<Protocolo>" + protocolo + "</Protocolo>" +
            "</ConsultarSituacaoLoteRpsEnvio>";

        try {
            String resposta = servicePort.consultarSituacaoLoteRpsV3("", xmlConsulta);
            int situacao = extrairSituacao(resposta);
            log.info("[Job] Protocolo {} situacao={}", protocolo, situacao);

            switch (situacao) {
                case 1, 2 -> {} // ainda pendente
                case 3 -> {
                    String erro = extrairTag(resposta, "Mensagem", "Erro no processamento");
                    log.warn("[Job] Protocolo {} ERRO: {}", protocolo, erro);
                    rpsRepository.updateStatusByProtocolo(protocolo, 3, erro);
                }
                case 4 -> {
                    log.info("[Job] Protocolo {} SUCESSO", protocolo);
                    rpsRepository.updateStatusByProtocolo(protocolo, 4, null);
                }
                default -> log.warn("[Job] Protocolo {} situacao desconhecida: {}", protocolo, situacao);
            }
        } catch (Exception e) {
            log.error("[Job] Erro SOAP protocolo {}: {}", protocolo, e.getMessage());
        }
    }

    private int extrairSituacao(String xml) {
        String val = extrairTag(xml, "Situacao", null);
        if (val != null) {
            try { return Integer.parseInt(val.trim()); } catch (Exception ignored) {}
        }
        return -1;
    }

    private String extrairTag(String xml, String tag, String fallback) {
        int s = xml.indexOf("<" + tag + ">");
        int e = xml.indexOf("</" + tag + ">");
        if (s >= 0 && e > s) return xml.substring(s + tag.length() + 2, e).trim();
        return fallback;
    }
}
