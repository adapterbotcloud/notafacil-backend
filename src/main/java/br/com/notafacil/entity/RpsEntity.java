package br.com.notafacil.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * Entidade que representa um RPS emitido.
 */
@Entity
@Table(
        name = "rps",
        indexes = {
                @Index(name = "idx_rps_status", columnList = "status")
        }
)
public class RpsEntity {

    // Convenção de status como inteiro (rápido para index/lookup)
    public static final class Status {
        public static final int PENDENTE = 0; // persistido e aguardando envio
        public static final int ENVIANDO = 1; // em processamento assíncrono
        public static final int ENVIADO  = 2; // lote enviado com sucesso
        public static final int FALHA    = 3; // falha no cálculo/mapeamento/envio
        private Status() {}
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_rps_empresa"))
    private EmpresaEntity empresa;

    // getters/setters
    public EmpresaEntity getEmpresa() { return empresa; }
    public void setEmpresa(EmpresaEntity empresa) { this.empresa = empresa; }

    /** ID de negócio enviado pelo cliente para associação */
    @Column(name = "request_id", nullable = false)
    private Long requestId;

    /** Número sequencial do RPS gerado */
    @Column(nullable = false, length = 30)
    private String numero;

    /** Série do RPS (por exemplo, ano ou outro identificador) */
    @Column(nullable = false, length = 10)
    private String serie;

    /** Tipo do RPS (conforme manual da NFSe) */
    @Column(nullable = false)
    private Integer tipo;

    /** Data e hora de emissão do RPS */
    @Column(name = "data_emissao", nullable = false)
    private LocalDateTime dataEmissao;

    /** Natureza da operação (fixo = 1) */
    @Column(name = "natureza_operacao", nullable = false)
    private Integer naturezaOperacao;

    /** Regime especial de tributação */
    @Column(name = "regime_especial_tributacao", nullable = false)
    private Integer regimeEspecialTributacao;

    /** Indicador de optante pelo Simples Nacional */
    @Column(name = "optante_simples_nacional", nullable = false)
    private Integer optanteSimplesNacional;

    /** Indicador de incentivador cultural */
    @Column(name = "incentivador_cultural", nullable = false)
    private Integer incentivadorCultural;

    /** Status do RPS (inteiro para performance) */
    @Column(nullable = false)
    private Integer status;

    /** Valor dos serviços */
    @Column(name = "valor_servicos", nullable = false, precision = 13, scale = 2)
    private BigDecimal valorServicos;

    /** Protocolo retornado pela Prefeitura para acompanhar processamento do lote */
    @Column(name = "protocolo", length = 60)
    private String protocolo;

    /** Mensagem de erro do processamento/envio */
    @Column(name = "mensagem_erro", length = 2000)
    private String mensagemErro;

    /** Auditoria simples */
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public RpsEntity() {
    }

    // Construtor completo (mantido e extendido)
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

    // Defaults/atualizações automáticas
    @PrePersist
    public void prePersist() {
        var now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        // defaults seguros
        if (this.dataEmissao == null) {
            this.dataEmissao = LocalDateTime.now();
        }
        if (this.naturezaOperacao == null) {
            this.naturezaOperacao = 1; // conforme seu requisito
        }
        if (this.serie == null) {
            this.serie = "A";
        }
        if (this.tipo == null) {
            this.tipo = 1;
        }
        if (this.status == null) {
            this.status = Status.PENDENTE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    // Getters e Setters

    public Long getId() { return id; }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }

    public Integer getTipo() { return tipo; }
    public void setTipo(Integer tipo) { this.tipo = tipo; }

    public LocalDateTime getDataEmissao() { return dataEmissao; }
    public void setDataEmissao(LocalDateTime dataEmissao) { this.dataEmissao = dataEmissao; }

    public Integer getNaturezaOperacao() { return naturezaOperacao; }
    public void setNaturezaOperacao(Integer naturezaOperacao) { this.naturezaOperacao = naturezaOperacao; }

    public Integer getRegimeEspecialTributacao() { return regimeEspecialTributacao; }
    public void setRegimeEspecialTributacao(Integer regimeEspecialTributacao) { this.regimeEspecialTributacao = regimeEspecialTributacao; }

    public Integer getOptanteSimplesNacional() { return optanteSimplesNacional; }
    public void setOptanteSimplesNacional(Integer optanteSimplesNacional) { this.optanteSimplesNacional = optanteSimplesNacional; }

    public Integer getIncentivadorCultural() { return incentivadorCultural; }
    public void setIncentivadorCultural(Integer incentivadorCultural) { this.incentivadorCultural = incentivadorCultural; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public BigDecimal getValorServicos() { return valorServicos; }
    public void setValorServicos(BigDecimal valorServicos) { this.valorServicos = valorServicos; }

    public String getProtocolo() { return protocolo; }
    public void setProtocolo(String protocolo) { this.protocolo = protocolo; }

    public String getMensagemErro() { return mensagemErro; }
    public void setMensagemErro(String mensagemErro) { this.mensagemErro = mensagemErro; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
