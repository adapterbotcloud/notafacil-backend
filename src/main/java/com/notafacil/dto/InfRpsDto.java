package com.notafacil.dto;

import java.time.LocalDateTime;

public record InfRpsDto(
    IdentificacaoRpsDto identificacaoRps,
    LocalDateTime dataEmissao,
    Integer naturezaOperacao,
    Integer regimeEspecialTributacao,
    Integer optanteSimplesNacional,
    Integer incentivadorCultural,
    Integer status,
    ServicoDto servico,
    PrestadorDto prestador,
    TomadorDto tomador
) {}