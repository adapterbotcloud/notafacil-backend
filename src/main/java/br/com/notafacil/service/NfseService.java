package br.com.notafacil.service;

import br.com.notafacil.dto.*;
import br.com.notafacil.entity.RpsEntity;
import br.com.notafacil.schemas.*;
import br.com.notafacil.mapping.NfseMapper;
import br.com.notafacil.wsdl.ServiceGinfesImplServiceService;
import br.com.notafacil.wsdl.ServiceGinfes;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import java.util.concurrent.atomic.AtomicLong; // importe o AtomicLong aqui

@Service
public class NfseService {
    private final ServiceGinfes port;
    private final NfseMapper mapper;
    private final JAXBContext jaxbCtx;
    private final AzureVaultXmlSigningService xmlSigner;

    // simula contador em memória; em produção, você usaria uma tabela no BD para garantir unicidade
    private final AtomicLong loteCounter = new AtomicLong(1);
    private final AtomicLong rpsCounter = new AtomicLong(1);

    private static final int MAX_RPS_PER_LOTE = 50;

    public NfseService(NfseMapper mapper, AzureVaultXmlSigningService xmlSigner) throws Exception {
        this.mapper = mapper;
        this.xmlSigner = xmlSigner;

        // 1) Cria o stub do serviço
        ServiceGinfesImplServiceService svc = new ServiceGinfesImplServiceService();
        svc.getServiceGinfes();
        this.port = svc.getServiceGinfes();

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

    private List<List<InfRpsRequestDto>> agruparEmBatches(
            List<InfRpsRequestDto> lista,
            int tamanhoBatch) {

        List<List<InfRpsRequestDto>> batches = new ArrayList<>();
        int total = lista.size();

        for (int inicio = 0; inicio < total; inicio += tamanhoBatch) {
            int fim = Math.min(inicio + tamanhoBatch, total);
            batches.add(lista.subList(inicio, fim));
        }

        return batches;
    }

    public void emitirRpsEmLotes(CabecalhoDto cabecalhoDto, List<InfRpsRequestDto> listaRps) {
        // 1) Divide a lista em lotes de até 50 itens
        List<EnviarLoteRpsEnvioDto> lotesDto = montarLotesParaEnvio(listaRps);

        // 2) Para cada lote, marshal/assinatura e chamada SOAP
        for (EnviarLoteRpsEnvioDto envioDto : lotesDto) {
            recepcionarLote(cabecalhoDto, envioDto);
        }
    }

    private List<EnviarLoteRpsEnvioDto> montarLotesParaEnvio(List<InfRpsRequestDto> listaRequest) {
        List<EnviarLoteRpsEnvioDto> lotesProntos = new ArrayList<>();
        int tamanho = listaRequest.size();
        final int TAMANHO_MAXIMO_LOTE = 50;

        for (int inicio = 0; inicio < tamanho; inicio += TAMANHO_MAXIMO_LOTE) {
            int fim = Math.min(inicio + TAMANHO_MAXIMO_LOTE, tamanho);
            List<InfRpsRequestDto> subLista = listaRequest.subList(inicio, fim);
            lotesProntos.add(montarUmLote(subLista));
        }

        return lotesProntos;
    }

    private EnviarLoteRpsEnvioDto montarUmLote(List<InfRpsRequestDto> listaRequest) {
        List<RpsDto> listaRpsDto = new ArrayList<>();

        for (InfRpsRequestDto req : listaRequest) {
            // Gera identificador sequencial do RPS
            String numeroSequencialRps = String.valueOf(rpsCounter.getAndIncrement());
            // Usa o ano atual como série
            String serie = String.valueOf(LocalDate.now().getYear());
            Integer tipo = 2;
            LocalDateTime dataEmissao = LocalDateTime.now();

            // Mapeia ServicoDto
            ServicoDto servicoDto = new ServicoDto(
                    new ValoresDto(
                            req.servico().valores().valorServicos(),
                            req.servico().valores().valorDeducoes(),
                            req.servico().valores().valorPis(),
                            req.servico().valores().valorCofins(),
                            req.servico().valores().valorInss(),
                            req.servico().valores().valorIr(),
                            req.servico().valores().valorCsll(),
                            req.servico().valores().issRetido(),
                            req.servico().valores().valorIss(),
                            req.servico().valores().valorIssRetido(),
                            req.servico().valores().outrasRetencoes(),
                            req.servico().valores().baseCalculo(),
                            req.servico().valores().aliquota(),
                            req.servico().valores().valorLiquidoNfse(),
                            req.servico().valores().descontoIncondicionado(),
                            req.servico().valores().descontoCondicionado()
                    ),
                    req.servico().itemListaServico(),
                    req.servico().codigoTributacaoMunicipio(),
                    req.servico().discriminacao(),
                    req.servico().codigoMunicipio()
            );

            // Mapeia PrestadorDto
            PrestadorDto prestadorDto = new PrestadorDto(
                    req.prestador().cnpj(),
                    req.prestador().inscricaoMunicipal()
            );

            // Mapeia TomadorDto, cuidando de endereco opcional
            InfRpsRequestDto.TomadorRequest tomadorReq = req.tomador();
            IdentificacaoTomadorDto idTomadorDto = new IdentificacaoTomadorDto(
                    tomadorReq.identificacaoTomador().cnpj(),
                    tomadorReq.identificacaoTomador().inscricaoMunicipal()
            );
            String razaoSocial = tomadorReq.razaoSocial();

            EnderecoDto enderecoDto = null;
            if (tomadorReq.endereco() != null) {
                var e = tomadorReq.endereco();
                enderecoDto = new EnderecoDto(
                        e.endereco(),
                        e.numero(),
                        e.complemento(),
                        e.bairro(),
                        e.codigoMunicipio(),
                        e.uf(),
                        e.cep()
                );
            }

            ContatoDto contatoDto = new ContatoDto(
                    tomadorReq.contato().telefone(),
                    tomadorReq.contato().email()
            );

            TomadorDto tomadorDto = new TomadorDto(
                    idTomadorDto,
                    razaoSocial,
                    enderecoDto,
                    contatoDto
            );

            // Constrói InfRpsDto completo
            InfRpsDto infRpsDto = new InfRpsDto(
                    new IdentificacaoRpsDto(numeroSequencialRps, serie, tipo),
                    dataEmissao,
                    req.naturezaOperacao(),
                    req.regimeEspecialTributacao(),
                    req.optanteSimplesNacional(),
                    req.incentivadorCultural(),
                    req.status(),
                    servicoDto,
                    prestadorDto,
                    tomadorDto
            );

            listaRpsDto.add(new RpsDto(infRpsDto, req.id()));
        }

        // Monta o objeto LoteRpsDto a partir da sublista
        long idLote = loteCounter.getAndIncrement();
        String numeroLoteString = String.format("%04d", idLote);
        Integer quantidadeRps = listaRpsDto.size();
        String cnpjPrestador = listaRequest.get(0).prestador().cnpj();
        String inscricaoPrestador = listaRequest.get(0).prestador().inscricaoMunicipal();

        LoteRpsDto loteDto = new LoteRpsDto(
                idLote,
                numeroLoteString,
                cnpjPrestador,
                inscricaoPrestador,
                quantidadeRps,
                listaRpsDto
        );

        return new EnviarLoteRpsEnvioDto(loteDto);
    }

    /**
     * Persiste em banco todos os RPS contidos no lote,
     * mapeando absolutamente todos os campos de InfRpsDto e Tomador/Prestador.
     */
    @Transactional
    private void persistirRps(EnviarLoteRpsEnvioDto envioDto) {
      /*  LoteRpsDto lote = envioDto.loteRps();
        List<RpsEntity> entidades = new ArrayList<>(lote.listaRps().size());

        for (RpsDto rpsDto : lote.listaRps()) {
            InfRpsDto inf = rpsDto.infRps();

            // Cria e popula a entidade
            RpsEntity e = new RpsEntity();
            // ID de negócio (mensalidade) — você deve incluir esse campo em RpsDto
            //e.setRequestId(rpsDto.());

            // Identificação do RPS
            e.setNumero(inf.identificacaoRps().numero());
            e.setSerie(inf.identificacaoRps().serie());
            e.setTipo(inf.identificacaoRps().tipo());
            e.setDataEmissao(inf.dataEmissao());

            // Campos de operação
            e.setNaturezaOperacao(inf.naturezaOperacao());
            e.setRegimeEspecialTributacao(inf.regimeEspecialTributacao());
            e.setOptanteSimplesNacional(inf.optanteSimplesNacional());
            e.setIncentivadorCultural(inf.incentivadorCultural());
            e.setStatus(inf.status());

            // Valores do serviço
            ValoresDto v = inf.servico().valores();
            e.setValorServicos(v.valorServicos());
            e.setValorDeducoes(v.valorDeducoes());
            e.setValorPis(v.valorPis());
            e.setValorCofins(v.valorCofins());
            e.setValorInss(v.valorInss());
            e.setValorIr(v.valorIr());
            e.setValorCsll(v.valorCsll());
            e.setIssRetido(v.issRetido());
            e.setValorIss(v.valorIss());
            e.setValorIssRetido(v.valorIssRetido());
            e.setOutrasRetencoes(v.outrasRetencoes());
            e.setBaseCalculo(v.baseCalculo());
            e.setAliquota(v.aliquota());
            e.setValorLiquidoNfse(v.valorLiquidoNfse());
            e.setDescontoIncondicionado(v.descontoIncondicionado());
            e.setDescontoCondicionado(v.descontoCondicionado());

            // Dados do prestador
            PrestadorDto p = inf.prestador();
            e.setPrestadorCnpj(p.cnpj());
            e.setPrestadorInscricaoMunicipal(p.inscricaoMunicipal());

            // Dados do tomador
            TomadorDto t = inf.tomador();
            e.setTomadorCnpj(t.identificacaoTomador().cnpj());
            e.setTomadorInscricaoMunicipal(t.identificacaoTomador().inscricaoMunicipal());
            e.setTomadorRazaoSocial(t.razaoSocial());

            // Endereço do tomador (opcional)
            if (t.endereco() != null) {
                EnderecoDto end = t.endereco();
                e.setTomadorEndereco(end.endereco());
                e.setTomadorNumero(end.numero());
                e.setTomadorComplemento(end.complemento());
                e.setTomadorBairro(end.bairro());
                e.setTomadorCodigoMunicipio(end.codigoMunicipio());
                e.setTomadorUf(end.uf());
                e.setTomadorCep(end.cep());
            }

            // Contato do tomador
            ContatoDto c = t.contato();
            e.setTomadorTelefone(c.telefone());
            e.setTomadorEmail(c.email());

            entidades.add(e);
        }

        // Salva todos em batch
        rpsRepository.saveAll(entidades);*/
    }



}

