package br.com.notafacil.provider.model;

public record CancelamentoResponse(
        boolean success,
        String rawResponse,
        String errorMessage
) {}
