package br.com.notafacil.service;

import br.com.notafacil.dto.*;
import br.com.notafacil.entity.EmpresaEntity;
import br.com.notafacil.wsdl.ServiceGinfes;
import br.com.notafacil.wsdl.ServiceGinfesImplServiceService;
import br.com.notafacil.schemas.v4.IbsCbsXmlAdapter;
import br.com.notafacil.schemas.v4.TcDadosServicoV4;
import br.com.notafacil.schemas.*;
import br.com.notafacil.util.XmlNamespaceCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Serviço de emissão de NFSe v4 - SEFIN/Ginfes.
 * Usa RecepcionarLoteRpsV4 com bloco IBS/CBS obrigatório.
 */
@Service
public class NfseServiceV4 {

    private static final Logger log = LoggerFactory.getLogger(NfseServiceV4.class);
    private static final String NS_GINFES = "http://www.ginfes.com.br/servico_enviar_lote_rps_envio_v03.xsd";
    private static final String NS_TIPOS  = "http://www.ginfes.com.br/tipos_v03.xsd";
    private static final String NS_DSIG   = "http://www.w3.org/2000/09/xmldsig#";
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final JaxbXmlService jaxbXmlService;
    private final AzureVaultXmlSigningService signer;
    private final ServiceGinfes servicePort;

    @Value("${nfse.lote.tamanho:50}")
    private int tamanhoBatch;

    private static final URL WSDL_URL;
    static {
        try {
            WSDL_URL = new URL("https://isshomo.sefin.fortaleza.ce.gov.br/grpfor-iss/ServiceGinfesImplService?wsdl");
        } catch (MalformedURLException e) {
            throw new IllegalStateException("URL do WSDL NFSe inválida", e);
        }
    }

    public NfseServiceV4(JaxbXmlService jaxbXmlService, AzureVaultXmlSigningService signer) {
        this.jaxbXmlService = jaxbXmlService;
        this.signer = signer;
        this.servicePort = new ServiceGinfesImplServiceService(WSDL_URL).getServiceGinfes();
    }

    /**
     * Monta e envia lote RPS v4 (com IBS/CBS).
     * Retorna lista de protocolos.
     */
    public List<String> enviarLoteV4(CabecalhoDto cabecalho, List<InfRpsV4RequestDto> lista, EmpresaEntity empresa) {
        if (lista == null || lista.isEmpty()) return List.of();

        List<String> protocolos = new ArrayList<>();
        int batchSize = Math.max(1, tamanhoBatch);

        for (int i = 0; i < lista.size(); i += batchSize) {
            int end = Math.min(i + batchSize, lista.size());
            List<InfRpsV4RequestDto> batch = lista.subList(i, end);
            String xml = construirXmlLoteV4(batch, empresa, cabecalho);
            xml = XmlNamespaceCleaner.limpar(xml);
            String xmlAssinado;
            try {
                xmlAssinado = signer.signXml(xml);
            } catch (Exception e) {
                throw new RuntimeException("Falha ao assinar XML v4: " + e.getMessage(), e);
            }
            String resposta = servicePort.recepcionarLoteRpsV4(cabecalho.toString(), xmlAssinado);
            String protocolo = extrairProtocolo(resposta);
            if (protocolo == null || protocolo.isBlank()) {
                throw new RuntimeException("Protocolo não retornado. Resposta: " + resposta);
            }
            protocolos.add(protocolo);
            log.info("Lote v4 enviado. protocolo={}", protocolo);
        }
        return protocolos;
    }

