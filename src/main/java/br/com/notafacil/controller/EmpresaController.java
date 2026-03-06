package br.com.notafacil.controller;

import br.com.notafacil.entity.EmpresaEntity;
import br.com.notafacil.repository.EmpresaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/empresas")
public class EmpresaController {

    private final EmpresaRepository empresaRepository;

    public EmpresaController(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @GetMapping
    public List<EmpresaEntity> listar() {
        return empresaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaEntity> buscar(@PathVariable Long id) {
        return empresaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> criar(@RequestBody EmpresaEntity empresa) {
        if (empresaRepository.findByCnpj(empresa.getCnpj()).isPresent()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "CNPJ já cadastrado"));
        }
        EmpresaEntity salva = empresaRepository.save(empresa);
        return ResponseEntity.ok(salva);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody EmpresaEntity empresa) {
        return empresaRepository.findById(id).map(existing -> {
            if (empresa.getRazaoSocial() != null) existing.setRazaoSocial(empresa.getRazaoSocial());
            if (empresa.getCnpj() != null) existing.setCnpj(empresa.getCnpj());
            if (empresa.getInscricaoMunicipal() != null) existing.setInscricaoMunicipal(empresa.getInscricaoMunicipal());
            if (empresa.getEndereco() != null) existing.setEndereco(empresa.getEndereco());
            if (empresa.getNumero() != null) existing.setNumero(empresa.getNumero());
            if (empresa.getComplemento() != null) existing.setComplemento(empresa.getComplemento());
            if (empresa.getBairro() != null) existing.setBairro(empresa.getBairro());
            if (empresa.getCep() != null) existing.setCep(empresa.getCep());
            if (empresa.getTelefone() != null) existing.setTelefone(empresa.getTelefone());
            if (empresa.getAliquota() != null) existing.setAliquota(empresa.getAliquota());
            if (empresa.getItemListaServico() != null) existing.setItemListaServico(empresa.getItemListaServico());
            if (empresa.getCodigoTributacaoMunicipio() != null) existing.setCodigoTributacaoMunicipio(empresa.getCodigoTributacaoMunicipio());
            if (empresa.getCodigoMunicipio() != null) existing.setCodigoMunicipio(empresa.getCodigoMunicipio());
            if (empresa.getRegimeEspecialTributacao() != null) existing.setRegimeEspecialTributacao(empresa.getRegimeEspecialTributacao());
            if (empresa.getOptanteSimplesNacional() != null) existing.setOptanteSimplesNacional(empresa.getOptanteSimplesNacional());
            if (empresa.getIncentivadorCultural() != null) existing.setIncentivadorCultural(empresa.getIncentivadorCultural());
            if (empresa.getSubstitutoTributario() != null) existing.setSubstitutoTributario(empresa.getSubstitutoTributario());
            return ResponseEntity.ok(empresaRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        if (!empresaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        empresaRepository.deleteById(id);
        return ResponseEntity.ok(java.util.Map.of("message", "Empresa removida"));
    }
}
