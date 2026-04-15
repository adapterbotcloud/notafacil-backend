package br.com.notafacil.dto;

/**
 * InfRps estendido com campos obrigatórios v4 (IBS/CBS).
 * Adiciona NBS, indicadorOperacao, cst, classificacaoTributaria
 * e o bloco IbsCbs ao InfRpsDto padrão.
 */
public record InfRpsV4Dto(
    InfRpsDto infRps,

    // ─── Campos v4 obrigatórios (empresa) ────────────────────────────────────
    String nbs,
    String indicadorOperacao,
    String cst,
    String classificacaoTributaria,

    // ─── Bloco IBS/CBS (v4) ─────────────────────────────────────────────────
    IbsCbsDto ibsCbs
) {}
