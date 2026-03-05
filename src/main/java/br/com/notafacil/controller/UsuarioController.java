package br.com.notafacil.controller;

import br.com.notafacil.entity.Usuario;
import br.com.notafacil.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public UsuarioController(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @GetMapping
    public List<Map<String, Object>> listar() {
        return repo.findAll().stream().map(this::toMap).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id) {
        return repo.findById(id)
                .map(u -> ResponseEntity.ok(toMap(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String nome = body.get("nome");
        String cnpj = body.get("cnpj");
        String role = body.getOrDefault("role", "USER");

        if (username == null || password == null || nome == null || cnpj == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Campos obrigatórios: username, password, nome, cnpj"));
        }

        if (repo.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Usuário já existe: " + username));
        }

        cnpj = cnpj.replaceAll("[.\\-/]", "");

        if (!role.equals("ADMIN") && !role.equals("USER")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Role deve ser ADMIN ou USER"));
        }

        Usuario u = new Usuario(username, encoder.encode(password), nome, cnpj, role);
        repo.save(u);
        return ResponseEntity.ok(toMap(u));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return repo.findById(id).map(u -> {
            if (body.containsKey("nome")) u.setNome(body.get("nome"));
            if (body.containsKey("cnpj")) u.setCnpj(body.get("cnpj").replaceAll("[.\\-/]", ""));
            if (body.containsKey("role")) {
                String role = body.get("role");
                if (!role.equals("ADMIN") && !role.equals("USER")) {
                    return ResponseEntity.badRequest().body((Object) Map.of("message", "Role deve ser ADMIN ou USER"));
                }
                u.setRole(role);
            }
            if (body.containsKey("password") && !body.get("password").isBlank()) {
                u.setPassword(encoder.encode(body.get("password")));
            }
            repo.save(u);
            return ResponseEntity.ok((Object) toMap(u));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Usuário removido"));
    }

    private Map<String, Object> toMap(Usuario u) {
        return Map.of(
                "id", u.getId(),
                "username", u.getUsername(),
                "nome", u.getNome(),
                "cnpj", u.getCnpj(),
                "role", u.getRole()
        );
    }
}
