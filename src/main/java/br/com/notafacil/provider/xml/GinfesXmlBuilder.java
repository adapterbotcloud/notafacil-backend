package br.com.notafacil.provider.xml;

import br.com.notafacil.dto.*;
import br.com.notafacil.entity.EmpresaEntity;
import br.com.notafacil.entity.RpsEntity;
import br.com.notafacil.mapping.NfseMapper;
import br.com.notafacil.service.JaxbXmlService;
import br.com.notafacil.util.XmlNamespaceCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Constrói XML de lotes RPS no formato GINFES (V3 via JAXB, V4 via string builder).
 */
@Component
public class GinfesXmlBuilder {

    private static final Logger log = LoggerFactory.getLogger(GinfesXmlBuilder.class);
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final String NS_GINFES = "http://www.ginfes.com.br/servico_enviar_lote_rps_envio_v03.xsd";
    private static final String NS_TIPOS = "http://www.ginfes.com.br/tipos_v03.xsd";
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final NfseMapper mapper;
    private final JaxbXmlService jaxbXmlService;

    public GinfesXmlBuilder(NfseMapper mapper, JaxbXmlService jaxbXmlService) {
        this.mapper = mapper;
        this.jaxbXmlService = jaxbXmlService;
    }

    /**
     * Constrói XML V3 (JAXB) para um batch de RPS entities.
     */
    public String buildXmlV3(List<RpsEntity> rpsBatch, EmpresaEntity empresa) {
        List<InfRpsRequestDto> completos = toInfRpsList(rpsBatch);
        LoteRpsDto lote = montarLoteDto(completos);
        EnviarLoteRpsEnvioDto envio = new EnviarLoteRpsEnvioDto(lote);

        String xml = jaxbXmlService.marshal(mapper.toSchema(envio));
        return XmlNamespaceCleaner.limpar(xml);
    }

