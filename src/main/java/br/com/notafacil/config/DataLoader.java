package br.com.notafacil.config;

import br.com.notafacil.entity.Usuario;
import br.com.notafacil.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public DataLoader(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (repo.findByUsername("admin").isEmpty()) {
            repo.save(new Usuario("admin", encoder.encode("admin123"), "Administrador", "27288254000103", "ADMIN"));
            System.out.println(">>> Usuário admin criado com senha: admin123");
        }
    }
}
