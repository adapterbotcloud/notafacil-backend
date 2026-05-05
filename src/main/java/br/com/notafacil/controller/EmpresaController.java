package br.com.notafacil.controller;

import br.com.notafacil.entity.EmpresaEntity;
import br.com.notafacil.repository.EmpresaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/empresas")
public class EmpresaController {

    private static final Logger log = LoggerFactory.getLogger(EmpresaController.class);

    private final EmpresaRepository empresaRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public EmpresaController(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @GetMapping("/consulta-cnpj/{cnpj}")
    public ResponseEntity<?> consultarCnpj(@PathVariable String cnpj) {
        String cnpjLimpo = cnpj.replaceAll("\\D", "");
        if (cnpjLimpo.length() != 14) {
            return ResponseEntity.badRequest().body(Map.of("error", "CNPJ inválido"));
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> resp = restTemplate.getForObject(
                    "https://brasilapi.com.br/api/cnpj/v1/" + cnpjLimpo, Map.class);
            if (resp == null) {
                return ResponseEntity.status(502).body(Map.of("error", "Resposta vazia da BrasilAPI"));
            }

            Map<String, Object> dados = new LinkedHashMap<>();
            dados.put("cnpj", cnpjLimpo);
            dados.put("razaoSocial", resp.get("razao_social"));
            dados.put("codigoMunicipio", String.valueOf(resp.get("codigo_municipio_ibge")));
            dados.put("municipio", resp.get("municipio"));
            dados.put("uf", resp.get("uf"));
            dados.put("endereco", buildEndereco(resp));
            dados.put("numero", resp.get("numero"));
            dados.put("complemento", resp.get("complemento"));
            dados.put("bairro", resp.get("bairro"));
            dados.put("cep", resp.get("cep"));
            dados.put("telefone", resp.get("ddd_telefone_1"));
            dados.put("cnaeFiscal", resp.get("cnae_fiscal"));
            dados.put("cnaeFiscalDescricao", resp.get("cnae_fiscal_descricao"));
            dados.put("optanteSimplesNacional", Boolean.TRUE.equals(resp.get("opcao_pelo_simples")) ? 1 : 2);

            return ResponseEntity.ok(dados);
        } catch (Exception e) {
            log.warn("Falha ao consultar CNPJ {}: {}", cnpjLimpo, e.getMessage());
            return ResponseEntity.status(502).body(Map.of("error", "Falha ao consultar BrasilAPI: " + e.getMessage()));
        }
    }

    private String buildEndereco(Map<String, Object> resp) {
        String tipo = resp.get("descricao_tipo_de_logradouro") != null
                ? resp.get("descricao_tipo_de_logradouro").toString() : "";
        String logradouro = resp.get("logradouro") != null
                ? resp.get("logradouro").toString() : "";
        return (tipo + " " + logradouro).trim();
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
