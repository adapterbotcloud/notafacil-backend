package br.com.notafacil.config;

import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.security.KeyStore;
import java.security.Security;

@Configuration

@ConditionalOnProperty(name = "azure.keyvault.enabled", havingValue = "true", matchIfMissing = false)
public class AzureKeyVaultConfig {

    private static final Logger log = LoggerFactory.getLogger(AzureKeyVaultConfig.class);

    @Value("${azure.keyvault.url:}")
    private String vaultUrl;

    @Value("${azure.client.id:}")
    private String clientId;

    @Value("${azure.client.secret:}")
    private String clientSecret;

    @Value("${azure.tenant.id:}")
    private String tenantId;

    @Bean
    public CertificateClient certificateClient() {
        if (vaultUrl == null || vaultUrl.isBlank()) {
            throw new IllegalStateException("Azure Key Vault URL is not configured");
        }
        return new CertificateClientBuilder()
                .vaultUrl(vaultUrl)
                .credential(new ClientSecretCredentialBuilder()
                        .tenantId(tenantId)
                        .clientId(clientId)
                        .clientSecret(clientSecret)
                        .build())
                .buildClient();
    }

    @Bean
    @ConditionalOnProperty(name = "azure.keyvault.enabled", havingValue = "true", matchIfMissing = false)
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
    @ConditionalOnProperty(name = "azure.keyvault.enabled", havingValue = "true", matchIfMissing = false)
    public KeyStore azureKeyVaultKeyStore(KeyVaultJcaProvider provider) {
        try {
            KeyStore ks = KeyStore.getInstance("AzureKeyVault");
            ks.load(null, null);
            return ks;
        } catch (Exception e) {
            log.warn("Azure Key Vault KeyStore não pôde ser carregado: {}", e.getMessage());
            throw new RuntimeException("Falha ao criar Azure Key Vault KeyStore", e);
        }
    }
}
