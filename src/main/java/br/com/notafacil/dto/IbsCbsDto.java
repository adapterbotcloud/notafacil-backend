package br.com.notafacil.dto;

/**
 * Bloco IBS/CBS - Campos obrigatórios v4 para NFSe SEFIN/Ginfes.
 * Reforma tributária: novos campos de classificação tributária.
 */
public record IbsCbsDto(
    /** Indicador de finalidade da NFSe (código) */
    String codigoIndicadorFinalidadeNFSe,

    /** Indicador operação uso/consumo pessoal */
    String codigoIndicadorOperacaoUsoConsumoPessoal,

    /** Código indicador operação (6 dígitos) */
    String codigoIndicadorOperacao,

    /** Tipo operação */
    String tipoOp,

    /** NFSe referenciadas (chaves de 50 dígitos) */
    java.util.List<String> nfseReferenciadas,

    /** Tipo ente governamental */
    String tipoEnteGovernamental,

    /** Indicador destinatário (0=NT, 1=T) */
    String indDest,

    /** CST - Código Situação Tributária (3 dígitos, ex: 101, 102...) */
    String cst,

    /** Código classificação tributária (6 dígitos, ex: 410003) */
    String codigoClassTrib,

    /** Código crédito presumido (2 dígitos) */
    String codigoCreditoPresumido
) {
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String codigoIndicadorFinalidadeNFSe;
        private String codigoIndicadorOperacaoUsoConsumoPessoal;
        private String codigoIndicadorOperacao;
        private String tipoOp;
        private java.util.List<String> nfseReferenciadas = new java.util.ArrayList<>();
        private String tipoEnteGovernamental;
        private String indDest;
        private String cst;
        private String codigoClassTrib;
        private String codigoCreditoPresumido;

        public Builder codigoIndicadorFinalidadeNFSe(String v) { this.codigoIndicadorFinalidadeNFSe = v; return this; }
        public Builder codigoIndicadorOperacaoUsoConsumoPessoal(String v) { this.codigoIndicadorOperacaoUsoConsumoPessoal = v; return this; }
        public Builder codigoIndicadorOperacao(String v) { this.codigoIndicadorOperacao = v; return this; }
        public Builder tipoOp(String v) { this.tipoOp = v; return this; }
        public Builder nfseReferenciadas(java.util.List<String> v) { this.nfseReferenciadas = v; return this; }
        public Builder tipoEnteGovernamental(String v) { this.tipoEnteGovernamental = v; return this; }
        public Builder indDest(String v) { this.indDest = v; return this; }
        public Builder cst(String v) { this.cst = v; return this; }
        public Builder codigoClassTrib(String v) { this.codigoClassTrib = v; return this; }
        public Builder codigoCreditoPresumido(String v) { this.codigoCreditoPresumido = v; return this; }

        public IbsCbsDto build() {
            return new IbsCbsDto(
                codigoIndicadorFinalidadeNFSe,
                codigoIndicadorOperacaoUsoConsumoPessoal,
                codigoIndicadorOperacao,
                tipoOp,
                nfseReferenciadas,
                tipoEnteGovernamental,
                indDest,
                cst,
                codigoClassTrib,
                codigoCreditoPresumido
            );
        }
    }
}
