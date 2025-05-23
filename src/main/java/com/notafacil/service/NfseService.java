package com.notafacil.service;

import com.notafacil.dto.CabecalhoDto;
import com.notafacil.dto.EnviarLoteRpsEnvioDto;
import com.notafacil.schemas.Cabecalho;
import com.notafacil.schemas.EnviarLoteRpsEnvio;
import com.notafacil.schemas.EnviarLoteRpsResposta;
import com.notafacil.mapping.NfseMapper;
import com.notafacil.wsdl.ServiceGinfesImplServiceService;
import com.notafacil.wsdl.ServiceGinfes;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.stereotype.Service;

import jakarta.xml.ws.BindingProvider;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

@Service
public class NfseService {
    private final ServiceGinfes port;
    private final NfseMapper mapper;
    private final JAXBContext jaxbCtx;
    //private final XmlSigningService xmlSigner;
    private final AzureVaultXmlSigningService xmlSigner;

    public NfseService(NfseMapper mapper, AzureVaultXmlSigningService xmlSigner) throws Exception {
        this.mapper = mapper;
        this.xmlSigner = xmlSigner;

        // 1) Cria o stub do serviço
        ServiceGinfesImplServiceService svc = new ServiceGinfesImplServiceService();
        svc.getServiceGinfes();
        this.port = svc.getServiceGinfes();

        // (Opcional) se quiser sobrescrever a URL em runtime:
        Map<String,Object> rc = ((BindingProvider)port).getRequestContext();
        rc.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                "http://isshomo.sefin.fortaleza.ce.gov.br/grpfor-iss/ServiceGinfesImplService");

        // 2) Prepara o JAXBContext com os schemas das classes geradas
        this.jaxbCtx = JAXBContext.newInstance(
                Cabecalho.class,
                EnviarLoteRpsEnvio.class,
                EnviarLoteRpsResposta.class
        );
    }

    public EnviarLoteRpsResposta recepcionarLote(CabecalhoDto hdrDto,
                                                 EnviarLoteRpsEnvioDto bodyDto) {
        try {
            // --- Marshaling do cabeçalho ---
            Marshaller m = jaxbCtx.createMarshaller();
            m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            StringWriter hdrSw = new StringWriter();
            Cabecalho hdrJaxb = new Cabecalho();
            hdrJaxb.setVersao(hdrDto.versao());
            hdrJaxb.setVersaoDados(hdrDto.versaoDados());
            m.marshal(hdrJaxb, hdrSw);
            String cabecalhoXml = hdrSw.toString();

            // --- Marshaling do corpo (EnviarLoteRpsEnvio) ---
            StringWriter bodySw = new StringWriter();
            EnviarLoteRpsEnvio envioJaxb = mapper.toSchema(bodyDto);
            m.marshal(envioJaxb, bodySw);
            String unsignedXml = bodySw.toString();

            // 3) XML → XML Assinado
            String bodyXml = xmlSigner.signXml(unsignedXml);

            // bodyXml = bodyXml.replaceFirst("^<\\?xml[^>]*\\?>", "");

            // --- Chamada RPC via JAX-WS stub ---
            String respostaXml = port.recepcionarLoteRpsV3(cabecalhoXml, bodyXml);

            // --- Unmarshal da resposta ---
            Unmarshaller u = jaxbCtx.createUnmarshaller();
            return (EnviarLoteRpsResposta) u.unmarshal(new StringReader(respostaXml));

        } catch (Exception e) {
            throw new RuntimeException("Erro ao chamar RecepcionarLoteRpsV3 via JAX-WS", e);
        }
    }

}

