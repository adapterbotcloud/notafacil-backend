package br.com.notafacil.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Indicador de Operação IBS/CBS (6 dígitos, ex: 030102).
 * Combinação: tipo operação (2) + subtipo (2) + detalhe (2).
 */
@Entity
@Table(name = "indicador_operacao",
        uniqueConstraints = @UniqueConstraint(name = "uk_indop_codigo", columnNames = "codigo"),
        indexes = @Index(name = "idx_indop_codigo", columnList = "codigo")
)
@Getter
@Setter
public class IndicadorOperacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código do indicador de operação (6 dígitos, ex: 030102) */
    @Column(name = "codigo", nullable = false, length = 6, unique = true)
    private String codigo;

    /** Descrição do indicador */
    @Column(name = "descricao", nullable = false, length = 500)
    private String descricao;

    @Column(name = "habilitado", nullable = false)
    private Boolean habilitado = true;
}
