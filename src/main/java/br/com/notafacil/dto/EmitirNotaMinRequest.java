package br.com.notafacil.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record EmitirNotaMinRequest(
        @NotEmpty @Valid List<RpsMinRequestDto> listaRps
) { }
