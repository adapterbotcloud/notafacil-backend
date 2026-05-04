package br.com.notafacil.provider.model;

import br.com.notafacil.entity.EmpresaEntity;
import br.com.notafacil.entity.RpsEntity;

import java.util.List;

public record EnviarLoteRequest(
        EmpresaEntity empresa,
        List<RpsEntity> rpsList,
        String certificateAlias,
        String versaoSchema
) {}
