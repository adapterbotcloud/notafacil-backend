package br.com.notafacil.dto;

public record TomadorDto(
    IdentificacaoTomadorDto identificacaoTomador,
    String razaoSocial,
    EnderecoDto endereco,
    ContatoDto contato
) {}