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
        public static final int FALHA    = 3; // falha no envio ou processamento
        public static final int PROCESSADO = 4; // processado com sucesso pela SEFIN // falha no cálculo/mapeamento/envio
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

    /** ID da cobrança original (da planilha) - chave de deduplicação */
    @Column(name = "id_cobranca", nullable = true)
    private Long idCobranca;

    /** Mês da cobrança (da planilha) */
    @Column(name = "mes_cobranca", nullable = true)
    private Integer mesCobranca;

    /** Ano da cobrança (da planilha) */
    @Column(name = "ano_cobranca", nullable = true)
    private Integer anoCobranca;

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

    @Column(name = "tomador_cpf", length = 14)
    private String tomadorCpf;

    @Column(name = "tomador_razao_social", length = 115)
    private String tomadorRazaoSocial;

    @Column(name = "discriminacao", length = 2000)
    private String discriminacao;

    /** ID do provider que processou este RPS (GINFES, XTR_SA, PADRAO_NACIONAL) */
    @Column(name = "provider_id", length = 50)
    private String providerId;

    /** Versão do schema usada no envio (3, 4, etc.) */
    @Column(name = "versao_schema", length = 5)
    private String versaoSchema;

    /** Número de tentativas de envio */
    @Column(name = "tentativas_envio")
    private Integer tentativasEnvio = 0;

    /** Número da NFS-e retornado após processamento */
    @Column(name = "numero_nfse", length = 30)
    private String numeroNfse;

    /** Código de verificação da NFS-e */
    @Column(name = "codigo_verificacao", length = 50)
    private String codigoVerificacao;

    /** Regime tributário: ISS ou IBS_CBS */
    @Column(name = "regime_tributario", length = 10)
    private String regimeTributario;

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

    public String getTomadorCpf() { return tomadorCpf; }
    public void setTomadorCpf(String tomadorCpf) { this.tomadorCpf = tomadorCpf; }
    public String getTomadorRazaoSocial() { return tomadorRazaoSocial; }
    public void setTomadorRazaoSocial(String v) { this.tomadorRazaoSocial = v; }
    public String getDiscriminacao() { return discriminacao; }
    public void setDiscriminacao(String discriminacao) { this.discriminacao = discriminacao; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public Long getIdCobranca() { return idCobranca; }
    public void setIdCobranca(Long idCobranca) { this.idCobranca = idCobranca; }
    public Integer getMesCobranca() { return mesCobranca; }
    public void setMesCobranca(Integer mesCobranca) { this.mesCobranca = mesCobranca; }
    public Integer getAnoCobranca() { return anoCobranca; }
    public void setAnoCobranca(Integer anoCobranca) { this.anoCobranca = anoCobranca; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public String getVersaoSchema() { return versaoSchema; }
    public void setVersaoSchema(String versaoSchema) { this.versaoSchema = versaoSchema; }
    public Integer getTentativasEnvio() { return tentativasEnvio; }
    public void setTentativasEnvio(Integer tentativasEnvio) { this.tentativasEnvio = tentativasEnvio; }
    public String getNumeroNfse() { return numeroNfse; }
    public void setNumeroNfse(String numeroNfse) { this.numeroNfse = numeroNfse; }
    public String getCodigoVerificacao() { return codigoVerificacao; }
    public void setCodigoVerificacao(String codigoVerificacao) { this.codigoVerificacao = codigoVerificacao; }
    public String getRegimeTributario() { return regimeTributario; }
    public void setRegimeTributario(String regimeTributario) { this.regimeTributario = regimeTributario; }
}