package br.com.notafacil.dto;

public record IdentificacaoRpsDto(
    String numero,
    String serie,
    Integer tipo
) {}