package br.com.notafacil.strategy.impl;

import br.com.notafacil.repository.UsuarioRepository;
import br.com.notafacil.strategy.CertificateAliasStrategy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class DefaultCertificateAliasStrategy implements CertificateAliasStrategy {

    private final UsuarioRepository usuarioRepo;

    public DefaultCertificateAliasStrategy(UsuarioRepository usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    public String resolveAlias() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            var usuario = usuarioRepo.findByUsername(auth.getName());
            if (usuario.isPresent() && usuario.get().getCnpj() != null) {
                return "CNPJ" + usuario.get().getCnpj();
            }
        }
        // Fallback
        return "CNPJ27288254000103";
    }
}
