package br.com.notafacil.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * InfRpsRequestDto estendido com campos obrigatórios v4 (IBS/CBS).
 * Adiciona: nbs, indicadorOperacao, cst, classificacaoTributaria, ibsCbs
 */
public record InfRpsV4RequestDto(
        @NotNull Long id,
        @NotNull Integer naturezaOperacao,
        @NotNull Integer regimeEspecialTributacao,
        @NotNull Integer optanteSimplesNacional,
        @NotNull Integer incentivadorCultural,
        @NotNull Integer status,
        @NotNull ServicoV4Request servico,
        @NotNull InfRpsRequestDto.PrestadorRequest prestador,
        @NotNull InfRpsRequestDto.TomadorRequest tomador,
        LocalDateTime dataEmissao
) {
    /**
     * Serviço com campos v4 (NBS + IBS/CBS).
     */
    public record ServicoV4Request(
            @NotNull InfRpsRequestDto.ServicoRequest.ValoresRequest valores,
            @NotBlank String itemListaServico,
            @NotBlank String codigoTributacaoMunicipio,
            @NotBlank String discriminacao,
            @NotBlank String codigoMunicipio,

            // ─── Campos v4 ─────────────────────────────────────────────────────
            /** NBS - Nomenclatura Brasileira de Serviços (9 dígitos) */
            @NotBlank @Size(max = 9) String nbs,

            /** Indicador de Operação (6 dígitos) */
            @NotBlank @Size(max = 6) String indicadorOperacao,

            /** IbsCbsDto com CST e Classificação Tributária */
            @NotNull IbsCbsDto ibsCbs
    ) {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Integer naturezaOperacao;
        private Integer regimeEspecialTributacao;
        private Integer optanteSimplesNacional;
        private Integer incentivadorCultural;
        private Integer status;
        private ServicoV4Request servico;
        private InfRpsRequestDto.PrestadorRequest prestador;
        private InfRpsRequestDto.TomadorRequest tomador;
        private LocalDateTime dataEmissao;

        public Builder id(Long v) { this.id = v; return this; }
        public Builder naturezaOperacao(Integer v) { this.naturezaOperacao = v; return this; }
        public Builder regimeEspecialTributacao(Integer v) { this.regimeEspecialTributacao = v; return this; }
        public Builder optanteSimplesNacional(Integer v) { this.optanteSimplesNacional = v; return this; }
        public Builder incentivadorCultural(Integer v) { this.incentivadorCultural = v; return this; }
        public Builder status(Integer v) { this.status = v; return this; }
        public Builder servico(ServicoV4Request v) { this.servico = v; return this; }
        public Builder prestador(InfRpsRequestDto.PrestadorRequest v) { this.prestador = v; return this; }
        public Builder tomador(InfRpsRequestDto.TomadorRequest v) { this.tomador = v; return this; }
        public Builder dataEmissao(LocalDateTime v) { this.dataEmissao = v; return this; }

        public InfRpsV4RequestDto build() {
            return new InfRpsV4RequestDto(id, naturezaOperacao, regimeEspecialTributacao,
                optanteSimplesNacional, incentivadorCultural, status, servico,
                prestador, tomador, dataEmissao);
        }
    }
}
