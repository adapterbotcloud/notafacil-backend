package com.notafacil.wswrapper;


import com.notafacil.schemas.EnviarLoteRpsEnvio;
import jakarta.xml.bind.annotation.*;

/**
 * <p>Java class for anonymous complex type corresponding to the WSDL operation wrapper.</p>
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Cabecalho" type="{http://www.ginfes.com.br/cabecalho_v03.xsd}Cabecalho"/&gt;
 *         &lt;element name="EnviarLoteRpsEnvio" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "cabecalho",
        "enviarLoteRpsEnvio"
})
@XmlRootElement(name = "RecepcionarLoteRpsV3", namespace = "http://producao.issfortaleza.com.br")
public class RecepcionarLoteRpsV3 {

    @XmlElement(name = "Cabecalho", namespace = "http://producao.issfortaleza.com.br", required = true)
    protected String cabecalho;

    @XmlElement(name = "EnviarLoteRpsEnvio", namespace = "http://producao.issfortaleza.com.br", required = true)
    protected String enviarLoteRpsEnvio;

    /**
     * Gets the value of the cabecalho property.
     *
     * @return possible object is {@link String }
     */
    public String getCabecalho() {
        return cabecalho;
    }

    /**
     * Sets the value of the cabecalho property.
     *
     * @param value allowed object is {@link String }
     */
    public void setCabecalho(String value) {
        this.cabecalho = value;
    }

    /**
     * Gets the value of the enviarLoteRpsEnvio property.
     *
     * @return possible object is {@link String }
     */
    public String getEnviarLoteRpsEnvio() {
        return enviarLoteRpsEnvio;
    }

    /**
     * Sets the value of the enviarLoteRpsEnvio property.
     *
     * @param value allowed object is {@link String }
     */
    public void setEnviarLoteRpsEnvio(String value) {
        this.enviarLoteRpsEnvio = value;
    }
}
