package br.com.notafacil.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "parametros_rps")
public class ParametrosRps {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal aliquota;

    @Column(name = "item_lista_servico", nullable = false, length = 10)
    private String itemListaServico;

    @Column(name = "codigo_tributacao_municipio", length = 20)
    private String codigoTributacaoMunicipio;

    @Column(name = "codigo_municipio", nullable = false, length = 10)
    private String codigoMunicipio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tomador_id", nullable = false)
    private Tomador tomador;
}
