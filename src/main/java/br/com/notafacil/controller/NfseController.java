package br.com.notafacil.controller;

import br.com.notafacil.dto.CabecalhoDto;
import br.com.notafacil.dto.EnviarLoteRpsEnvioDto;
import br.com.notafacil.dto.InfRpsRequestDto;
import br.com.notafacil.schemas.ConsultarLoteRpsResposta;
import br.com.notafacil.schemas.ConsultarSituacaoLoteRpsResposta;
import br.com.notafacil.schemas.EnviarLoteRpsResposta;
import br.com.notafacil.service.NfseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import br.com.notafacil.dto.EmitirNotaRequest;

import java.util.List;

@RestController
@RequestMapping("/api/v1/nfse")
@Validated
public class NfseController {

    private final NfseService service;


    public NfseController(NfseService service) {
        this.service = service;
    }

    @PostMapping("/recepcionar-lote-rps")
    public ResponseEntity<EnviarLoteRpsResposta> recepcionar(@RequestBody EnviarLoteRpsEnvioDto dto) {
        CabecalhoDto cabecalhoDto = new CabecalhoDto("3","3");
        return ResponseEntity.ok(service.recepcionarLote(cabecalhoDto,dto));
    }


    @GetMapping("/consulta-situacao-lote-rps/{protocolo}")
    public ResponseEntity<ConsultarSituacaoLoteRpsResposta> consultarSituacaoLoteRps(  @PathVariable("protocolo") String protocolo) {
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

}