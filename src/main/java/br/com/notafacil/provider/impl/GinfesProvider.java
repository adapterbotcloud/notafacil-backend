package br.com.notafacil.provider.impl;

import br.com.notafacil.config.NfseProviderProperties;
import br.com.notafacil.entity.EmpresaEntity;
import br.com.notafacil.provider.NfseProvider;
import br.com.notafacil.provider.model.*;
import br.com.notafacil.provider.xml.GinfesXmlBuilder;
import br.com.notafacil.service.AzureVaultXmlSigningService;
import br.com.notafacil.wsdl.ServiceGinfes;
import br.com.notafacil.wsdl.ServiceGinfesImplServiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GinfesProvider implements NfseProvider {

    private static final Logger log = LoggerFactory.getLogger(GinfesProvider.class);
    private static final Pattern PROTOCOLO_TAG = Pattern.compile("<(?:[\\w]+:)?Protocolo>([^<]+)</(?:[\\w]+:)?Protocolo>");
    private static final Pattern SITUACAO_TAG = Pattern.compile("<(?:[\\w]+:)?Situacao>([^<]+)</(?:[\\w]+:)?Situacao>");

    private final GinfesXmlBuilder xmlBuilder;
    private final AzureVaultXmlSigningService signer;
    private final ServiceGinfes servicePort;

    public GinfesProvider(GinfesXmlBuilder xmlBuilder,
                          AzureVaultXmlSigningService signer,
                          NfseProviderProperties properties) {
        this.xmlBuilder = xmlBuilder;
        this.signer = signer;

        String wsdlUrl = "https://isshomo.sefin.fortaleza.ce.gov.br/grpfor-iss/ServiceGinfesImplService?wsdl";
        NfseProviderProperties.ProviderConfig ginfesConfig = properties.getProviders().get("ginfes");
        if (ginfesConfig != null && ginfesConfig.getWsdlUrl() != null && !ginfesConfig.getWsdlUrl().isBlank()) {
            wsdlUrl = ginfesConfig.getWsdlUrl();
        }

        ServiceGinfes port = null;
        try {
            port = new ServiceGinfesImplServiceService(new URL(wsdlUrl)).getServiceGinfes();
            log.info("GinfesProvider inicializado com WSDL: {}", wsdlUrl);
        } catch (Exception e) {
            log.warn("GinfesProvider: SOAP port indisponível ({}). Tentativas de envio falharão.", e.getMessage());
        }
        this.servicePort = port;
    }

    @Override
    public String getProviderId() {
        return "GINFES";
    }

    @Override
    public boolean supportsIbsCbs() {
        return true; // GINFES V4 suporta IBS/CBS
    }

    @Override
    public ProtocolType getProtocolType() {
        return ProtocolType.SOAP;
    }

    @Override
    public ProviderResponse enviarLote(EnviarLoteRequest request) {
        if (servicePort == null) {
            return ProviderResponse.failure("SOAP port indisponível", null);
        }

        EmpresaEntity empresa = request.empresa();
        String versao = request.versaoSchema() != null ? request.versaoSchema() : "3";

        try {
            // Construir XML conforme versão
            String xml;
            if ("4".equals(versao)) {
                xml = xmlBuilder.buildXmlV4(request.rpsList(), empresa);
            } else {
                xml = xmlBuilder.buildXmlV3(request.rpsList(), empresa);
            }

            // Assinar XML
            String certAlias = request.certificateAlias();
            String xmlAssinado;
            if (certAlias != null && !certAlias.isBlank()) {
                xmlAssinado = signer.signXmlWithAlias(xml, certAlias);
            } else {
                xmlAssinado = signer.signXml(xml);
            }

            log.debug("[GINFES] XML assinado para envio:\n{}", xmlAssinado);

            // Montar cabeçalho
            String cabecalho = xmlBuilder.buildCabecalhoXml(versao);

            // Chamar SOAP
            String resposta;
            if ("4".equals(versao)) {
                resposta = servicePort.recepcionarLoteRpsV4(cabecalho, xmlAssinado);
            } else {
                resposta = servicePort.recepcionarLoteRpsV3(cabecalho, xmlAssinado);
            }

            log.debug("[GINFES] Resposta SOAP:\n{}", resposta);

            // Extrair protocolo
            String protocolo = extrairProtocolo(resposta);
            if (protocolo == null || protocolo.isBlank()) {
                return ProviderResponse.failure("Protocolo não retornado. Resposta: " + resposta, resposta);
            }

            return ProviderResponse.success(protocolo, resposta);

        } catch (Exception e) {
            log.error("[GINFES] Erro no envio de lote: {}", e.getMessage(), e);
            return ProviderResponse.failure("Erro ao enviar lote GINFES: " + e.getMessage(), null);
        }
    }

    @Override
    public ConsultaSituacaoResponse consultarSituacaoLote(ConsultaSituacaoRequest request) {
        if (servicePort == null) {
            return new ConsultaSituacaoResponse(-1, "SOAP port indisponível", null);
        }

        EmpresaEntity empresa = request.empresa();
        try {
            String xml = xmlBuilder.buildConsultaSituacaoXml(
                    empresa.getCnpj(), empresa.getInscricaoMunicipal(), request.protocolo());

            String certAlias = request.certificateAlias();
            String xmlAssinado;
            if (certAlias != null && !certAlias.isBlank()) {
                xmlAssinado = signer.signXmlWithAlias(xml, certAlias);
            } else {
                xmlAssinado = signer.signXml(xml);
            }

            String cabecalho = xmlBuilder.buildCabecalhoXml("3");
            String resposta = servicePort.consultarSituacaoLoteRpsV3(cabecalho, xmlAssinado);

            log.debug("[GINFES] Resposta consulta situação protocolo {}: {}", request.protocolo(), resposta);

            int situacao = extrairSituacao(resposta);
            String mensagem = null;
            if (situacao == 3) {
                mensagem = extrairTag(resposta, "Mensagem", "Erro no processamento");
            } else if (situacao < 0) {
                mensagem = extrairTag(resposta, "Mensagem", null);
            }

            return new ConsultaSituacaoResponse(situacao, mensagem, resposta);

        } catch (Exception e) {
            log.error("[GINFES] Erro ao consultar situação protocolo {}: {}", request.protocolo(), e.getMessage());
            return new ConsultaSituacaoResponse(-1, e.getMessage(), null);
        }
    }

    @Override
    public ConsultaLoteResponse consultarLote(ConsultaLoteRequest request) {
        if (servicePort == null) {
            return new ConsultaLoteResponse(false, java.util.List.of(), null, "SOAP port indisponível");
        }

        EmpresaEntity empresa = request.empresa();
        try {
            String xml = xmlBuilder.buildConsultaLoteXml(
                    empresa.getCnpj(), empresa.getInscricaoMunicipal(), request.protocolo());

            String certAlias = request.certificateAlias();
            String xmlAssinado;
            if (certAlias != null && !certAlias.isBlank()) {
                xmlAssinado = signer.signXmlWithAlias(xml, certAlias);
            } else {
                xmlAssinado = signer.signXml(xml);
            }

            String cabecalho = xmlBuilder.buildCabecalhoXml("3");
            String resposta = servicePort.consultarLoteRpsV3(cabecalho, xmlAssinado);

            log.debug("[GINFES] Resposta consulta lote protocolo {}: {}", request.protocolo(), resposta);

            // Parsing simplificado — em produção deve usar JAXB unmarshal
            return new ConsultaLoteResponse(true, java.util.List.of(), resposta, null);

        } catch (Exception e) {
            log.error("[GINFES] Erro ao consultar lote protocolo {}: {}", request.protocolo(), e.getMessage());
            return new ConsultaLoteResponse(false, java.util.List.of(), null, e.getMessage());
        }
    }

    @Override
    public CancelamentoResponse cancelarNfse(CancelamentoRequest request) {
        // TODO: Implementar cancelamento GINFES quando necessário
        return new CancelamentoResponse(false, null, "Cancelamento GINFES ainda não implementado");
    }

    // --- Helpers de parsing XML ---

    private String extrairProtocolo(String xml) {
        if (xml == null) return null;
        Matcher m = PROTOCOLO_TAG.matcher(xml);
        if (m.find()) return m.group(1).trim();
        return null;
    }

    private int extrairSituacao(String xml) {
        if (xml == null) return -1;
        Matcher m = SITUACAO_TAG.matcher(xml);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1).trim()); } catch (NumberFormatException ignored) {}
        }
        // Fallback sem namespace
        String val = extrairTag(xml, "Situacao", null);
        if (val != null) {
            try { return Integer.parseInt(val.trim()); } catch (NumberFormatException ignored) {}
        }
        return -1;
    }

    private String extrairTag(String xml, String tag, String fallback) {
        if (xml == null) return fallback;
        int s = xml.indexOf("<" + tag + ">");
        int e = xml.indexOf("</" + tag + ">");
        if (s >= 0 && e > s) return xml.substring(s + tag.length() + 2, e).trim();
        // com namespace
        Matcher m = Pattern.compile("<[^:>]+:" + tag + ">([^<]+)</[^:>]+:" + tag + ">").matcher(xml);
        if (m.find()) return m.group(1).trim();
        return fallback;
    }
}
