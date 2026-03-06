package br.com.notafacil.controller;

import br.com.notafacil.dto.*;
import br.com.notafacil.repository.UsuarioRepository;
import br.com.notafacil.schemas.ConsultarLoteRpsResposta;
import br.com.notafacil.schemas.ConsultarSituacaoLoteRpsResposta;
import br.com.notafacil.schemas.EnviarLoteRpsResposta;
import br.com.notafacil.service.NfseService;
import br.com.notafacil.service.NfseService1;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import br.com.notafacil.entity.EmpresaEntity;
import br.com.notafacil.repository.RpsRepository;
import br.com.notafacil.repository.EmpresaRepository;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/nfse")
@Validated
public class NfseController {

    private final NfseService1 service1;
    private final NfseService service;
    private final UsuarioRepository usuarioRepo;
    private final RpsRepository rpsRepo;
    private final EmpresaRepository empresaRepo;

    public NfseController(NfseService1 service1, NfseService service, UsuarioRepository usuarioRepo, RpsRepository rpsRepo, EmpresaRepository empresaRepo) {
        this.service1 = service1;
        this.service = service;
        this.usuarioRepo = usuarioRepo;
        this.rpsRepo = rpsRepo;
        this.empresaRepo = empresaRepo;
    }

    @PostMapping("/recepcionar-lote-rps")
    public ResponseEntity<EnviarLoteRpsResposta> recepcionar(@RequestBody EnviarLoteRpsEnvioDto dto) {
        CabecalhoDto cabecalhoDto = new CabecalhoDto("3","3");
        return ResponseEntity.ok(service.recepcionarLote(cabecalhoDto,dto));
    }

    @GetMapping("/consulta-situacao-lote-rps/{protocolo}")
    public ResponseEntity<ConsultarSituacaoLoteRpsResposta> consultarSituacaoLoteRps(
            @PathVariable("protocolo") String protocolo) {
        CabecalhoDto cabecalhoDto = new CabecalhoDto("3","3");
        ConsultarSituacaoLoteRpsResposta resp =
                service.consultarSituacaoLoteRps(cabecalhoDto, protocolo);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/consulta-lote-rps/{protocolo}")
    public ResponseEntity<ConsultarLoteRpsResposta> consultarLoteRps(
            @PathVariable("protocolo") String protocolo) {
        CabecalhoDto cabecalhoDto = new CabecalhoDto("3","3");
        ConsultarLoteRpsResposta resp =
                service.consultarLoteRps(cabecalhoDto, protocolo);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/emitir-rps")
    public ResponseEntity<Void> emitirRps(
            @RequestBody @Valid EmitirNotaRequest request) {
        CabecalhoDto cabecalho = new CabecalhoDto("2.00", "2.00");
        service.emitirRpsEmLotes(cabecalho, request.listaRps());
        return ResponseEntity.accepted().build();
    }

    /**
     * Fluxo SINCRONO: persiste RPS -> envia à prefeitura -> atualiza status
     * CNPJ é obtido do usuário autenticado (JWT)
     * Fallback: header X-Empresa-CNPJ (para compatibilidade)
     */
    @PostMapping("/emitir-rps-teste")
    public ResponseEntity<EmitirRpsResponse> emitirRpsSync(
            @RequestHeader(value = "X-Empresa-CNPJ", required = false) String headerCnpj,
            @RequestBody @Valid EmitirNotaMinRequest request,
            Authentication auth) {

        // Pegar CNPJ do usuário logado; fallback pro header
        String empresaCnpj = headerCnpj;
        if (auth != null) {
            var usuario = usuarioRepo.findByUsername(auth.getName());
            if (usuario.isPresent()) {
                empresaCnpj = usuario.get().getCnpj();
            }
        }

        if (empresaCnpj == null || empresaCnpj.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        var cabecalho = new CabecalhoDto("2.00", "2.00");
        var resp = service1.emitirSincrono(cabecalho, empresaCnpj, request.listaRps());
        return ResponseEntity.ok(resp);
    }

    /**
     * Reenviar RPS pendentes/falhos da empresa do usuário logado
     */
    @PostMapping("/reenviar-pendentes")
    public ResponseEntity<?> reenviarPendentes(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();

        var usuario = usuarioRepo.findByUsername(auth.getName()).orElseThrow();
        String empresaCnpj = usuario.getCnpj();

        if (empresaCnpj == null || empresaCnpj.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "CNPJ não encontrado"));
        }

        try {
            var cabecalho = new CabecalhoDto("2.00", "2.00");
            var resp = service1.reenviarPendentes(cabecalho, empresaCnpj);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("error", e.getMessage(), "reenviados", 0));
        }
    }

    /**
     * Retorna lista de idCobranca que já possuem RPS para a empresa do usuário logado
     */
    @PostMapping("/rps-existentes")
    public ResponseEntity<?> verificarRpsExistentes(
            @RequestBody List<Long> idCobrancas,
            Authentication auth) {

        if (auth == null) return ResponseEntity.status(401).build();

        var usuario = usuarioRepo.findByUsername(auth.getName()).orElseThrow();
        var empresa = empresaRepo.findByCnpj(usuario.getCnpj());
        if (empresa.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<Long> existentes = rpsRepo.findIdCobrancasByEmpresaAndIds(empresa.get().getId(), idCobrancas);
        return ResponseEntity.ok(existentes);
    }
}
