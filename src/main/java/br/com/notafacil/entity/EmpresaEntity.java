package br.com.notafacil.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "empresa",
        uniqueConstraints = @UniqueConstraint(name = "uk_empresa_cnpj", columnNames = "cnpj"),
        indexes = @Index(name = "idx_empresa_cnpj", columnList = "cnpj")
)
public class EmpresaEntity {

        @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Getter
    @NotBlank
    @Size(min = 14, max = 14)
    @Column(nullable = false, length = 14)
    private String cnpj;

    @Setter
    @Getter
    @NotBlank
    @Size(max = 20)
    @Column(name = "inscricao_municipal", nullable = false, length = 20)
    private String inscricaoMunicipal;

    @Setter
    @Getter
    @NotNull
    @Column(name = "regime_especial_tributacao", nullable = false)
    private Integer regimeEspecialTributacao;

    @Setter
    @Getter
    @NotNull
    @Column(name = "optante_simples_nacional", nullable = false)
    private Integer optanteSimplesNacional;

    @Setter
    @Getter
    @NotNull
    @Column(name = "incentivador_cultural", nullable = false)
    private Integer incentivadorCultural;

    @Setter
    @Getter
    @NotBlank
    @Size(max = 10)
    @Column(name = "item_lista_servico", nullable = false, length = 10)
    private String itemListaServico;

    @Setter
    @Getter
    @NotBlank
    @Size(max = 20)
    @Column(name = "codigo_tributacao_municipio", nullable = false, length = 20)
    private String codigoTributacaoMunicipio;

    @Setter
    @Getter
    @NotBlank
    @Size(max = 7)
    @Column(name = "codigo_municipio", nullable = false, length = 7)
    private String codigoMunicipio;

    @Getter
    @Setter
    @NotNull
    @Digits(integer = 4, fraction = 6)
    @Column(name = "aliquota", nullable = false, precision = 10, scale = 6)
    private BigDecimal aliquota;

    @Getter @Setter
    @Column(name = "razao_social", length = 200)
    private String razaoSocial;

    @Getter @Setter
    @Column(name = "endereco", length = 200)
    private String endereco;

    @Getter @Setter
    @Column(name = "numero", length = 20)
    private String numero;

    @Getter @Setter
    @Column(name = "complemento", length = 100)
    private String complemento;

    @Getter @Setter
    @Column(name = "bairro", length = 100)
    private String bairro;

    @Getter @Setter
    @Column(name = "cep", length = 10)
    private String cep;

    @Getter @Setter
    @Column(name = "telefone", length = 20)
    private String telefone;

    @Getter @Setter
    @Column(name = "substituto_tributario")
    private Boolean substitutoTributario = false;

    // ─── Campos Obrigatórios v4 (com FK para tabelas de referência) ─────────────

    /** NBS - Nomenclatura Brasileira de Serviços (FK → nbs.codigo) */
    @Setter @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nbs", referencedColumnName = "codigo", nullable = false,
            foreignKey = @ForeignKey(name = "fk_empresa_nbs"))
    private NbsEntity nbsRef;

    /** Indicador de Operação (FK → indicador_operacao.codigo) */
    @Setter @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicador_operacao", referencedColumnName = "codigo", nullable = false,
            foreignKey = @ForeignKey(name = "fk_empresa_indicador_operacao"))
    private IndicadorOperacaoEntity indicadorOperacaoRef;

    /** CST - Código Situação Tributária (FK → cst_ibs_cbs.codigo) */
    @Setter @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cst", referencedColumnName = "codigo", nullable = false,
            foreignKey = @ForeignKey(name = "fk_empresa_cst"))
    private CstIbsCbsEntity cstRef;

    /** Classificação Tributária (FK → classificacao_tributaria.codigo) */
    @Setter @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classificacao_tributaria", referencedColumnName = "codigo", nullable = false,
            foreignKey = @ForeignKey(name = "fk_empresa_classificacao_tributaria"))
    private ClassificacaoTributariaEntity classificacaoTributariaRef;

    // ─── Convenience getters para manter compatibilidade com código existente ──

    public String getNbs() { return nbsRef != null ? nbsRef.getCodigo() : null; }
    public String getIndicadorOperacao() { return indicadorOperacaoRef != null ? indicadorOperacaoRef.getCodigo() : null; }
    public String getCst() { return cstRef != null ? cstRef.getCodigo() : null; }
    public String getClassificacaoTributaria() { return classificacaoTributariaRef != null ? classificacaoTributariaRef.getCodigo() : null; }

    // ─── Campos IBS/CBS (Reforma Tributária LC 214/2025) ───────────────────────

    /** Regime tributário preferido: ISS (padrão) ou IBS_CBS */
    @Getter @Setter
    @Column(name = "regime_tributario_preferido", length = 10)
    private String regimeTributarioPreferido = "ISS";

    /** Flag para habilitar IBS/CBS nesta empresa */
    @Getter @Setter
    @Column(name = "ibs_cbs_habilitado")
    private Boolean ibsCbsHabilitado = false;

}
