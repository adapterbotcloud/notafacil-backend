package br.com.notafacil.dto;

public record IdentificacaoTomadorDto(
    String cpf,
    String cnpj,
    String inscricaoMunicipal
) {}