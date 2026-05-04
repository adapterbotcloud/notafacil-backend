package br.com.notafacil.provider.model;

public record ProviderResponse(
        boolean success,
        String protocolo,
        String rawResponse,
        String errorMessage
) {
    public static ProviderResponse success(String protocolo, String rawResponse) {
        return new ProviderResponse(true, protocolo, rawResponse, null);
    }

    public static ProviderResponse failure(String errorMessage, String rawResponse) {
        return new ProviderResponse(false, null, rawResponse, errorMessage);
    }
}
