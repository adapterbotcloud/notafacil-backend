package br.com.notafacil.provider;

import br.com.notafacil.provider.model.*;

public interface NfseProvider {

    String getProviderId();

    boolean supportsIbsCbs();

    ProtocolType getProtocolType();

    ProviderResponse enviarLote(EnviarLoteRequest request);

    ConsultaSituacaoResponse consultarSituacaoLote(ConsultaSituacaoRequest request);

    ConsultaLoteResponse consultarLote(ConsultaLoteRequest request);

    CancelamentoResponse cancelarNfse(CancelamentoRequest request);
}
