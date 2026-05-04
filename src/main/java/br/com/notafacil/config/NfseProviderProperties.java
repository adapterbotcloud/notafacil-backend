package br.com.notafacil.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "nfse")
public class NfseProviderProperties {

    private LoteConfig lote = new LoteConfig();
    private Map<String, ProviderConfig> providers = new HashMap<>();
    private Map<String, String> municipioMapping = new HashMap<>();
    private Map<String, String> ufDefaults = new HashMap<>();
    private FeaturesConfig features = new FeaturesConfig();

    // --- Nested classes ---

    public static class LoteConfig {
        private int tamanho = 50;

        public int getTamanho() { return tamanho; }
        public void setTamanho(int tamanho) { this.tamanho = tamanho; }
    }

    public static class ProviderConfig {
        private boolean enabled = false;
        private String wsdlUrl;
        private String baseUrl;
        private String versaoSchema = "3";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getWsdlUrl() { return wsdlUrl; }
        public void setWsdlUrl(String wsdlUrl) { this.wsdlUrl = wsdlUrl; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getVersaoSchema() { return versaoSchema; }
        public void setVersaoSchema(String versaoSchema) { this.versaoSchema = versaoSchema; }
    }

    public static class FeaturesConfig {
        private boolean ibsCbsEnabled = false;
        private boolean v4SchemaEnabled = false;

        public boolean isIbsCbsEnabled() { return ibsCbsEnabled; }
        public void setIbsCbsEnabled(boolean ibsCbsEnabled) { this.ibsCbsEnabled = ibsCbsEnabled; }
        public boolean isV4SchemaEnabled() { return v4SchemaEnabled; }
        public void setV4SchemaEnabled(boolean v4SchemaEnabled) { this.v4SchemaEnabled = v4SchemaEnabled; }
    }

    // --- Getters/Setters ---

    public LoteConfig getLote() { return lote; }
    public void setLote(LoteConfig lote) { this.lote = lote; }
    public Map<String, ProviderConfig> getProviders() { return providers; }
    public void setProviders(Map<String, ProviderConfig> providers) { this.providers = providers; }
    public Map<String, String> getMunicipioMapping() { return municipioMapping; }
    public void setMunicipioMapping(Map<String, String> municipioMapping) { this.municipioMapping = municipioMapping; }
    public Map<String, String> getUfDefaults() { return ufDefaults; }
    public void setUfDefaults(Map<String, String> ufDefaults) { this.ufDefaults = ufDefaults; }
    public FeaturesConfig getFeatures() { return features; }
    public void setFeatures(FeaturesConfig features) { this.features = features; }
}
