package br.com.notafacil.provider.model;

import java.util.List;

public record ConsultaLoteResponse(
        boolean success,
        List<NfseInfo> nfseList,
        String rawResponse,
        String errorMessage
) {
    public record NfseInfo(
            String numeroNfse,
            String codigoVerificacao,
            String numeroRps,
            String serieRps
    ) {}
}
