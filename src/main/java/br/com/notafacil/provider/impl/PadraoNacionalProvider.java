package br.com.notafacil.provider.impl;

import br.com.notafacil.config.NfseProviderProperties;
import br.com.notafacil.provider.NfseProvider;
import br.com.notafacil.provider.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Provider para o Padrão Nacional NFS-e (ABRASF v2.04).
 * Stub — será implementado na Fase 3.
 */
@Component
@ConditionalOnProperty(name = "nfse.providers.padrao-nacional.enabled", havingValue = "true")
public class PadraoNacionalProvider implements NfseProvider {

    private static final Logger log = LoggerFactory.getLogger(PadraoNacionalProvider.class);

    public PadraoNacionalProvider(NfseProviderProperties properties) {
        log.info("PadraoNacionalProvider inicializado (stub)");
    }

    @Override
    public String getProviderId() {
        return "PADRAO_NACIONAL";
    }

    @Override
    public boolean supportsIbsCbs() {
        return true; // Padrão Nacional suportará IBS/CBS
    }

    @Override
    public ProtocolType getProtocolType() {
        return ProtocolType.REST;
    }

    @Override
    public ProviderResponse enviarLote(EnviarLoteRequest request) {
        return ProviderResponse.failure("PADRAO_NACIONAL provider ainda não implementado (Fase 3)", null);
    }

    @Override
    public ConsultaSituacaoResponse consultarSituacaoLote(ConsultaSituacaoRequest request) {
        return new ConsultaSituacaoResponse(-1, "PADRAO_NACIONAL provider ainda não implementado (Fase 3)", null);
    }

    @Override
    public ConsultaLoteResponse consultarLote(ConsultaLoteRequest request) {
        return new ConsultaLoteResponse(false, java.util.List.of(), null, "PADRAO_NACIONAL provider ainda não implementado (Fase 3)");
    }

    @Override
    public CancelamentoResponse cancelarNfse(CancelamentoRequest request) {
        return new CancelamentoResponse(false, null, "PADRAO_NACIONAL provider ainda não implementado (Fase 3)");
    }
}
