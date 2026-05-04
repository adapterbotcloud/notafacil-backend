package br.com.notafacil.provider.model;

import br.com.notafacil.entity.EmpresaEntity;

public record CancelamentoRequest(
        EmpresaEntity empresa,
        String numeroNfse,
        String codigoCancelamento,
        String certificateAlias
) {}