    /**
     * Constrói o XML do lote v4 com IBS/CBS.
     * Usa JAXB para as partes existentes + String builder para o bloco IBS/CBS.
     */
    private String construirXmlLoteV4(List<InfRpsV4RequestDto> batch, EmpresaEntity empresa, CabecalhoDto cabecalho) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<EnviarLoteRpsEnvio xmlns=\"").append(NS_GINFES).append("\">");

        // Lote
        sb.append("<LoteRps Id=\"Lote").append(System.currentTimeMillis()).append("\" versao=\"2.01\">");
        sb.append("<NumeroLote>").append(UUID.randomUUID().toString().substring(0, 15)).append("</NumeroLote>");
        sb.append("<Cnpj>").append(somenteNumeros(empresa.getCnpj())).append("</Cnpj>");
        sb.append("<InscricaoMunicipal>").append(empresa.getInscricaoMunicipal()).append("</InscricaoMunicipal>");
        sb.append("<QuantidadeRps>").append(batch.size()).append("</QuantidadeRps>");
        sb.append("<ListaRps>");

        for (InfRpsV4RequestDto rps : batch) {
            sb.append(construirRpsXmlV4(rps));
        }

        sb.append("</ListaRps>");
        sb.append("</LoteRps>");
        sb.append("</EnviarLoteRpsEnvio>");
        return sb.toString();
    }

    private String construirRpsXmlV4(InfRpsV4RequestDto rps) {
        InfRpsV4RequestDto.ServicoV4Request srv = rps.servico();
        InfRpsRequestDto.ServicoRequest.ValoresRequest vals = srv.valores();

        StringBuilder sb = new StringBuilder();
        sb.append("<Rps><InfRps Id=\"Rps").append(rps.id()).append("\">");

        // Identificação RPS
        sb.append("<IdentificacaoRps>");
        sb.append("<Numero>").append(rps.id()).append("</Numero>");
        sb.append("<Serie>NF</Serie>");
        sb.append("<Tipo>1</Tipo>");
        sb.append("</IdentificacaoRps>");

        sb.append("<DataEmissao>").append(rps.dataEmissao() != null
            ? rps.dataEmissao().format(DTF) : LocalDateTime.now().format(DTF))
            .append("</DataEmissao>");
        sb.append("<NaturezaOperacao>").append(rps.naturezaOperacao()).append("</NaturezaOperacao>");
        sb.append("<RegimeEspecialTributacao>").append(rps.regimeEspecialTributacao()).append("</RegimeEspecialTributacao>");
        sb.append("<OptanteSimplesNacional>").append(rps.optanteSimplesNacional()).append("</OptanteSimplesNacional>");
        sb.append("<IncentivadorCultural>").append(rps.incentivadorCultural()).append("</IncentivadorCultural>");
        sb.append("<Status>").append(rps.status()).append("</Status>");

        // Serviço
        sb.append("<Servico>");
        sb.append("<Valores>");
        sb.append("<ValorServicos>").append(vals.valorServicos()).append("</ValorServicos>");
        sb.append("<ValorDeducoes>").append(vals.valorDeducoes()).append("</ValorDeducoes>");
        sb.append("<ValorPis>").append(vals.valorPis()).append("</ValorPis>");
        sb.append("<ValorCofins>").append(vals.valorCofins()).append("</ValorCofins>");
        sb.append("<ValorInss>").append(vals.valorInss()).append("</ValorInss>");
        sb.append("<ValorIr>").append(vals.valorIr()).append("</ValorIr>");
        sb.append("<ValorCsll>").append(vals.valorCsll()).append("</ValorCsll>");
        sb.append("<IssRetido>").append(vals.issRetido()).append("</IssRetido>");
        sb.append("<ValorIss>").append(vals.valorIss()).append("</ValorIss>");
        sb.append("<ValorIssRetido>").append(vals.valorIssRetido()).append("</ValorIssRetido>");
        sb.append("<OutrasRetencoes>").append(vals.outrasRetencoes()).append("</OutrasRetencoes>");
        sb.append("<BaseCalculo>").append(vals.baseCalculo()).append("</BaseCalculo>");
        sb.append("<Aliquota>").append(vals.aliquota()).append("</Aliquota>");
        sb.append("<ValorLiquidoNfse>").append(vals.valorLiquidoNfse()).append("</ValorLiquidoNfse>");
        sb.append("<DescontoIncondicionado>").append(vals.descontoIncondicionado()).append("</DescontoIncondicionado>");
        sb.append("<DescontoCondicionado>").append(vals.descontoCondicionado()).append("</DescontoCondicionado>");
        sb.append("</Valores>");

        sb.append("<ItemListaServico>").append(srv.itemListaServico()).append("</ItemListaServico>");
        sb.append("<CodigoTributacaoMunicipio>").append(srv.codigoTributacaoMunicipio()).append("</CodigoTributacaoMunicipio>");
        sb.append("<Discriminacao>").append(escapeXml(srv.discriminacao())).append("</Discriminacao>");
        sb.append("<CodigoMunicipio>").append(srv.codigoMunicipio()).append("</CodigoMunicipio>");

        // Campos v4: NBS e CodigoNbs
        sb.append("<CodigoNbs>").append(srv.nbs()).append("</CodigoNbs>");

        // ─── Bloco IBS/CBS (v4) ─────────────────────────────────────────────
        sb.append(construirIbsCbsXml(srv.ibsCbs(), srv.indicadorOperacao()));
        // ────────────────────────────────────────────────────────────────────

        sb.append("</Servico>");

        // Prestador
        sb.append("<Prestador>");
        sb.append("<Cnpj>").append(somenteNumeros(rps.prestador().cnpj())).append("</Cnpj>");
        sb.append("<InscricaoMunicipal>").append(rps.prestador().inscricaoMunicipal()).append("</InscricaoMunicipal>");
        sb.append("</Prestador>");

        // Tomador
        sb.append("<Tomador>");
        if (rps.tomador().identificacaoTomador() != null) {
            sb.append("<IdentificacaoTomador>");
            String cpf = rps.tomador().identificacaoTomador().cpf();
            if (cpf != null && cpf.length() == 11) {
                sb.append("<Cpf>").append(cpf).append("</Cpf>");
            } else {
                sb.append("<Cnpj>").append(somenteNumeros(cpf)).append("</Cnpj>");
            }
            sb.append("</IdentificacaoTomador>");
        }
        sb.append("<RazaoSocial>").append(escapeXml(rps.tomador().razaoSocial())).append("</RazaoSocial>");
        if (rps.tomador().endereco() != null) {
            sb.append("<Endereco>");
            sb.append("<CodigoMunicipio>").append(rps.tomador().endereco().codigoMunicipio()).append("</CodigoMunicipio>");
            sb.append("<Uf>").append(rps.tomador().endereco().uf()).append("</Uf>");
            sb.append("</Endereco>");
        }
        sb.append("</Tomador>");
        sb.append("</InfRps></Rps>");

        return sb.toString();
    }

    private String construirIbsCbsXml(IbsCbsDto ibs, String indicadorOperacao) {
        if (ibs == null) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("<IbsCbs>");

        // Indicador de Operação (v4)
        if (indicadorOperacao != null) {
            sb.append("<CodigoIndicadorOperacao>").append(indicadorOperacao).append("</CodigoIndicadorOperacao>");
        }

        // Indicadores
        if (ibs.codigoIndicadorFinalidadeNFSe() != null) {
            sb.append("<CodigoIndicadorFinalidadeNFSe>")
              .append(ibs.codigoIndicadorFinalidadeNFSe())
              .append("</CodigoIndicadorFinalidadeNFSe>");
        }
        if (ibs.codigoIndicadorOperacaoUsoConsumoPessoal() != null) {
            sb.append("<CodigoIndicadorOperacaoUsoConsumoPessoal>")
              .append(ibs.codigoIndicadorOperacaoUsoConsumoPessoal())
              .append("</CodigoIndicadorOperacaoUsoConsumoPessoal>");
        }
        if (ibs.tipoOp() != null) {
            sb.append("<TipoOp>").append(ibs.tipoOp()).append("</TipoOp>");
        }

        // NFSe Referenciadas
        if (ibs.nfseReferenciadas() != null && !ibs.nfseReferenciadas().isEmpty()) {
            sb.append("<GrupoNFSeReferenciada>");
            for (String chave : ibs.nfseReferenciadas()) {
                sb.append("<ChaveNFSeReferenciada>").append(chave).append("</ChaveNFSeReferenciada>");
            }
            sb.append("</GrupoNFSeReferenciada>");
        }

        if (ibs.tipoEnteGovernamental() != null) {
            sb.append("<TipoEnteGovernamental>").append(ibs.tipoEnteGovernamental()).append("</TipoEnteGovernamental>");
        }
        if (ibs.indDest() != null) {
            sb.append("<IndDest>").append(ibs.indDest()).append("</IndDest>");
        }

        // Valores → Tributos IBS/CBS
        sb.append("<Valores>");
        sb.append("<TributosIbsCbs>");
        sb.append("<GrupoIbsCbs>");

        if (ibs.cst() != null) {
            sb.append("<CST>").append(ibs.cst()).append("</CST>");
        }
        if (ibs.codigoClassTrib() != null) {
            sb.append("<CodigoClassTrib>").append(ibs.codigoClassTrib()).append("</CodigoClassTrib>");
        }
        if (ibs.codigoCreditoPresumido() != null) {
            sb.append("<CodigoCreditoPresumido>").append(ibs.codigoCreditoPresumido()).append("</CodigoCreditoPresumido>");
        }

        sb.append("</GrupoIbsCbs>");
        sb.append("</TributosIbsCbs>");
        sb.append("</Valores>");
        sb.append("</IbsCbs>");

        return sb.toString();
    }

    private String extrairProtocolo(String respostaXml) {
        if (respostaXml == null) return null;
        int ini = respostaXml.indexOf("<NumeroLoteProtocolo>");
        if (ini < 0) ini = respostaXml.indexOf("<Protocolo>");
        if (ini < 0) return null;
        int fim = respostaXml.indexOf("<", ini + 1);
        return respostaXml.substring(ini + (ini > 0 ? 20 : 12), fim).replaceAll("[^0-9]", "");
    }

    private static String somenteNumeros(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }

    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                 .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
