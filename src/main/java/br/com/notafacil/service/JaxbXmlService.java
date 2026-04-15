package br.com.notafacil.service;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.StringWriter;

import org.springframework.stereotype.Component;

/** Marshal/Unmarshal de objetos JAXB gerados do pacote br.com.notafacil.schemas */
@Component
public class JaxbXmlService {

    // Contexto para TODO o pacote de schemas (rápido e cacheado)
    private final JAXBContext ctx;

    public JaxbXmlService() {
        try {
            this.ctx = JAXBContext.newInstance("br.com.notafacil.schemas:br.com.notafacil.schemas.v4");
        } catch (JAXBException e) {
            throw new IllegalStateException("Falha ao inicializar JAXBContext", e);
        }
    }

    public String marshal(Object jaxbObject) {
        try {
            Marshaller m = ctx.createMarshaller();
            m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE); // sem <?xml ...?>
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
