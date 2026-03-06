package br.com.notafacil.dto;

public record CabecalhoDto(
        String versao,
        String versaoDados
) {
    @Override
    public String toString() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<ns2:cabecalho versao=\"" + versao + "\" xmlns:ns2=\"http://www.ginfes.com.br/cabecalho_v03.xsd\">" +
               "<versaoDados>" + versaoDados + "</versaoDados>" +
               "</ns2:cabecalho>";
    }
}
