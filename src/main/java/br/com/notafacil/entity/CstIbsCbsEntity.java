package br.com.notafacil.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Código de Situação Tributária IBS/CBS (3 dígitos).
 * Ex: 101 = Tributação integral, 102 = Redução de alíquota, 201 = Imunidade, 900 = Outros.
 */
@Entity
@Table(name = "cst_ibs_cbs",
        uniqueConstraints = @UniqueConstraint(name = "uk_cst_codigo", columnNames = "codigo"),
        indexes = @Index(name = "idx_cst_codigo", columnList = "codigo")
)
@Getter
@Setter
public class CstIbsCbsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código CST (3 dígitos, ex: 101) */
    @Column(name = "codigo", nullable = false, length = 3, unique = true)
    private String codigo;

    /** Descrição do CST */
    @Column(name = "descricao", nullable = false, length = 300)
    private String descricao;

    /** Grupo: TRIBUTADO, ISENTO, IMUNE, OUTROS */
    @Column(name = "grupo", length = 30)
    private String grupo;

    @Column(name = "habilitado", nullable = false)
    private Boolean habilitado = true;
}
