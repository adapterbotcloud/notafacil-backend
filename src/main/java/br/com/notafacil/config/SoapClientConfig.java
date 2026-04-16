package br.com.notafacil.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import br.com.notafacil.schemas.v4.TcDadosIbsCbs;
import br.com.notafacil.schemas.v4.TcDadosServicoV4;
import br.com.notafacil.schemas.v4.TcValoresIbsCbs;


@Configuration
public class SoapClientConfig {

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        // Usa refs diretas às classes em vez de package scanning (evita procurar ObjectFactory/jaxb.index)
        marshaller.setClassesToBeBound(
            TcDadosIbsCbs.class,
            TcDadosServicoV4.class,
            TcValoresIbsCbs.class
        );
        return marshaller;
    }
}