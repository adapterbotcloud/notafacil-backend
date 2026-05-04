package br.com.notafacil.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Classificação Tributária completa (6 dígitos, ex: 410003).
 * Define o tratamento fiscal IBS/CBS do serviço.
 */
@Entity
@Table(name = "classificacao_tributaria",
        uniqueConstraints = @UniqueConstraint(name = "uk_classtrib_codigo", columnNames = "codigo"),
        indexes = @Index(name = "idx_classtrib_codigo", columnList = "codigo")
)
@Getter
@Setter
public class ClassificacaoTributariaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código de classificação tributária (6 dígitos, ex: 410003) */
    @Column(name = "codigo", nullable = false, length = 6, unique = true)
    private String codigo;

    /** Descrição da classificação */
    @Column(name = "descricao", nullable = false, length = 500)
    private String descricao;

    /** Grupo principal (4xx = serviços gerais, 5xx = construção civil, etc.) */
    @Column(name = "grupo", length = 50)
    private String grupo;

    @Column(name = "habilitado", nullable = false)
    private Boolean habilitado = true;
}
