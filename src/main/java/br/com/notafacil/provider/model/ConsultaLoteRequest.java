package br.com.notafacil.provider.model;

import br.com.notafacil.entity.EmpresaEntity;

public record ConsultaLoteRequest(
        EmpresaEntity empresa,
        String protocolo,
        String certificateAlias
) {}
