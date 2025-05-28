package br.com.notafacil.dto;

import java.math.BigDecimal;

public record ValoresDto(
    BigDecimal valorServicos,
    BigDecimal valorDeducoes,
    BigDecimal valorPis,
    BigDecimal valorCofins,
    BigDecimal valorInss,
    BigDecimal valorIr,
    BigDecimal valorCsll,
    Integer issRetido,
    BigDecimal valorIss,
    BigDecimal valorIssRetido,
    BigDecimal outrasRetencoes,
    BigDecimal baseCalculo,
    BigDecimal aliquota,
    BigDecimal valorLiquidoNfse,
    BigDecimal descontoIncondicionado,
    BigDecimal descontoCondicionado
) {}