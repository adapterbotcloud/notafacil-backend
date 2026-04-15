package br.com.notafacil.schemas.v4;

import jakarta.xml.bind.annotation.*;

/**
 * Bloco IBS/CBS - campos obrigatórios NFSe v4 (SEFIN/Ginfes).
 * Reforma tributária. Added to tcDadosServico.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tcDadosIbsCbs", namespace = "http://www.ginfes.com.br/tipos_v03.xsd",
         propOrder = {
             "codigoIndicadorFinalidadeNFSe",
             "codigoIndicadorOperacaoUsoConsumoPessoal",
             "codigoIndicadorOperacao",
             "tipoOp",
             "grupoNFSeReferenciada",
             "tipoEnteGovernamental",
             "indDest",
             "valores"
         })
public class TcDadosIbsCbs {

    @XmlElement(name = "CodigoIndicadorFinalidadeNFSe", namespace = "http://www.ginfes.com.br/tipos_v03.xsd")
    protected String codigoIndicadorFinalidadeNFSe;

    @XmlElement(name = "CodigoIndicadorOperacaoUsoConsumoPessoal", namespace = "http://www.ginfes.com.br/tipos_v03.xsd")
    protected String codigoIndicadorOperacaoUsoConsumoPessoal;

    @XmlElement(name = "CodigoIndicadorOperacao", namespace = "http://www.ginfes.com.br/tipos_v03.xsd")
    protected String codigoIndicadorOperacao;

    @XmlElement(name = "TipoOp", namespace = "http://www.ginfes.com.br/tipos_v03.xsd")
    protected String tipoOp;

    @XmlElementWrapper(name = "GrupoNFSeReferenciada", namespace = "http://www.ginfes.com.br/tipos_v03.xsd")
    @XmlElement(name = "ChaveNFSeReferenciada", namespace = "http://www.ginfes.com.br/tipos_v03.xsd")
    protected java.util.List<String> grupoNFSeReferenciada;

    @XmlElement(name = "TipoEnteGovernamental", namespace = "http://www.ginfes.com.br/tipos_v03.xsd")
    protected String tipoEnteGovernamental;

    @XmlElement(name = "IndDest", namespace = "http://www.ginfes.com.br/tipos_v03.xsd")
    protected String indDest;

    @XmlElement(name = "Valores", namespace = "http://www.ginfes.com.br/tipos_v03.xsd")
    protected TcValoresIbsCbs valores;

    // ─── Getters e Setters ────────────────────────────────────────────────────

    public String getCodigoIndicadorFinalidadeNFSe() { return codigoIndicadorFinalidadeNFSe; }
    public void setCodigoIndicadorFinalidadeNFSe(String v) { this.codigoIndicadorFinalidadeNFSe = v; }

    public String getCodigoIndicadorOperacaoUsoConsumoPessoal() { return codigoIndicadorOperacaoUsoConsumoPessoal; }
    public void setCodigoIndicadorOperacaoUsoConsumoPessoal(String v) { this.codigoIndicadorOperacaoUsoConsumoPessoal = v; }

    public String getCodigoIndicadorOperacao() { return codigoIndicadorOperacao; }
    public void setCodigoIndicadorOperacao(String v) { this.codigoIndicadorOperacao = v; }

    public String getTipoOp() { return tipoOp; }
    public void setTipoOp(String v) { this.tipoOp = v; }

    public java.util.List<String> getGrupoNFSeReferenciada() { return grupoNFSeReferenciada; }
    public void setGrupoNFSeReferenciada(java.util.List<String> v) { this.grupoNFSeReferenciada = v; }

    public String getTipoEnteGovernamental() { return tipoEnteGovernamental; }
    public void setTipoEnteGovernamental(String v) { this.tipoEnteGovernamental = v; }

    public String getIndDest() { return indDest; }
    public void setIndDest(String v) { this.indDest = v; }

    public TcValoresIbsCbs getValores() { return valores; }
    public void setValores(TcValoresIbsCbs v) { this.valores = v; }
}
