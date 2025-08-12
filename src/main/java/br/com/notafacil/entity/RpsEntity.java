package br.com.notafacil.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidade que representa um RPS emitido.
 */
@Entity
@Table(name = "rps")
public class RpsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID de negócio enviado pelo cliente para associação
     */
    @Column(name = "request_id", nullable = false)
    private Long requestId;

    /**
     * Número sequencial do RPS gerado
     */
    @Column(nullable = false)
    private String numero;

    /**
     * Série do RPS (por exemplo, ano ou outro identificador)
     */
    @Column(nullable = false)
    private String serie;

    /**
     * Tipo do RPS (conforme manual da NFSe)
     */
    @Column(nullable = false)
    private Integer tipo;

    /**
     * Data e hora de emissão do RPS
     */
    @Column(name = "data_emissao", nullable = false)
    private LocalDateTime dataEmissao;

    /**
     * Natureza da operação
     */
    @Column(name = "natureza_operacao", nullable = false)
    private Integer naturezaOperacao;

    /**
     * Regime especial de tributação
     */
    @Column(name = "regime_especial_tributacao", nullable = false)
    private Integer regimeEspecialTributacao;

    /**
     * Indicador de optante pelo Simples Nacional
     */
    @Column(name = "optante_simples_nacional", nullable = false)
    private Integer optanteSimplesNacional;

    /**
     * Indicador de incentivador cultural
     */
    @Column(name = "incentivador_cultural", nullable = false)
    private Integer incentivadorCultural;

    /**
     * Status do RPS
     */
    @Column(nullable = false)
    private Integer status;

    /**
     * Valor dos serviços
     */
    @Column(name = "valor_servicos", nullable = false, precision = 13, scale = 2)
    private BigDecimal valorServicos;

    // Construtor padrão
    public RpsEntity() {
    }

    // Construtor completo
    public RpsEntity(Long requestId,
                     String numero,
                     String serie,
                     Integer tipo,
                     LocalDateTime dataEmissao,
                     Integer naturezaOperacao,
                     Integer regimeEspecialTributacao,
                     Integer optanteSimplesNacional,
                     Integer incentivadorCultural,
                     Integer status,
                     BigDecimal valorServicos) {
        this.requestId = requestId;
        this.numero = numero;
        this.serie = serie;
        this.tipo = tipo;
        this.dataEmissao = dataEmissao;
        this.naturezaOperacao = naturezaOperacao;
        this.regimeEspecialTributacao = regimeEspecialTributacao;
        this.optanteSimplesNacional = optanteSimplesNacional;
        this.incentivadorCultural = incentivadorCultural;
        this.status = status;
        this.valorServicos = valorServicos;
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public Integer getTipo() {
        return tipo;
    }

    public void setTipo(Integer tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(LocalDateTime dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public Integer getNaturezaOperacao() {
        return naturezaOperacao;
    }

    public void setNaturezaOperacao(Integer naturezaOperacao) {
        this.naturezaOperacao = naturezaOperacao;
    }

    public Integer getRegimeEspecialTributacao() {
        return regimeEspecialTributacao;
    }

    public void setRegimeEspecialTributacao(Integer regimeEspecialTributacao) {
        this.regimeEspecialTributacao = regimeEspecialTributacao;
    }

    public Integer getOptanteSimplesNacional() {
        return optanteSimplesNacional;
    }

    public void setOptanteSimplesNacional(Integer optanteSimplesNacional) {
        this.optanteSimplesNacional = optanteSimplesNacional;
    }

    public Integer getIncentivadorCultural() {
        return incentivadorCultural;
    }

    public void setIncentivadorCultural(Integer incentivadorCultural) {
        this.incentivadorCultural = incentivadorCultural;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public BigDecimal getValorServicos() {
        return valorServicos;
    }

    public void setValorServicos(BigDecimal valorServicos) {
        this.valorServicos = valorServicos;
    }
}
