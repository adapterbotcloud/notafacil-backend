package br.com.notafacil.dto;

import java.util.List;

public record EmitirRpsResponse(
        List<Long> rpsIds,
        List<String> protocolos // pode ter 1 por lote
) {}
