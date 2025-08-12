package br.com.notafacil.dto;

import br.com.notafacil.dto.prestador.PrestadorDto;
import br.com.notafacil.dto.tomador.TomadorDto;

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