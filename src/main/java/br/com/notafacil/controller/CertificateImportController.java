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

import java.util.Optional;

@RestController
@RequestMapping("/certificates")
@ConditionalOnProperty(name = "azure.keyvault.enabled", havingValue = "true", matchIfMissing = false)
public class CertificateImportController {

    private final Optional<CertificateClient> certificateClient;

    @Autowired(required = false)
    public CertificateImportController(CertificateClient certificateClient) {
        this.certificateClient = Optional.of(certificateClient);
    }

    @GetMapping("/check/{name}")
    public ResponseEntity<?> checkCertificate(@PathVariable String name) {
        if (certificateClient.isEmpty()) {
            return ResponseEntity.ok(java.util.Map.of(
                "available", false,
                "message", "Azure Key Vault não configurado"
            ));
        }
        try {
            var cert = certificateClient.get().getCertificate(name);
            var props = cert.getProperties();
            return ResponseEntity.ok(java.util.Map.of(
                "exists", true,
                "name", name,
                "createdOn", props.getCreatedOn() != null ? props.getCreatedOn().toString() : "",
                "expiresOn", props.getExpiresOn() != null ? props.getExpiresOn().toString() : ""
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(java.util.Map.of("exists", false, "name", name));
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
            byte[] certBytes = file.getBytes();
            ImportCertificateOptions options = new ImportCertificateOptions(name, certBytes)
                    .setPassword(password);
            certificateClient.get().importCertificate(options);
            return ResponseEntity.ok("Certificado importado com sucesso: " + name);
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
