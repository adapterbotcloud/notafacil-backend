package com.notafacil.dto;

public record EnderecoDto(
    String endereco,
    String numero,
    String complemento,
    String bairro,
    String codigoMunicipio,
    String uf,
    String cep
) {}