package br.com.notafacil.service;


import br.com.notafacil.entity.EmpresaEntity;
import br.com.notafacil.repository.EmpresaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpresaService {

    private final EmpresaRepository repo;

    public EmpresaService(EmpresaRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public EmpresaEntity getByCnpjOrThrow(String cnpjRaw) {
        String cnpj = onlyDigits(cnpjRaw);
        return repo.findByCnpj(cnpj)
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada para CNPJ: " + cnpj));
    }

    private static String onlyDigits(String s) {
        return s == null ? null : s.replaceAll("\\D+", "");
    }
}
