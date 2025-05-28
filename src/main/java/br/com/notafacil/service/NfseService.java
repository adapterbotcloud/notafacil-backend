package br.com.notafacil.service;

import br.com.notafacil.dto.CabecalhoDto;
import br.com.notafacil.dto.EnviarLoteRpsEnvioDto;
import br.com.notafacil.schemas.*;
import br.com.notafacil.schemas.*;
import br.com.notafacil.mapping.NfseMapper;
import br.com.notafacil.wsdl.ServiceGinfesImplServiceService;
import br.com.notafacil.wsdl.ServiceGinfes;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.stereotype.Service;

import jakarta.xml.ws.BindingProvider;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.UUID;

@Service
public class NfseService {
    private final ServiceGinfes port;
    private final NfseMapper mapper;
    private final JAXBContext jaxbCtx;
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

    public EnviarLoteRpsResposta recepcionarLote(CabecalhoDto cabecalhoDto,
                                                 EnviarLoteRpsEnvioDto enviarLoteRpsEnvioDto) {
        try {

            // 1) Gera o UUID para o atributo Id do LoteRps
            String loteId = "lote-" + UUID.randomUUID();
            // --- Marshaling do cabeçalho ---
            Marshaller m = jaxbCtx.createMarshaller();
            m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            StringWriter hdrSw = new StringWriter();
            Cabecalho cabecalho = new Cabecalho();
            cabecalho.setVersao(cabecalhoDto.versao());
            cabecalho.setVersaoDados(cabecalhoDto.versaoDados());
            m.marshal(cabecalho, hdrSw);
            String cabecalhoXml = hdrSw.toString();

            // --- Marshaling do corpo (EnviarLoteRpsEnvio) ---
            StringWriter bodySw = new StringWriter();
            EnviarLoteRpsEnvio envioJaxb = mapper.toSchema(enviarLoteRpsEnvioDto,loteId);
            m.marshal(envioJaxb, bodySw);
            String unsignedXml = bodySw.toString();

            // 3) XML → XML Assinado
            String bodyXml = xmlSigner.signXml(unsignedXml,loteId);

            // --- Chamada RPC via JAX-WS stub ---
            String respostaXml = port.recepcionarLoteRpsV3(cabecalhoXml, bodyXml);

            // --- Unmarshal da resposta ---
            Unmarshaller u = jaxbCtx.createUnmarshaller();
            return (EnviarLoteRpsResposta) u.unmarshal(new StringReader(respostaXml));

        } catch (Exception e) {
            throw new RuntimeException("Erro ao chamar RecepcionarLoteRpsV3 via JAX-WS", e);
        }
    }

    public ConsultarSituacaoLoteRpsResposta consultarSituacaoLoteRps(
            CabecalhoDto cabecalhoDto,
            String protocolo) {
        try {
            // 1) monta o XML de cabeçalho
            Marshaller m = jaxbCtx.createMarshaller();
            m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            StringWriter hdrSw = new StringWriter();
            Cabecalho hdrJaxb = new Cabecalho();
            hdrJaxb.setVersao(cabecalhoDto.versao());
            hdrJaxb.setVersaoDados(cabecalhoDto.versaoDados());
            m.marshal(hdrJaxb, hdrSw);
            String cabecalhoXml = hdrSw.toString();

            // 2) monta o objeto de envio de acordo com o XSD
            ConsultarSituacaoLoteRpsEnvio envio = new ConsultarSituacaoLoteRpsEnvio();
            //   <Prestador>
            TcIdentificacaoPrestador tp = new TcIdentificacaoPrestador();
            tp.setCnpj("27288254000103");
            tp.setInscricaoMunicipal("469159");
            envio.setPrestador(tp);
            //   <Protocolo>
            envio.setProtocolo(protocolo);
            //   (opcional) assinatura <dsig:Signature>
            //        se precisar, chamamos xmlSigner.signXmlWithId()

            // 3) marshalling do body
            StringWriter bodySw = new StringWriter();
            m.marshal(envio, bodySw);
            String unsignedBodyXml = bodySw.toString();

            // 4) assinar o body (caso queira)
            String signedBodyXml = xmlSigner.signXml(unsignedBodyXml);

            // 5) chamada ao stub JAX-WS
            String respostaXml = port.consultarSituacaoLoteRpsV3(cabecalhoXml, signedBodyXml);

            // 6) unmarshal da resposta em objeto
            Unmarshaller u = jaxbCtx.createUnmarshaller();
            return (ConsultarSituacaoLoteRpsResposta)
                    u.unmarshal(new StringReader(respostaXml));

        } catch (Exception e) {
            throw new RuntimeException("Erro ao chamar ConsultarSituacaoLoteRpsV3 via JAX-WS", e);
        }
    }

    public ConsultarLoteRpsResposta consultarLoteRps(
            CabecalhoDto cabecalhoDto,
            String protocolo) {

        try {
            // 1) Monta cabeçalho
            Marshaller m = jaxbCtx.createMarshaller();
            m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            StringWriter hdrSw = new StringWriter();
            Cabecalho hdrJaxb = new Cabecalho();
            hdrJaxb.setVersao(cabecalhoDto.versao());
            hdrJaxb.setVersaoDados(cabecalhoDto.versaoDados());
            m.marshal(hdrJaxb, hdrSw);
            String cabecalhoXml = hdrSw.toString();

            // 2) Monta body de envio (ConsultarLoteRpsEnvio)
            ConsultarLoteRpsEnvio envio = new ConsultarLoteRpsEnvio();
            // 2.1) Prestador
            TcIdentificacaoPrestador tp = new TcIdentificacaoPrestador();
            tp.setCnpj("27288254000103");
            tp.setInscricaoMunicipal("469159");
            envio.setPrestador(tp);
            // 2.2) Protocolo
            envio.setProtocolo(protocolo);

            // 3) Marshal do body
            StringWriter bodySw = new StringWriter();
            m.marshal(envio, bodySw);
            String unsignedBodyXml = bodySw.toString();

            // 4) Assina o body usando o protocolo como Id
            String signedBodyXml = xmlSigner.signXml(unsignedBodyXml);

            // 5) Chama o stub JAX-WS
            //    supondo que o método no port se chame consultarLoteRpsV3
            String respostaXml = port.consultarLoteRpsV3(cabecalhoXml, signedBodyXml);

            // 6) Unmarshal da resposta em objeto
            Unmarshaller u = jaxbCtx.createUnmarshaller();
            return (ConsultarLoteRpsResposta)
                    u.unmarshal(new StringReader(respostaXml));

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao chamar ConsultarLoteRpsV3 via JAX-WS", e);
        }
    }

}

