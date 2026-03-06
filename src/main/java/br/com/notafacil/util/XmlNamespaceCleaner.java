package br.com.notafacil.util;

public class XmlNamespaceCleaner {

    private static final String NS_ENVIO = "http://www.ginfes.com.br/servico_enviar_lote_rps_envio_v03.xsd";
    private static final String NS_TIPOS = "http://www.ginfes.com.br/tipos_v03.xsd";

    public static String limpar(String xml) {
        // 1) Remove todas as declaracoes xmlns:nsN="..."
        xml = xml.replaceAll("\\s+xmlns:ns\\d+=\"[^\"]*\"", "");

        // 2) Substitui <nsN:EnviarLoteRpsEnvio...> pelo root correto
        xml = xml.replaceAll("<ns\\d+:EnviarLoteRpsEnvio[^>]*>",
            "<EnviarLoteRpsEnvio xmlns=\"" + NS_ENVIO + "\" xmlns:tipos=\"" + NS_TIPOS + "\">");
        xml = xml.replaceAll("</ns\\d+:EnviarLoteRpsEnvio>", "</EnviarLoteRpsEnvio>");

        // 3) LoteRps pertence ao namespace do envio (sem prefixo)
        xml = xml.replaceAll("<ns\\d+:LoteRps", "<LoteRps");
        xml = xml.replaceAll("</ns\\d+:LoteRps>", "</LoteRps>");

        // 4) Todos os outros elementos nsN: -> tipos:
        xml = xml.replaceAll("<ns\\d+:", "<tipos:");
        xml = xml.replaceAll("</ns\\d+:", "</tipos:");

        return xml;
    }
}
