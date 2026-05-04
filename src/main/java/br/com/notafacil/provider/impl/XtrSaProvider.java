package br.com.notafacil.provider.impl;

import br.com.notafacil.config.NfseProviderProperties;
import br.com.notafacil.provider.NfseProvider;
import br.com.notafacil.provider.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Provider para o sistema XTR/SA da SEFIN-CE.
 * Stub — será implementado na Fase 2.
 */
@Component
@ConditionalOnProperty(name = "nfse.providers.xtr-sa.enabled", havingValue = "true")
public class XtrSaProvider implements NfseProvider {

    private static final Logger log = LoggerFactory.getLogger(XtrSaProvider.class);

    public XtrSaProvider(NfseProviderProperties properties) {
        log.info("XtrSaProvider inicializado (stub)");
    }

    @Override
    public String getProviderId() {
        return "XTR_SA";
    }

    @Override
    public boolean supportsIbsCbs() {
        return false; // TBD na Fase 2
    }

    @Override
    public ProtocolType getProtocolType() {
        return ProtocolType.REST;
    }

    @Override
    public ProviderResponse enviarLote(EnviarLoteRequest request) {
        return ProviderResponse.failure("XTR_SA provider ainda não implementado (Fase 2)", null);
    }

    @Override
    public ConsultaSituacaoResponse consultarSituacaoLote(ConsultaSituacaoRequest request) {
        return new ConsultaSituacaoResponse(-1, "XTR_SA provider ainda não implementado (Fase 2)", null);
    }

    @Override
    public ConsultaLoteResponse consultarLote(ConsultaLoteRequest request) {
        return new ConsultaLoteResponse(false, java.util.List.of(), null, "XTR_SA provider ainda não implementado (Fase 2)");
    }

    @Override
    public CancelamentoResponse cancelarNfse(CancelamentoRequest request) {
        return new CancelamentoResponse(false, null, "XTR_SA provider ainda não implementado (Fase 2)");
    }
}
