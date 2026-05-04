package br.com.notafacil.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Nomenclatura Brasileira de Serviços (NBS) - 9 dígitos.
 * Tabela de referência para validação e autocompletar.
 */
@Entity
@Table(name = "nbs",
        uniqueConstraints = @UniqueConstraint(name = "uk_nbs_codigo", columnNames = "codigo"),
        indexes = @Index(name = "idx_nbs_codigo", columnList = "codigo")
)
@Getter
@Setter
public class NbsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código NBS (9 dígitos, ex: 108011100) */
    @Column(name = "codigo", nullable = false, length = 9, unique = true)
    private String codigo;

    /** Descrição do serviço */
    @Column(name = "descricao", nullable = false, length = 500)
    private String descricao;

    /** Capítulo/Seção para agrupamento */
    @Column(name = "secao", length = 50)
    private String secao;

    @Column(name = "habilitado", nullable = false)
    private Boolean habilitado = true;
}
