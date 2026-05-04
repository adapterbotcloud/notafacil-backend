package br.com.notafacil.controller;

import br.com.notafacil.dto.*;
import br.com.notafacil.repository.UsuarioRepository;
import br.com.notafacil.schemas.ConsultarLoteRpsResposta;
import br.com.notafacil.schemas.ConsultarSituacaoLoteRpsResposta;
import br.com.notafacil.schemas.EnviarLoteRpsResposta;
import br.com.notafacil.service.NfseService;
import br.com.notafacil.service.NfseService1;
import br.com.notafacil.service.NfseOrchestrationService;
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
@Validated
public class NfseController {

    private final NfseService1 service1;
    private final NfseService service;
    private final NfseOrchestrationService orchestrationService;
    private final UsuarioRepository usuarioRepo;
    private final RpsRepository rpsRepo;
    private final EmpresaRepository empresaRepo;

    public NfseController(NfseService1 service1,
                          NfseService service,
                          NfseOrchestrationService orchestrationService,
                          UsuarioRepository usuarioRepo,
                          RpsRepository rpsRepo,
                          EmpresaRepository empresaRepo) {
        this.service1 = service1;
        this.service = service;
        this.orchestrationService = orchestrationService;
        this.usuarioRepo = usuarioRepo;
        this.rpsRepo = rpsRepo;
        this.empresaRepo = empresaRepo;
    }

    // ============================================================
    // V2 ENDPOINTS — Multi-provedor (via NfseOrchestrationService)
    // ============================================================

    /**
     * Emissão de NFS-e via arquitetura multi-provedor.
     * Roteia automaticamente para o provider correto (GINFES, XTR_SA, PADRAO_NACIONAL)
     * baseado no código do município da empresa.
     */
    @PostMapping("/api/v2/nfse/emitir")
    public ResponseEntity<EmitirRpsResponse> emitirV2(
            @RequestHeader(value = "X-Empresa-CNPJ", required = false) String headerCnpj,
            @RequestBody @Valid EmitirNotaMinRequest request,
            Authentication auth) {

        String empresaCnpj = resolverCnpj(auth, headerCnpj);
        if (empresaCnpj == null || empresaCnpj.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        var resp = orchestrationService.emitir(empresaCnpj, request.listaRps());
        return ResponseEntity.ok(resp);
    }

    /**
     * Reenviar RPS pendentes/falhos via multi-provedor.
     */
    @PostMapping("/api/v2/nfse/reenviar-pendentes")
    public ResponseEntity<?> reenviarPendentesV2(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();

        var usuario = usuarioRepo.findByUsername(auth.getName()).orElseThrow();
        String empresaCnpj = usuario.getCnpj();

        if (empresaCnpj == null || empresaCnpj.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "CNPJ não encontrado"));
        }

        try {
            var resp = orchestrationService.reenviarPendentes(empresaCnpj);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("error", e.getMessage(), "reenviados", 0));
        }
    }

    // ============================================================
    // V1 ENDPOINTS — Legacy (backward compatibility)
    // ============================================================

    @PostMapping("/api/v1/nfse/recepcionar-lote-rps")
    public ResponseEntity<EnviarLoteRpsResposta> recepcionar(@RequestBody EnviarLoteRpsEnvioDto dto) {
        CabecalhoDto cabecalhoDto = new CabecalhoDto("3","3");
        return ResponseEntity.ok(service.recepcionarLote(cabecalhoDto,dto));
    }

    @GetMapping("/api/v1/nfse/consulta-situacao-lote-rps/{protocolo}")
    public ResponseEntity<ConsultarSituacaoLoteRpsResposta> consultarSituacaoLoteRps(
            @PathVariable("protocolo") String protocolo) {
        CabecalhoDto cabecalhoDto = new CabecalhoDto("3","3");
        ConsultarSituacaoLoteRpsResposta resp =
                service.consultarSituacaoLoteRps(cabecalhoDto, protocolo);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/api/v1/nfse/consulta-lote-rps/{protocolo}")
    public ResponseEntity<ConsultarLoteRpsResposta> consultarLoteRps(
            @PathVariable("protocolo") String protocolo) {
        CabecalhoDto cabecalhoDto = new CabecalhoDto("3","3");
        ConsultarLoteRpsResposta resp =
                service.consultarLoteRps(cabecalhoDto, protocolo);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/api/v1/nfse/emitir-rps")
    public ResponseEntity<Void> emitirRps(
            @RequestBody @Valid EmitirNotaRequest request) {
        CabecalhoDto cabecalho = new CabecalhoDto("2.00", "2.00");
        service.emitirRpsEmLotes(cabecalho, request.listaRps());
        return ResponseEntity.accepted().build();
    }

    /**
     * @deprecated Use POST /api/v2/nfse/emitir instead
     */
    @Deprecated
    @PostMapping("/api/v1/nfse/emitir-rps-teste")
    public ResponseEntity<EmitirRpsResponse> emitirRpsSync(
            @RequestHeader(value = "X-Empresa-CNPJ", required = false) String headerCnpj,
            @RequestBody @Valid EmitirNotaMinRequest request,
            Authentication auth) {

        String empresaCnpj = resolverCnpj(auth, headerCnpj);
        if (empresaCnpj == null || empresaCnpj.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        var cabecalho = new CabecalhoDto("2.00", "2.00");
        var resp = service1.emitirSincrono(cabecalho, empresaCnpj, request.listaRps());
        return ResponseEntity.ok(resp);
    }

    /**
     * @deprecated Use POST /api/v2/nfse/reenviar-pendentes instead
     */
    @Deprecated
    @PostMapping("/api/v1/nfse/reenviar-pendentes")
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

    @PostMapping("/api/v1/nfse/rps-existentes")
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

    // ============================================================
    // Helpers
    // ============================================================

    private String resolverCnpj(Authentication auth, String headerCnpj) {
        String cnpj = headerCnpj;
        if (auth != null) {
            var usuario = usuarioRepo.findByUsername(auth.getName());
            if (usuario.isPresent()) {
                cnpj = usuario.get().getCnpj();
            }
        }
        return cnpj;
    }
}
