package br.com.notafacil.service;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.StringWriter;

import org.springframework.stereotype.Component;

import br.com.notafacil.schemas.EnviarLoteRpsEnvio;

/** Marshal/Unmarshal de objetos JAXB gerados do pacote V3. V4 usa string builder. */
@Component
public class JaxbXmlService {

    private final JAXBContext ctxV3;

    public JaxbXmlService() {
        try {
            this.ctxV3 = JAXBContext.newInstance(EnviarLoteRpsEnvio.class);
        } catch (JAXBException e) {
            throw new IllegalStateException("Falha ao inicializar JAXBContext V3", e);
        }
    }

    public String marshal(Object jaxbObject) {
        try {
            Marshaller m = ctxV3.createMarshaller();
            m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
            m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            try (StringWriter sw = new StringWriter(4096)) {
                m.marshal(jaxbObject, sw);
                return sw.toString();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao gerar XML via JAXB", e);
        }
    }
}