package br.com.notafacil.schemas.v4;

import br.com.notafacil.schemas.*;
import jakarta.xml.bind.annotation.*;
import java.math.BigDecimal;

/**
 * Versão estendida de tcDadosServico com campos obrigatórios v4 (IBS/CBS).
 * Copia todos os campos existentes e adiciona o bloco IbsCbs.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tcDadosServico", namespace = "http://www.ginfes.com.br/tipos_v03.xsd",
         propOrder = {"valores", "itemListaServico", "codigoCnae",
                      "codigoTributacaoMunicipio", "discriminacao",
                      "codigoMunicipio", "codigoNbs", "ibsCbs"})
public class TcDadosServicoV4 {

    @XmlElement(namespace = "http://www.ginfes.com.br/tipos_v03.xsd", required = true)
    protected TcValores valores = new TcValores();

    @XmlElement(namespace = "http://www.ginfes.com.br/tipos_v03.xsd", required = true)
    protected String itemListaServico;

    @XmlElement(namespace = "http://www.ginfes.com.br/tipos_v03.xsd")
    protected Integer codigoCnae;

    @XmlElement(namespace = "http://www.ginfes.com.br/tipos_v03.xsd")
    protected String codigoTributacaoMunicipio;

    @XmlElement(namespace = "http://www.ginfes.com.br/tipos_v03.xsd", required = true)
    protected String discriminacao;

    @XmlElement(namespace = "http://www.ginfes.com.br/tipos_v03.xsd", required = true)
    protected int codigoMunicipio;

    @XmlElement(namespace = "http://www.ginfes.com.br/tipos_v03.xsd")
    protected String codigoNbs;

    /** Bloco IBS/CBS - v4 obrigatório */
    @XmlElement(namespace = "http://www.ginfes.com.br/tipos_v03.xsd")
    protected TcDadosIbsCbs ibsCbs;

    // ─── Construtores ─────────────────────────────────────────────────────────

    public TcDadosServicoV4() {}

    /** Cópia a partir de um tcDadosServico existente (JAXB v3). */
    public TcDadosServicoV4(TcDadosServico src) {
        if (src == null) return;
        if (src.getValores() != null) {
            TcValores v = new TcValores();
            v.setValorServicos(src.getValores().getValorServicos());
            v.setValorDeducoes(src.getValores().getValorDeducoes());
            v.setValorPis(src.getValores().getValorPis());
            v.setValorCofins(src.getValores().getValorCofins());
            v.setValorInss(src.getValores().getValorInss());
            v.setValorIr(src.getValores().getValorIr());
            v.setValorCsll(src.getValores().getValorCsll());
            v.setIssRetido(src.getValores().getIssRetido());
            v.setValorIss(src.getValores().getValorIss());
            v.setValorIssRetido(src.getValores().getValorIssRetido());
            v.setOutrasRetencoes(src.getValores().getOutrasRetencoes());
            v.setBaseCalculo(src.getValores().getBaseCalculo());
            v.setAliquota(src.getValores().getAliquota());
            v.setValorLiquidoNfse(src.getValores().getValorLiquidoNfse());
            v.setDescontoIncondicionado(src.getValores().getDescontoIncondicionado());
            v.setDescontoCondicionado(src.getValores().getDescontoCondicionado());
            this.valores = v;
        }
        this.itemListaServico = src.getItemListaServico();
        this.codigoCnae = src.getCodigoCnae();
        this.codigoTributacaoMunicipio = src.getCodigoTributacaoMunicipio();
        this.discriminacao = src.getDiscriminacao();
        this.codigoMunicipio = src.getCodigoMunicipio();
        // codigoNbs didn't exist in v3 - this is the new field
    }

    // ─── Getters e Setters ───────────────────────────────────────────────────

    public TcValores getValores() { return valores; }
    public void setValores(TcValores v) { this.valores = v; }

    public String getItemListaServico() { return itemListaServico; }
    public void setItemListaServico(String v) { this.itemListaServico = v; }

    public Integer getCodigoCnae() { return codigoCnae; }
    public void setCodigoCnae(Integer v) { this.codigoCnae = v; }

    public String getCodigoTributacaoMunicipio() { return codigoTributacaoMunicipio; }
    public void setCodigoTributacaoMunicipio(String v) { this.codigoTributacaoMunicipio = v; }

    public String getDiscriminacao() { return discriminacao; }
    public void setDiscriminacao(String v) { this.discriminacao = v; }

    public int getCodigoMunicipio() { return codigoMunicipio; }
    public void setCodigoMunicipio(int v) { this.codigoMunicipio = v; }

    public String getCodigoNbs() { return codigoNbs; }
    public void setCodigoNbs(String v) { this.codigoNbs = v; }

    public TcDadosIbsCbs getIbsCbs() { return ibsCbs; }
    public void setIbsCbs(TcDadosIbsCbs v) { this.ibsCbs = v; }
}
