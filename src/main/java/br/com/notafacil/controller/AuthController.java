package br.com.notafacil.controller;

import br.com.notafacil.entity.Usuario;
import br.com.notafacil.repository.UsuarioRepository;
import br.com.notafacil.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = {"https://notafacil.adapterbot.cloud", "https://notafacil-dev.adapterbot.cloud", "http://localhost:3000", "http://localhost:3001"})
public class AuthController {

    private final UsuarioRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UsuarioRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Usuário e senha são obrigatórios"));
        }

        var userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Usuário ou senha inválidos"));
        }

        Usuario user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message", "Usuário ou senha inválidos"));
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getNome(), user.getCnpj());

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("nome", user.getNome());
        response.put("role", user.getRole());
        response.put("cnpj", user.getCnpj());
        response.put("token", token);

        return ResponseEntity.ok(response);
    }
}
