package br.com.notafacil.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Tabela de correlação entre Item da Lista de Serviços (LC 116/2003)
 * e os códigos da reforma tributária (NBS, Indicador de Operação, Classificação Tributária).
 *
 * Permite derivar automaticamente os campos V4 a partir do item_lista_servico da empresa.
 */
@Entity
@Table(name = "correlacao_lc116_nbs",
        indexes = {
                @Index(name = "idx_corr_item_lista", columnList = "item_lista_servico"),
                @Index(name = "idx_corr_nbs", columnList = "nbs_codigo")
        }
)
@Getter
@Setter
public class CorrelacaoLc116NbsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Item da Lista de Serviços LC 116 (ex: 01.01, 17.05) */
    @Column(name = "item_lista_servico", nullable = false, length = 10)
    private String itemListaServico;

    /** Descrição do item LC 116 */
    @Column(name = "descricao_lc116", length = 500)
    private String descricaoLc116;

    /** Código NBS correspondente (9 dígitos) */
    @Column(name = "nbs_codigo", nullable = false, length = 9)
    private String nbsCodigo;

    /** Indicador de Operação padrão (6 dígitos) */
    @Column(name = "indicador_operacao_codigo", length = 6)
    private String indicadorOperacaoCodigo;

    /** Classificação Tributária padrão (6 dígitos) */
    @Column(name = "classificacao_tributaria_codigo", length = 6)
    private String classificacaoTributariaCodigo;

    /** CST padrão para esta correlação (3 dígitos) */
    @Column(name = "cst_padrao", length = 3)
    private String cstPadrao;

    @Column(name = "habilitado", nullable = false)
    private Boolean habilitado = true;
}
