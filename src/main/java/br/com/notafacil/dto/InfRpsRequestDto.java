package br.com.notafacil.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.math.BigDecimal;

import java.time.LocalDateTime;

public record InfRpsRequestDto(
        @NotNull Long id,
        @NotNull Integer naturezaOperacao,
        @NotNull Integer regimeEspecialTributacao,
        @NotNull Integer optanteSimplesNacional,
        @NotNull Integer incentivadorCultural,
        @NotNull Integer status,
        @NotNull ServicoRequest servico,
        @NotNull PrestadorRequest prestador,
        @NotNull TomadorRequest tomador,
        LocalDateTime dataEmissao
) {
    public record ServicoRequest(
            @NotNull ValoresRequest valores,
            @NotNull String itemListaServico,
            @NotNull String codigoTributacaoMunicipio,
            @NotNull String discriminacao,
            @NotNull String codigoMunicipio
    ) {
        public record ValoresRequest(
                @NotNull BigDecimal valorServicos,
                @NotNull BigDecimal valorDeducoes,
                @NotNull BigDecimal valorPis,
                @NotNull BigDecimal valorCofins,
                @NotNull BigDecimal valorInss,
                @NotNull BigDecimal valorIr,
                @NotNull BigDecimal valorCsll,
                @NotNull Integer issRetido,
                @NotNull BigDecimal valorIss,
                @NotNull BigDecimal valorIssRetido,
                @NotNull BigDecimal outrasRetencoes,
                @NotNull BigDecimal baseCalculo,
                @NotNull BigDecimal aliquota,
                @NotNull BigDecimal valorLiquidoNfse,
                @NotNull BigDecimal descontoIncondicionado,
                @NotNull BigDecimal descontoCondicionado
        ) { }
    }

    public record PrestadorRequest(
            @NotNull String cnpj,
            @NotNull String inscricaoMunicipal
    ) { }

    public record TomadorRequest(
            @NotNull IdentificacaoTomadorRequest identificacaoTomador,
            @NotNull String razaoSocial,
            @Valid EnderecoRequest endereco,
            @Null ContatoRequest contato
    ) {
        public record IdentificacaoTomadorRequest(
                @NotNull String cpf,
                @Null String inscricaoMunicipal
        ) { }

        public record EnderecoRequest(
                @NotNull String endereco,
                @NotNull String numero,
                @NotNull String complemento,
                @NotNull String bairro,
                @NotNull String codigoMunicipio,
                @NotNull String uf,
                @NotNull String cep
        ) { }

        public record ContatoRequest(
                @NotNull String telefone,
                @NotNull String email
        ) { }
    }
}
