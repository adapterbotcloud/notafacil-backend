package br.com.notafacil.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "empresa")
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String cnpj;

    @Column(name = "inscricao_municipal", length = 30)
    private String inscricaoMunicipal;

    @Column(name = "optante_simples_nacional", nullable = false)
    private boolean optanteSimplesNacional;

    @Column(name = "incentivador_cultural", nullable = false)
    private boolean incentivadorCultural;

    @Column(name = "natureza_operacao", length = 50)
    private String naturezaOperacao;

    @Column(name = "regime_especial_tributacao", length = 50)
    private String regimeEspecialTributacao;
}
