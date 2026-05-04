package br.com.notafacil.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "municipio_provedor",
        uniqueConstraints = @UniqueConstraint(name = "uk_municipio_codigo", columnNames = "codigo_municipio"),
        indexes = @Index(name = "idx_municipio_codigo", columnList = "codigo_municipio")
)
@Getter
@Setter
public class MunicipioProvedorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_municipio", nullable = false, length = 7, unique = true)
    private String codigoMunicipio;

    @Column(name = "nome_municipio", length = 200)
    private String nomeMunicipio;

    @Column(name = "uf", length = 2)
    private String uf;

    /** GINFES, XTR_SA, PADRAO_NACIONAL */
    @Column(name = "provider_id", nullable = false, length = 50)
    private String providerId;

    /** Versão do schema: "3" ou "4" */
    @Column(name = "versao_schema", length = 5)
    private String versaoSchema = "3";

    /** Override de endpoint específico para este município */
    @Column(name = "endpoint_url", length = 500)
    private String endpointUrl;

    /** Liga/desliga mapeamento */
    @Column(name = "habilitado", nullable = false)
    private Boolean habilitado = true;
}
