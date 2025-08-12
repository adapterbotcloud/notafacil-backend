package br.com.notafacil.dto;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record EmitirNotaRequest(
        @NotEmpty @Valid
        List<InfRpsRequestDto> listaRps
) { }
