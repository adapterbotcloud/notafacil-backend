package com.notafacil.dto;

public record ServicoDto(
    ValoresDto valores,
    String itemListaServico,
    String codigoTributacaoMunicipio,
    String discriminacao,
    String codigoMunicipio
) {}