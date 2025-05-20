package com.notafacil.service;

import com.notafacil.dto.CabecalhoDto;
import com.notafacil.dto.EnviarLoteRpsEnvioDto;
import com.notafacil.mapping.NfseMapper;
import com.notafacil.schemas.*;
import com.notafacil.wswrapper.RecepcionarLoteRpsV3;
import com.notafacil.wswrapper.RecepcionarLoteRpsV3Response;
import jakarta.xml.bind.JAXBElement;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

@Service
public class NfseService {
    private static final String WS_ACTION_RECEPCIONAR =
            "http://producao.issfortaleza.com.br/RecepcionarLoteRpsV3";

    private final WebServiceTemplate wsTemplate;
    private final Jaxb2Marshaller marshaller;
    private final XmlSigningService xmlSigner;
    private final NfseMapper mapper;

    public NfseService(WebServiceTemplate wsTemplate,
                       Jaxb2Marshaller marshaller,
                       XmlSigningService xmlSigner,
                       NfseMapper mapper) {
        this.wsTemplate = wsTemplate;
        this.marshaller = marshaller;
        this.xmlSigner = xmlSigner;
        this.mapper = mapper;
    }

    public EnviarLoteRpsResposta recepcionarLoteRps(EnviarLoteRpsEnvioDto dto) {
        try {
            // 1) DTO → JAXB
            EnviarLoteRpsEnvio jaxbReq = mapper.toSchema(dto);

            // 2) JAXB → String XML
            StringWriter sw = new StringWriter();
            marshaller.marshal(jaxbReq, new StreamResult(sw));
            String unsignedXml = sw.toString();

            // 3) XML → XML Assinado
            String signedXml = xmlSigner.signXml(unsignedXml);

            // 4) Cria Source a partir do XML assinado
            StreamSource requestSource = new StreamSource(new StringReader(signedXml));

            // 5) Envia e recebe T = EnviarLoteRpsResposta
            EnviarLoteRpsResposta response = wsTemplate.sendSourceAndReceive(
                    requestSource,
                    null,
                    message -> {
                        Source payload = ((SoapMessage) message).getSoapBody().getPayloadSource();
                        return (EnviarLoteRpsResposta) marshaller.unmarshal(payload);
                    }
            );

            return response;
        } catch (SoapFaultClientException e) {
            SoapFault fault = e.getSoapFault();
            throw new RuntimeException(
                    "SOAP Fault: " + fault.getFaultCode().getLocalPart()
                            + " - " + e.getFaultStringOrReason(), e
            );
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao assinar/enviar XML: " + ex.getMessage(), ex);
        }
    }

    public EnviarLoteRpsResposta recepcionarLoteRps(
            CabecalhoDto cabecalhoDto,
            EnviarLoteRpsEnvioDto bodyDto) {

        try {
            // --- 1) serializa o CABEÇALHO em XML String
            Cabecalho jaxbHdr = new Cabecalho();
            jaxbHdr.setVersao(cabecalhoDto.versao());
            jaxbHdr.setVersaoDados(cabecalhoDto.versaoDados());
            StringWriter swHdr = new StringWriter();
            marshaller.marshal(jaxbHdr, new StreamResult(swHdr));
            String xmlHdr = swHdr.toString();

            // --- 2) serializa o CORPO (EnviarLoteRpsEnvio) e assina
            EnviarLoteRpsEnvio jaxbBody = mapper.toSchema(bodyDto);
            StringWriter swBody = new StringWriter();
            marshaller.marshal(jaxbBody, new StreamResult(swBody));
            String signedBody = xmlSigner.signXml(swBody.toString());

            // --- 3) monta o wrapper RPC‐literal com as duas Strings
            RecepcionarLoteRpsV3 rpcReq = new RecepcionarLoteRpsV3();
            rpcReq.setCabecalho(xmlHdr);
            rpcReq.setEnviarLoteRpsEnvio(signedBody);

            // --- 4) envia e recebe o response wrapper
            RecepcionarLoteRpsV3Response rpcResp =
                    (RecepcionarLoteRpsV3Response) wsTemplate
                            .marshalSendAndReceive(rpcReq);

            // --- 5) extrai a String XML de resposta e desserializa em JAXB
            String xmlResp = rpcResp.getEnviarLoteRpsResposta();
            return (EnviarLoteRpsResposta) marshaller
                    .unmarshal(new StreamSource(new StringReader(xmlResp)));

        } catch (SoapFaultClientException e) {
            throw new RuntimeException(
                    "SOAP Fault: " +
                            e.getFaultCode().getLocalPart() +
                            " – " + e.getFaultStringOrReason(), e
            );
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Erro ao montar/enviar RecepcionarLoteRpsV3", ex
            );
        }
    }

    private String toXmlString(Object jaxbObject) throws IOException {
        StringWriter sw = new StringWriter();
        marshaller.marshal(jaxbObject, new StreamResult(sw));
        return sw.toString();
    }

}

