package br.com.notafacil.schemas.v4;

import jakarta.xml.bind.annotation.*;
import java.math.BigDecimal;

/** Valores IBS/CBS - dentro do bloco IbsCbs. */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tcValoresIbsCbs", namespace = "http://www.ginfes.com.br/tipos_v03.xsd",
         propOrder = {"grupoReeRepRes", "tributosIbsCbs"})
public class TcValoresIbsCbs {

    @XmlElementWrapper(name = "GrupoReeRepRes", namespace = "http://www.ginfes.com.br/tipos_v03.xsd")
    @XmlElement(name = "Documentos", namespace = "http://www.ginfes.com.br/tipos_v03.xsd")
    protected java.util.List<TcDocumentos> grupoReeRepRes;

    @XmlElement(name = "TributosIbsCbs", namespace = "http://www.ginfes.com.br/tipos_v03.xsd")
    protected TcTributosIbsCbs tributosIbsCbs;

    public java.util.List<TcDocumentos> getGrupoReeRepRes() { return grupoReeRepRes; }
    public void setGrupoReeRepRes(java.util.List<TcDocumentos> v) { this.grupoReeRepRes = v; }

    public TcTributosIbsCbs getTributosIbsCbs() { return tributosIbsCbs; }
    public void setTributosIbsCbs(TcTributosIbsCbs v) { this.tributosIbsCbs = v; }
}

/** Documentos no grupo REE/REP/RES. */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tcDocumentos", namespace = "http://www.ginfes.com.br/tipos_v03.xsd")
class TcDocumentos {
    // simplified - not all fields mapped
}

/** Container de Tributos IBS/CBS. */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tcTributosIbsCbs", namespace = "http://www.ginfes.com.br/tipos_v03.xsd",
         propOrder = {"grupoIbsCbs"})
class TcTributosIbsCbs {

    @XmlElement(name = "GrupoIbsCbs", namespace = "http://www.ginfes.com.br/tipos_v03.xsd")
    protected java.util.List<TcGrupoIbsCbs> grupoIbsCbs;

    public java.util.List<TcGrupoIbsCbs> getGrupoIbsCbs() { return grupoIbsCbs; }
    public void setGrupoIbsCbs(java.util.List<TcGrupoIbsCbs> v) { this.grupoIbsCbs = v; }
}

/** Grupo IBS/CBS com CST e Classificação Tributária. */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tcGrupoIbsCbs", namespace = "http://www.ginfes.com.br/tipos_v03.xsd",
         propOrder = {"cst", "codigoClassTrib", "codigoCreditoPresumido",
                      "grupoInfoTributacaoRegular", "grupoDiferimento"})
class TcGrupoIbsCbs {

    @XmlElement(name = "CST", namespace = "http://www.ginfes.com.br/tipos_v03.xsd")
    protected String cst;

    @XmlElement(name = "CodigoClassTrib", namespace = "http://www.ginfes.com.br/tipos_v03.xsd")
    protected String codigoClassTrib;

    @XmlElement(name = "CodigoCreditoPresumido", namespace = "http://www.ginfes.com.br/tipos_v03.xsd")
    protected String codigoCreditoPresumido;

    @XmlElement(name = "GrupoInfoTributacaoRegular", namespace = "http://www.ginfes.com.br/tipos_v03.xsd")
    protected TcGrupoInfoTributacaoRegular grupoInfoTributacaoRegular;

    @XmlElement(name = "GrupoDiferimento", namespace = "http://www.ginfes.com.br/tipos_v03.xsd")
    protected TcGrupoDiferimento grupoDiferimento;

    // ─── Getters/Setters ─────────────────────────────────────────────────────

    public String getCst() { return cst; }
    public void setCst(String v) { this.cst = v; }

    public String getCodigoClassTrib() { return codigoClassTrib; }
    public void setCodigoClassTrib(String v) { this.codigoClassTrib = v; }

    public String getCodigoCreditoPresumido() { return codigoCreditoPresumido; }
    public void setCodigoCreditoPresumido(String v) { this.codigoCreditoPresumido = v; }

    public TcGrupoInfoTributacaoRegular getGrupoInfoTributacaoRegular() { return grupoInfoTributacaoRegular; }
    public void setGrupoInfoTributacaoRegular(TcGrupoInfoTributacaoRegular v) { this.grupoInfoTributacaoRegular = v; }

    public TcGrupoDiferimento getGrupoDiferimento() { return grupoDiferimento; }
    public void setGrupoDiferimento(TcGrupoDiferimento v) { this.grupoDiferimento = v; }
}

class TcGrupoInfoTributacaoRegular {
    protected String codigoSitTribReg;
    protected String codigoClassTribReg;

    public String getCodigoSitTribReg() { return codigoSitTribReg; }
    public void setCodigoSitTribReg(String v) { this.codigoSitTribReg = v; }
    public String getCodigoClassTribReg() { return codigoClassTribReg; }
    public void setCodigoClassTribReg(String v) { this.codigoClassTribReg = v; }
}

class TcGrupoDiferimento {
    protected BigDecimal percentualDiferimentoIbsUf;
    protected BigDecimal percentualDiferimentoIbsMun;
    protected BigDecimal percentualDiferimentoCbs;

    public BigDecimal getPercentualDiferimentoIbsUf() { return percentualDiferimentoIbsUf; }
    public void setPercentualDiferimentoIbsUf(BigDecimal v) { this.percentualDiferimentoIbsUf = v; }
    public BigDecimal getPercentualDiferimentoIbsMun() { return percentualDiferimentoIbsMun; }
    public void setPercentualDiferimentoIbsMun(BigDecimal v) { this.percentualDiferimentoIbsMun = v; }
    public BigDecimal getPercentualDiferimentoCbs() { return percentualDiferimentoCbs; }
    public void setPercentualDiferimentoCbs(BigDecimal v) { this.percentualDiferimentoCbs = v; }
}
