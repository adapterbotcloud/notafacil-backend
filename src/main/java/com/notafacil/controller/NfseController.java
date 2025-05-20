package com.notafacil.controller;

import com.notafacil.dto.CabecalhoDto;
import com.notafacil.dto.EnviarLoteRpsEnvioDto;
import com.notafacil.schemas.EnviarLoteRpsResposta;
import com.notafacil.service.NfseJaxWsService;
import com.notafacil.service.NfseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/nfse")
public class NfseController {

    private final NfseService service;
    private final NfseJaxWsService jaxWsService;

    public NfseController(NfseService service, NfseJaxWsService jaxWsService) {
        this.jaxWsService = jaxWsService;
        this.service = service;
    }

    @PostMapping("/recepcionar-lote-rps")
    public ResponseEntity<EnviarLoteRpsResposta> recepcionar(@RequestBody EnviarLoteRpsEnvioDto dto) {
        CabecalhoDto cabecalhoDto = new CabecalhoDto("3","3");
       // return ResponseEntity.ok(service.recepcionarLoteRps(cabecalhoDto,dto));
        return ResponseEntity.ok(jaxWsService.recepcionarLote(cabecalhoDto,dto));
    }
}