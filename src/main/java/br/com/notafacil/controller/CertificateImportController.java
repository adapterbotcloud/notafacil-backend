package br.com.notafacil.controller;

import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.core.exception.HttpResponseException;
import com.azure.security.keyvault.certificates.models.ImportCertificateOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/certificates")
@ConditionalOnProperty(name = "azure.keyvault.enabled", havingValue = "true", matchIfMissing = false)
public class CertificateImportController {

    private final Optional<CertificateClient> certificateClient;
    private final KeyStore keyStore;

    @Autowired(required = false)
    public CertificateImportController(CertificateClient certificateClient, KeyStore keyStore) {
        this.certificateClient = Optional.of(certificateClient);
        this.keyStore = keyStore;
    }

    @GetMapping("/check/{name}")
    public ResponseEntity<?> checkCertificate(@PathVariable String name) {
        if (certificateClient.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "available", false,
                "message", "Azure Key Vault não configurado"
            ));
        }
        try {
            var cert = certificateClient.get().getCertificate(name);
            var props = cert.getProperties();

            Map<String, Object> result = new HashMap<>();
            result.put("exists", true);
            result.put("name", name);
            result.put("createdOn", props.getCreatedOn() != null ? props.getCreatedOn().toString() : "");
            result.put("expiresOn", props.getExpiresOn() != null ? props.getExpiresOn().toString() : "");

            // Extrair subject do X509 para ver o CNPJ embutido
            try {
                keyStore.load(null, null);
                X509Certificate x509 = (X509Certificate) keyStore.getCertificate(name);
                if (x509 != null) {
                    result.put("subject", x509.getSubjectX500Principal().getName());
                    result.put("issuer", x509.getIssuerX500Principal().getName());
                    result.put("serialNumber", x509.getSerialNumber().toString());
                }
            } catch (Exception ex) {
                result.put("subjectError", ex.getMessage());
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("exists", false, "name", name));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @PostMapping("/import")
    public ResponseEntity<String> importCertificate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("password") String password
    ) {
        if (certificateClient.isEmpty()) {
            return ResponseEntity.status(503)
                    .body("Azure Key Vault não configurado");
        }
        try {
            String certName = name.startsWith("CNPJ") ? name : "CNPJ" + name;
            byte[] certBytes = file.getBytes();
            ImportCertificateOptions options = new ImportCertificateOptions(certName, certBytes)
                    .setPassword(password);
            certificateClient.get().importCertificate(options);
            return ResponseEntity.ok("Certificado importado com sucesso: " + certName);
        } catch (HttpResponseException e) {
            return ResponseEntity
                    .status(e.getResponse().getStatusCode())
                    .body("Falha ao importar: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity
                    .status(500)
                    .body("Erro interno: " + e.getMessage());
        }
    }
}
