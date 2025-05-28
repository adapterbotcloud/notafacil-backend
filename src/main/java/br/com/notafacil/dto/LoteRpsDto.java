package br.com.notafacil.dto;

import java.util.List;

public record LoteRpsDto(
    Long id,
    String numeroLote,
    String cnpj,
    String inscricaoMunicipal,
    Integer quantidadeRps,
    List<RpsDto> listaRps
) {}