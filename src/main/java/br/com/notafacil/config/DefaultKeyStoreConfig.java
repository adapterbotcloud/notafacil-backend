package br.com.notafacil.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyStore;

@Configuration
@ConditionalOnProperty(name = "azure.keyvault.enabled", havingValue = "false", matchIfMissing = true)
public class DefaultKeyStoreConfig {

    @Bean
    public KeyStore defaultKeyStore() {
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            return ks;
        } catch (Exception e) {
            throw new RuntimeException("Falha ao criar KeyStore padrão", e);
        }
    }
}
