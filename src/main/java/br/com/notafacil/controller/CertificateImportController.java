package br.com.notafacil.controller;

import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.core.exception.HttpResponseException;
import com.azure.security.keyvault.certificates.models.ImportCertificateOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
@RequestMapping("/certificates")
public class CertificateImportController {

    private final CertificateClient certificateClient;


    public CertificateImportController(CertificateClient certificateClient) {
        this.certificateClient = certificateClient;
    }

    @PostMapping("/import")
    public ResponseEntity<String> importCertificate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("password") String password
    ) {
        try {
            byte[] certBytes = file.getBytes();
            ImportCertificateOptions options = new ImportCertificateOptions(name, certBytes)
                    .setPassword(password);
            certificateClient.importCertificate(options);
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