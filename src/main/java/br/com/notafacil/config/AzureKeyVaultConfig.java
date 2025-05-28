package br.com.notafacil.config;

import com.azure.identity.ClientSecretCredential;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyStore;
import java.security.Security;


@Configuration
public class AzureKeyVaultConfig {

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
        // Ajusta as properties para o provider JCA
        System.setProperty("azure.keyvault.uri", vaultUrl);
        System.setProperty("azure.keyvault.client-id", clientId);
        System.setProperty("azure.keyvault.client-secret", clientSecret);
        System.setProperty("azure.keyvault.tenant-id", tenantId);
        KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
        Security.addProvider(provider);
        return provider;
    }

    @Bean
    public KeyStore azureKeyVaultKeyStore(KeyVaultJcaProvider provider) throws Exception {
        KeyStore ks = KeyStore.getInstance("AzureKeyVault");
        ks.load(null, null);
        return ks;
    }

    @Bean
    public CertificateClient certificateClient(ClientSecretCredential credential) {
        return new CertificateClientBuilder()
                .vaultUrl(vaultUrl)
                .credential(credential)
                .buildClient();
    }
}