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

    // ─── Campos Obrigatórios v4 ────────────────────────────────────────────────

    /** NBS - Nomenclatura Brasileira de Serviços (9 dígitos) */
    @Setter @Getter
    @NotBlank
    @Size(max = 9)
    @Column(name = "nbs", nullable = false, length = 9)
    private String nbs;

    /** Indicador de Operação - código de 6 dígitos (ex: 030102) */
    @Setter @Getter
    @NotBlank
    @Size(max = 6)
    @Column(name = "indicador_operacao", nullable = false, length = 6)
    private String indicadorOperacao;

    /** CST - Código Situação Tributária (3 dígitos, ex: 101) */
    @Setter @Getter
    @NotBlank
    @Size(max = 3)
    @Column(name = "cst", nullable = false, length = 3)
    private String cst;

    /** Classificação Tributária completa (6 dígitos, ex: 410003) */
    @Setter @Getter
    @NotBlank
    @Size(max = 6)
    @Column(name = "classificacao_tributaria", nullable = false, length = 6)
    private String classificacaoTributaria;

}
