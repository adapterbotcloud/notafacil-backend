package br.com.notafacil.provider.model;

/**
 * @param situacao código de situação: 1=Não Recebido, 2=Não Processado, 3=Processado com Erro, 4=Processado com Sucesso
 */
public record ConsultaSituacaoResponse(
        int situacao,
        String mensagemErro,
        String rawResponse
) {}
