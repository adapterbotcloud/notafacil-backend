package br.com.notafacil.strategy.impl;

import br.com.notafacil.strategy.CertificateAliasStrategy;
import org.springframework.stereotype.Component;

@Component
public class DefaultCertificateAliasStrategy implements CertificateAliasStrategy {
    @Override
    public String resolveAlias() {
        // Exemplo estático; substitua pela lógica real
        return "27288254000103";
    }
}
