package br.com.notafacil.controller;

import br.com.notafacil.entity.Usuario;
import br.com.notafacil.repository.UsuarioRepository;
import br.com.notafacil.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepo;

    public AuthController(AuthenticationManager authManager, JwtUtil jwtUtil, UsuarioRepository usuarioRepo) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.usuarioRepo = usuarioRepo;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            Usuario user = usuarioRepo.findByUsername(username).orElseThrow();
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getNome(), user.getCnpj());

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "username", user.getUsername(),
                    "nome", user.getNome(),
                    "role", user.getRole(),
                    "cnpj", user.getCnpj()
            ));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(Map.of("message", "Usuário ou senha inválidos"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).body(Map.of("message", "Não autenticado"));
        Usuario user = usuarioRepo.findByUsername(auth.getName()).orElseThrow();
        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "nome", user.getNome(),
                "role", user.getRole(),
                "cnpj", user.getCnpj()
        ));
    }
}
