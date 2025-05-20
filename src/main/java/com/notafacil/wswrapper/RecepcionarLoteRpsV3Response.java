package com.notafacil.wswrapper;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * JAXB wrapper for the WSDL operation response "RecepcionarLoteRpsV3Response".
 * Corresponds to the message:
 *
 * <pre>
 * &lt;wsdl:message name="RecepcionarLoteRpsV3Response"&gt;
 *   &lt;wsdl:part name="EnviarLoteRpsResposta" type="xsd:string"/&gt;
 * &lt;/wsdl:message&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "",
        propOrder = { "enviarLoteRpsResposta" }
)
@XmlRootElement(
        name = "RecepcionarLoteRpsV3Response",
        namespace = "http://producao.issfortaleza.com.br"
)
public class RecepcionarLoteRpsV3Response {

    /**
     * O XML de resposta retornado pelo método RecepcionarLoteRpsV3,
     * na forma de string contendo o elemento <EnviarLoteRpsResposta>…</EnviarLoteRpsResposta>.
     */
    @XmlElement(
            name = "EnviarLoteRpsResposta",
            namespace = "http://producao.issfortaleza.com.br",
            required = true
    )
    protected String enviarLoteRpsResposta;

    /**
     * Obtém o conteúdo de EnviarLoteRpsResposta.
     */
    public String getEnviarLoteRpsResposta() {
        return enviarLoteRpsResposta;
    }

    /**
     * Define o conteúdo de EnviarLoteRpsResposta.
     */
    public void setEnviarLoteRpsResposta(String value) {
        this.enviarLoteRpsResposta = value;
    }
}
