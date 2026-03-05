package br.com.notafacil.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record RpsMinRequestDto(
        @NotNull Long id,
        Long idCobranca, // ID da cobrança na planilha (para deduplicação)
        @NotNull @Valid ServicoMin servico,
        @NotNull @Valid TomadorMin tomador
) {

    public record ServicoMin(
            @NotNull @Positive BigDecimal valorServicos,
            @NotNull @Size(min = 1, max = 4000) String discriminacao
    ) {}

    public record TomadorMin(
            @NotNull @Size(min = 11, max = 14) String cpf,
            @NotNull @Size(min = 1, max = 255) String razaoSocial
    ) {}
}
