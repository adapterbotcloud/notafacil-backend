package br.com.notafacil.provider;

public interface ProviderResolver {

    /**
     * Resolve o provider adequado para o município dado (código IBGE 7 dígitos).
     */
    NfseProvider resolve(String codigoMunicipio);

    /**
     * Resolve o provider pelo seu ID (GINFES, XTR_SA, PADRAO_NACIONAL).
     */
    NfseProvider resolveById(String providerId);
}
