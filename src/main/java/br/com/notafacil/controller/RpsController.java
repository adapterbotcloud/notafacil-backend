package br.com.notafacil.controller;

import br.com.notafacil.entity.RpsEntity;
import br.com.notafacil.repository.EmpresaRepository;
import br.com.notafacil.repository.RpsRepository;
import br.com.notafacil.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/rps")
public class RpsController {

    private final RpsRepository rpsRepo;
    private final EmpresaRepository empresaRepo;
    private final UsuarioRepository usuarioRepo;

    public RpsController(RpsRepository rpsRepo, EmpresaRepository empresaRepo, UsuarioRepository usuarioRepo) {
        this.rpsRepo = rpsRepo;
        this.empresaRepo = empresaRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Integer mes,
            Authentication auth) {

        if (auth == null) return ResponseEntity.status(401).build();

        var usuario = usuarioRepo.findByUsername(auth.getName()).orElseThrow();
        var empresa = empresaRepo.findByCnpj(usuario.getCnpj());
        if (empresa.isEmpty()) return ResponseEntity.ok(List.of());

        List<RpsEntity> lista;
        if (ano != null && mes != null) {
            lista = rpsRepo.findByEmpresaIdAndAnoMes(empresa.get().getId(), ano, mes);
        } else if (ano != null) {
            lista = rpsRepo.findByEmpresaIdAndAno(empresa.get().getId(), ano);
        } else {
            lista = rpsRepo.findByEmpresaId(empresa.get().getId());
        }

        var result = lista.stream().map(this::toMap).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/resumo")
    public ResponseEntity<?> resumo(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();

        var usuario = usuarioRepo.findByUsername(auth.getName()).orElseThrow();
        var empresa = empresaRepo.findByCnpj(usuario.getCnpj());
        if (empresa.isEmpty()) return ResponseEntity.ok(List.of());

        List<Object[]> anosMeses = rpsRepo.findDistinctAnoMesByEmpresaId(empresa.get().getId());
        var result = anosMeses.stream().map(row -> Map.of(
                "ano", row[0],
                "mes", row[1],
                "quantidade", row[2]
        )).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    private Map<String, Object> toMap(RpsEntity r) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", r.getId());
        map.put("idCobranca", r.getIdCobranca());
        map.put("numero", r.getNumero());
        map.put("serie", r.getSerie());
        map.put("dataEmissao", r.getDataEmissao() != null ? r.getDataEmissao().toString() : null);
        map.put("valorServicos", r.getValorServicos());
        map.put("tomadorCpf", r.getTomadorCpf());
        map.put("tomadorRazaoSocial", r.getTomadorRazaoSocial());
        map.put("discriminacao", r.getDiscriminacao());
        map.put("status", r.getStatus());
        map.put("protocolo", r.getProtocolo());
        map.put("mensagemErro", r.getMensagemErro());
        map.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
        return map;
    }
}
