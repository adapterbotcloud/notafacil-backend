package br.com.notafacil.config;

import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyStore;
import java.security.Security;


@Configuration
@ConditionalOnProperty(name = "azure.keyvault.url", matchIfMissing = false)
public class AzureKeyVaultConfig {

    private static final Logger log = LoggerFactory.getLogger(AzureKeyVaultConfig.class);

    @Value("${azure.keyvault.url}")
    private String vaultUrl;

    @Value("${azure.client.id}")
    private String clientId;

    @Value("${azure.client.secret}")
    private String clientSecret;

    @Value("${azure.tenant.id}")
    private String tenantId;


    @Bean
    public KeyVaultJcaProvider keyVaultProvider() {
        System.setProperty("azure.keyvault.uri", vaultUrl);
        System.setProperty("azure.keyvault.client-id", clientId);
        System.setProperty("azure.keyvault.client-secret", clientSecret);
        System.setProperty("azure.keyvault.tenant-id", tenantId);
        KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
        Security.addProvider(provider);
        return provider;
    }

    @Bean
    public KeyStore azureKeyVaultKeyStore(KeyVaultJcaProvider provider) {
        try {
            KeyStore ks = KeyStore.getInstance("AzureKeyVault");
            ks.load(null, null);
            return ks;
        } catch (Exception e) {
            log.warn("Azure Key Vault indisponível: {}. Certificado digital não funcionará.", e.getMessage());
            try {
                KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                ks.load(null, null);
                return ks;
            } catch (Exception ex) {
                throw new RuntimeException("Falha ao criar KeyStore fallback", ex);
            }
        }
    }
}