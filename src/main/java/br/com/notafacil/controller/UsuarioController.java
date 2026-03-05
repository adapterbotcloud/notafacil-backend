package br.com.notafacil.controller;

import br.com.notafacil.entity.Usuario;
import br.com.notafacil.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/usuarios")
@PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
public class UsuarioController {

    private static final Set<String> ROLES_VALIDAS = Set.of("ADMIN", "GESTOR", "USER");

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public UsuarioController(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @GetMapping
    public List<Map<String, Object>> listar(Authentication auth) {
        var caller = repo.findByUsername(auth.getName()).orElseThrow();
        if (caller.getRole().equals("ADMIN")) {
            return repo.findAll().stream().map(this::toMap).collect(Collectors.toList());
        }
        // GESTOR só vê usuários do mesmo CNPJ
        return repo.findByCnpj(caller.getCnpj()).stream().map(this::toMap).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id, Authentication auth) {
        var caller = repo.findByUsername(auth.getName()).orElseThrow();
        return repo.findById(id)
                .filter(u -> caller.getRole().equals("ADMIN") || u.getCnpj().equals(caller.getCnpj()))
                .map(u -> ResponseEntity.ok(toMap(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Map<String, String> body, Authentication auth) {
        var caller = repo.findByUsername(auth.getName()).orElseThrow();

        String username = body.get("username");
        String password = body.get("password");
        String nome = body.get("nome");
        String cnpj = body.get("cnpj");
        String role = body.getOrDefault("role", "USER");

        if (username == null || password == null || nome == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Campos obrigatórios: username, password, nome"));
        }

        if (repo.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Usuário já existe: " + username));
        }

        if (!ROLES_VALIDAS.contains(role)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Role deve ser ADMIN, GESTOR ou USER"));
        }

        // GESTOR: só pode criar USER ou GESTOR no mesmo CNPJ
        if (caller.getRole().equals("GESTOR")) {
            if (role.equals("ADMIN")) {
                return ResponseEntity.status(403).body(Map.of("message", "GESTOR não pode criar usuário ADMIN"));
            }
            cnpj = caller.getCnpj(); // força o mesmo CNPJ
        } else {
            if (cnpj == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "CNPJ é obrigatório"));
            }
            cnpj = cnpj.replaceAll("[.\\-/]", "");
        }

        Usuario u = new Usuario(username, encoder.encode(password), nome, cnpj, role);
        repo.save(u);
        return ResponseEntity.ok(toMap(u));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Map<String, String> body, Authentication auth) {
        var caller = repo.findByUsername(auth.getName()).orElseThrow();
        return repo.findById(id).map(u -> {
            // GESTOR só edita usuários do mesmo CNPJ e não pode promover a ADMIN
            if (caller.getRole().equals("GESTOR")) {
                if (!u.getCnpj().equals(caller.getCnpj())) {
                    return ResponseEntity.status(403).body((Object) Map.of("message", "Sem permissão para editar este usuário"));
                }
                if (u.getRole().equals("ADMIN")) {
                    return ResponseEntity.status(403).body((Object) Map.of("message", "GESTOR não pode editar ADMIN"));
                }
            }
            if (body.containsKey("nome")) u.setNome(body.get("nome"));
            if (body.containsKey("cnpj")) {
                if (caller.getRole().equals("ADMIN")) {
                    u.setCnpj(body.get("cnpj").replaceAll("[.\\-/]", ""));
                }
                // GESTOR não pode mudar CNPJ
            }
            if (body.containsKey("role")) {
                String role = body.get("role");
                if (!ROLES_VALIDAS.contains(role)) {
                    return ResponseEntity.badRequest().body((Object) Map.of("message", "Role deve ser ADMIN, GESTOR ou USER"));
                }
                if (caller.getRole().equals("GESTOR") && role.equals("ADMIN")) {
                    return ResponseEntity.status(403).body((Object) Map.of("message", "GESTOR não pode promover a ADMIN"));
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
    public ResponseEntity<?> deletar(@PathVariable Long id, Authentication auth) {
        var caller = repo.findByUsername(auth.getName()).orElseThrow();
        return repo.findById(id).map(u -> {
            if (caller.getRole().equals("GESTOR")) {
                if (!u.getCnpj().equals(caller.getCnpj())) {
                    return ResponseEntity.status(403).body((Object) Map.of("message", "Sem permissão"));
                }
                if (u.getRole().equals("ADMIN")) {
                    return ResponseEntity.status(403).body((Object) Map.of("message", "GESTOR não pode remover ADMIN"));
                }
            }
            repo.deleteById(id);
            return ResponseEntity.ok((Object) Map.of("message", "Usuário removido"));
        }).orElse(ResponseEntity.notFound().build());
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
