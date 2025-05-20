package com.notafacil.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;

import java.time.Duration;

@Configuration
public class SoapClientConfig {

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        // Em vez de setContextPath, use setPackagesToScan:
        marshaller.setPackagesToScan(
                "com.notafacil.schemas",
                "com.notafacil.wswrapper"
        );
        return marshaller;
    }

    @Bean
    public WebServiceTemplate webServiceTemplate(Jaxb2Marshaller marshaller,
                                                 @Value("${nfse.service.url}") String uri) {
        WebServiceTemplate template = new WebServiceTemplate();
        template.setMarshaller(marshaller);
        template.setUnmarshaller(marshaller);
        template.setDefaultUri(uri);
        var sender = new HttpUrlConnectionMessageSender();
        sender.setConnectionTimeout(Duration.ofSeconds(5000));
        sender.setReadTimeout(Duration.ofSeconds(15000));
        template.setMessageSender(sender);
        return template;
    }
}
