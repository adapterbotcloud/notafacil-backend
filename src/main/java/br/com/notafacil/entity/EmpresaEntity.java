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

}