    /**
     * Constrói XML V4 (string builder com bloco IBS/CBS) para um batch de RPS entities.
     */
    public String buildXmlV4(List<RpsEntity> rpsBatch, EmpresaEntity empresa) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<EnviarLoteRpsEnvio xmlns=\"").append(NS_GINFES).append("\">");

        sb.append("<LoteRps Id=\"Lote").append(System.currentTimeMillis()).append("\" versao=\"2.01\">");
        sb.append("<NumeroLote>").append(UUID.randomUUID().toString().substring(0, 15)).append("</NumeroLote>");
        sb.append("<Cnpj>").append(onlyDigits(empresa.getCnpj())).append("</Cnpj>");
        sb.append("<InscricaoMunicipal>").append(empresa.getInscricaoMunicipal()).append("</InscricaoMunicipal>");
        sb.append("<QuantidadeRps>").append(rpsBatch.size()).append("</QuantidadeRps>");
        sb.append("<ListaRps>");

        for (RpsEntity rps : rpsBatch) {
            sb.append(buildRpsXmlV4(rps, empresa));
        }

        sb.append("</ListaRps>");
        sb.append("</LoteRps>");
        sb.append("</EnviarLoteRpsEnvio>");

        return XmlNamespaceCleaner.limpar(sb.toString());
    }

    /**
     * Constrói XML de consulta de situação de lote RPS (V3).
     */
    public String buildConsultaSituacaoXml(String cnpj, String inscricaoMunicipal, String protocolo) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<ConsultarSituacaoLoteRpsEnvio xmlns=\"http://www.ginfes.com.br/servico_consultar_situacao_lote_rps_envio_v03.xsd\">" +
                "<Prestador><Cnpj>" + cnpj + "</Cnpj>" +
                "<InscricaoMunicipal>" + inscricaoMunicipal + "</InscricaoMunicipal></Prestador>" +
                "<Protocolo>" + protocolo + "</Protocolo>" +
                "</ConsultarSituacaoLoteRpsEnvio>";
    }

    /**
     * Constrói XML de consulta de lote RPS (V3).
     */
    public String buildConsultaLoteXml(String cnpj, String inscricaoMunicipal, String protocolo) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<ConsultarLoteRpsEnvio xmlns=\"http://www.ginfes.com.br/servico_consultar_lote_rps_envio_v03.xsd\">" +
                "<Prestador><Cnpj>" + cnpj + "</Cnpj>" +
                "<InscricaoMunicipal>" + inscricaoMunicipal + "</InscricaoMunicipal></Prestador>" +
                "<Protocolo>" + protocolo + "</Protocolo>" +
                "</ConsultarLoteRpsEnvio>";
    }

    public String buildCabecalhoXml(String versao) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<ns2:cabecalho versao=\"" + versao + "\" xmlns:ns2=\"http://www.ginfes.com.br/cabecalho_v03.xsd\">" +
                "<versaoDados>" + versao + "</versaoDados></ns2:cabecalho>";
    }

    // --- Conversão Entity → DTO (extraído de NfseService1) ---

    public List<InfRpsRequestDto> toInfRpsList(List<RpsEntity> entidades) {
        List<InfRpsRequestDto> out = new ArrayList<>(entidades.size());
        for (RpsEntity e : entidades) {
            out.add(fromEntityToInfRpsRequest(e));
        }
        return out;
    }

    public InfRpsRequestDto fromEntityToInfRpsRequest(RpsEntity base) {
        EmpresaEntity emp = base.getEmpresa();

        BigDecimal valorServicos = base.getValorServicos();
        BigDecimal aliquotaPerc = emp.getAliquota();
        BigDecimal aliquotaDecimal = aliquotaPerc.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
        BigDecimal baseCalculo = valorServicos.setScale(2, RoundingMode.HALF_UP);
        BigDecimal valorIss = valorServicos.multiply(aliquotaDecimal).setScale(2, RoundingMode.HALF_UP);
        BigDecimal valorLiquido = valorServicos.subtract(valorIss).setScale(2, RoundingMode.HALF_UP);

        var valoresReq = new InfRpsRequestDto.ServicoRequest.ValoresRequest(
                valorServicos, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO,
                2, valorIss, ZERO, ZERO, baseCalculo,
                aliquotaPerc.setScale(4, RoundingMode.HALF_UP),
                valorLiquido, ZERO, ZERO
        );

        var servicoReq = new InfRpsRequestDto.ServicoRequest(
                valoresReq,
                emp.getItemListaServico(),
                emp.getCodigoTributacaoMunicipio(),
                base.getDiscriminacao() != null ? base.getDiscriminacao() : "Serviço",
                emp.getCodigoMunicipio()
        );

        var prestadorReq = new InfRpsRequestDto.PrestadorRequest(
                emp.getCnpj(), emp.getInscricaoMunicipal()
        );

        var tomadorReq = new InfRpsRequestDto.TomadorRequest(
                new InfRpsRequestDto.TomadorRequest.IdentificacaoTomadorRequest(base.getTomadorCpf(), null),
                base.getTomadorRazaoSocial(), null, null
        );

        return new InfRpsRequestDto(
                base.getId(), 1, emp.getRegimeEspecialTributacao(),
                emp.getOptanteSimplesNacional(), emp.getIncentivadorCultural(),
                1, servicoReq, prestadorReq, tomadorReq, base.getDataEmissao()
        );
    }

    // --- Internal helpers ---

    private LoteRpsDto montarLoteDto(List<InfRpsRequestDto> batch) {
        InfRpsRequestDto first = batch.get(0);
        String numeroLote = "1" + System.currentTimeMillis();
        List<RpsDto> rpsList = new ArrayList<>(batch.size());

        for (InfRpsRequestDto req : batch) {
            rpsList.add(toRpsDto(req, req.dataEmissao()));
        }

        return new LoteRpsDto(
                null, numeroLote,
                first.prestador().cnpj(),
                first.prestador().inscricaoMunicipal(),
                batch.size(), rpsList
        );
    }

    private RpsDto toRpsDto(InfRpsRequestDto req, LocalDateTime dataEmissao) {
        InfRpsDto inf = new InfRpsDto(
                new IdentificacaoRpsDto(req.id().toString(), "A", 1),
                dataEmissao != null ? dataEmissao.truncatedTo(java.time.temporal.ChronoUnit.SECONDS)
                        : LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS),
                req.naturezaOperacao(), req.regimeEspecialTributacao(),
                req.optanteSimplesNacional(), req.incentivadorCultural(), req.status(),
                new ServicoDto(
                        new ValoresDto(
                                req.servico().valores().valorServicos(),
                                req.servico().valores().valorDeducoes(),
                                req.servico().valores().valorPis(),
                                req.servico().valores().valorCofins(),
                                req.servico().valores().valorInss(),
                                req.servico().valores().valorIr(),
                                req.servico().valores().valorCsll(),
                                req.servico().valores().issRetido(),
                                req.servico().valores().valorIss(),
                                req.servico().valores().valorIssRetido(),
                                req.servico().valores().outrasRetencoes(),
                                req.servico().valores().baseCalculo(),
                                req.servico().valores().aliquota(),
                                req.servico().valores().valorLiquidoNfse(),
                                req.servico().valores().descontoIncondicionado(),
                                req.servico().valores().descontoCondicionado()
                        ),
                        req.servico().itemListaServico(),
                        req.servico().codigoTributacaoMunicipio(),
                        req.servico().discriminacao(),
                        req.servico().codigoMunicipio()
                ),
                new PrestadorDto(req.prestador().cnpj(), req.prestador().inscricaoMunicipal()),
                new TomadorDto(
                        new IdentificacaoTomadorDto(
                                req.tomador().identificacaoTomador().cpf(), null,
                                req.tomador().identificacaoTomador().inscricaoMunicipal()
                        ),
                        req.tomador().razaoSocial(), null, null
                )
        );
        return new RpsDto(inf, null);
    }

    private String buildRpsXmlV4(RpsEntity rps, EmpresaEntity emp) {
        BigDecimal valorServicos = rps.getValorServicos();
        BigDecimal aliquotaPerc = emp.getAliquota();
        BigDecimal aliquotaDecimal = aliquotaPerc.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
        BigDecimal valorIss = valorServicos.multiply(aliquotaDecimal).setScale(2, RoundingMode.HALF_UP);

        StringBuilder sb = new StringBuilder();
        sb.append("<Rps><InfRps Id=\"Rps").append(rps.getId()).append("\">");

        sb.append("<IdentificacaoRps>");
        sb.append("<Numero>").append(rps.getId()).append("</Numero>");
        sb.append("<Serie>NF</Serie><Tipo>1</Tipo>");
        sb.append("</IdentificacaoRps>");

        sb.append("<DataEmissao>").append(rps.getDataEmissao() != null
                ? rps.getDataEmissao().format(DTF) : LocalDateTime.now().format(DTF)).append("</DataEmissao>");
        sb.append("<NaturezaOperacao>").append(rps.getNaturezaOperacao()).append("</NaturezaOperacao>");
        sb.append("<RegimeEspecialTributacao>").append(rps.getRegimeEspecialTributacao()).append("</RegimeEspecialTributacao>");
        sb.append("<OptanteSimplesNacional>").append(rps.getOptanteSimplesNacional()).append("</OptanteSimplesNacional>");
        sb.append("<IncentivadorCultural>").append(rps.getIncentivadorCultural()).append("</IncentivadorCultural>");
        sb.append("<Status>1</Status>");

        // Serviço
        sb.append("<Servico><Valores>");
        sb.append("<ValorServicos>").append(valorServicos).append("</ValorServicos>");
        sb.append("<ValorDeducoes>0.00</ValorDeducoes>");
        sb.append("<ValorPis>0.00</ValorPis><ValorCofins>0.00</ValorCofins>");
        sb.append("<ValorInss>0.00</ValorInss><ValorIr>0.00</ValorIr><ValorCsll>0.00</ValorCsll>");
        sb.append("<IssRetido>2</IssRetido>");
        sb.append("<ValorIss>").append(valorIss).append("</ValorIss>");
        sb.append("<ValorIssRetido>0.00</ValorIssRetido><OutrasRetencoes>0.00</OutrasRetencoes>");
        sb.append("<BaseCalculo>").append(valorServicos.setScale(2, RoundingMode.HALF_UP)).append("</BaseCalculo>");
        sb.append("<Aliquota>").append(aliquotaPerc.setScale(4, RoundingMode.HALF_UP)).append("</Aliquota>");
        sb.append("<ValorLiquidoNfse>").append(valorServicos.subtract(valorIss).setScale(2, RoundingMode.HALF_UP)).append("</ValorLiquidoNfse>");
        sb.append("<DescontoIncondicionado>0.00</DescontoIncondicionado>");
        sb.append("<DescontoCondicionado>0.00</DescontoCondicionado>");
        sb.append("</Valores>");

        sb.append("<ItemListaServico>").append(emp.getItemListaServico()).append("</ItemListaServico>");
        sb.append("<CodigoTributacaoMunicipio>").append(emp.getCodigoTributacaoMunicipio()).append("</CodigoTributacaoMunicipio>");
        sb.append("<Discriminacao>").append(escapeXml(rps.getDiscriminacao())).append("</Discriminacao>");
        sb.append("<CodigoMunicipio>").append(emp.getCodigoMunicipio()).append("</CodigoMunicipio>");
        sb.append("<CodigoNbs>").append(emp.getNbs()).append("</CodigoNbs>");

        // IBS/CBS block
        sb.append("<IbsCbs>");
        sb.append("<CodigoIndicadorOperacao>").append(emp.getIndicadorOperacao()).append("</CodigoIndicadorOperacao>");
        sb.append("<Valores><TributosIbsCbs><GrupoIbsCbs>");
        sb.append("<CST>").append(emp.getCst()).append("</CST>");
        sb.append("<CodigoClassTrib>").append(emp.getClassificacaoTributaria()).append("</CodigoClassTrib>");
        sb.append("</GrupoIbsCbs></TributosIbsCbs></Valores>");
        sb.append("</IbsCbs>");

        sb.append("</Servico>");

        // Prestador
        sb.append("<Prestador>");
        sb.append("<Cnpj>").append(onlyDigits(emp.getCnpj())).append("</Cnpj>");
        sb.append("<InscricaoMunicipal>").append(emp.getInscricaoMunicipal()).append("</InscricaoMunicipal>");
        sb.append("</Prestador>");

        // Tomador
        sb.append("<Tomador>");
        String cpf = onlyDigits(rps.getTomadorCpf());
        if (cpf != null && !cpf.isEmpty()) {
            sb.append("<IdentificacaoTomador>");
            if (cpf.length() == 11) {
                sb.append("<Cpf>").append(cpf).append("</Cpf>");
            } else {
                sb.append("<Cnpj>").append(cpf).append("</Cnpj>");
            }
            sb.append("</IdentificacaoTomador>");
        }
        sb.append("<RazaoSocial>").append(escapeXml(rps.getTomadorRazaoSocial())).append("</RazaoSocial>");
        sb.append("</Tomador>");

        sb.append("</InfRps></Rps>");
        return sb.toString();
    }

    private static String onlyDigits(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }

    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
