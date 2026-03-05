package br.com.notafacil.service;

import br.com.notafacil.dto.*;
import br.com.notafacil.dto.RpsMinRequestDto;
import br.com.notafacil.entity.EmpresaEntity;
import br.com.notafacil.entity.RpsEntity;
import br.com.notafacil.repository.RpsRepository;
import br.com.notafacil.mapping.NfseMapper;
import br.com.notafacil.wsdl.ServiceGinfes;
import br.com.notafacil.wsdl.ServiceGinfesImplServiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Serviço central de emissão de NFSe.
 * Fluxo SINCRONO: persiste RPS -> enriquece/calcula -> envia em lotes -> atualiza status.
 */
@Service
public class NfseService1 {

    private static final Logger log = LoggerFactory.getLogger(NfseService1.class);

    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final EmpresaService empresaService;
    private final RpsRepository rpsRepository;
    private final NfseMapper mapper;                      // MapStruct/JAXB mapper do projeto
    private final AzureVaultXmlSigningService signer;      // Assinatura XML via Azure Key Vault
    private final ServiceGinfes servicePort;               // Cliente SOAP (port)
    private final JaxbXmlService jaxbXmlService;

    @Value("${nfse.lote.tamanho:50}")
    private int tamanhoBatch;

    public NfseService1(EmpresaService empresaService,
                        RpsRepository rpsRepository,
                        NfseMapper mapper,
                        AzureVaultXmlSigningService signer, JaxbXmlService jaxbXmlService)
    {
        this.empresaService = Objects.requireNonNull(empresaService);
        this.rpsRepository  = Objects.requireNonNull(rpsRepository);
        this.mapper         = Objects.requireNonNull(mapper);
        this.signer         = Objects.requireNonNull(signer);
        this.jaxbXmlService = jaxbXmlService;
        this.servicePort    = new ServiceGinfesImplServiceService().getServiceGinfes();
    }

    /* =========================================================
       ORQUESTRAÇÃO SÍNCRONA (Controller -> este método)
       ========================================================= */
    public EmitirRpsResponse emitirSincrono(final CabecalhoDto cabecalho,
                                            final String empresaCnpj,
                                            final List<RpsMinRequestDto> listaMin) {
        if (listaMin == null || listaMin.isEmpty()) {
            return new EmitirRpsResponse(List.of(), List.of());
        }

        final EmpresaEntity empresa = empresaService.getByCnpjOrThrow(empresaCnpj);

        // 1) Persiste RPS como PENDENTE
        final List<Long> rpsIds = persistirRpsMinimos(empresa, listaMin);

        // 2) Converte/Enriquece e envia em lotes (assina + SOAP)
        final List<RpsEntity> entidades = rpsRepository.findAllById(rpsIds);
        final List<InfRpsRequestDto> completos = toInfRpsList(entidades);
        final List<String> protocolos = emitirRpsEmLotes(cabecalho, completos);

        // 3) Atualiza status/protocolo
        atualizarStatusPosEnvio(rpsIds, protocolos);

        return new EmitirRpsResponse(rpsIds, protocolos);
    }

    /* =========================================================
       PERSISTÊNCIA
       ========================================================= */
    @Transactional
    public List<Long> persistirRpsMinimos(final EmpresaEntity empresa, final List<RpsMinRequestDto> listaMin) {
        final int n = listaMin.size();
        final List<RpsEntity> salvos = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            final RpsMinRequestDto min = listaMin.get(i);

            final RpsEntity rps = new RpsEntity();
            rps.setEmpresa(empresa);

            // mínimos + defaults
            rps.setRequestId(min.id());
            rps.setNumero(gerarNumeroRps());
            rps.setSerie("A");
            rps.setTipo(1);
            rps.setDataEmissao(LocalDateTime.now());

            rps.setNaturezaOperacao(1);
            rps.setRegimeEspecialTributacao(empresa.getRegimeEspecialTributacao());
            rps.setOptanteSimplesNacional(empresa.getOptanteSimplesNacional());
            rps.setIncentivadorCultural(empresa.getIncentivadorCultural());

            rps.setStatus(RpsEntity.Status.PENDENTE);
            rps.setValorServicos(min.servico().valorServicos());

            rps.setTomadorCpf(onlyDigits(min.tomador().cpf()));
            rps.setTomadorRazaoSocial(min.tomador().razaoSocial());
            rps.setDiscriminacao(min.servico().discriminacao());
            rps.setIdCobranca(min.idCobranca());
            rps.setMesCobranca(min.mesCobranca());
            rps.setAnoCobranca(min.anoCobranca());

            salvos.add(rps);
        }

        rpsRepository.saveAllAndFlush(salvos);

        final List<Long> ids = new ArrayList<>(salvos.size());
        for (RpsEntity salvo : salvos) { // CORREÇÃO: Loop aprimorado
            ids.add(salvo.getId());
        }
        return ids;
    }

    private static String gerarNumeroRps() {
        return String.valueOf(System.currentTimeMillis());
    }

    /* =========================================================
       ENRIQUECIMENTO / CÁLCULOS
       ========================================================= */
    @Transactional(readOnly = true)
    public List<InfRpsRequestDto> toInfRpsList(final List<RpsEntity> entidades) {
        final int n = entidades.size();
        final List<InfRpsRequestDto> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            out.add(fromEntityToInfRpsRequest(entidades.get(i)));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public InfRpsRequestDto fromEntityToInfRpsRequest(final RpsEntity base) {
        final EmpresaEntity emp = base.getEmpresa();

        final BigDecimal valorServicos = base.getValorServicos();
        final BigDecimal aliquota      = emp.getAliquota();

        final BigDecimal baseCalculo   = valorServicos;
        final BigDecimal valorIss      = valorServicos.multiply(aliquota);
        final BigDecimal valorLiquido  = valorServicos.subtract(valorIss);

        final InfRpsRequestDto.ServicoRequest.ValoresRequest valoresReq = new InfRpsRequestDto.ServicoRequest.ValoresRequest(
                valorServicos, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO,
                2,
                valorIss,
                ZERO,
                ZERO,
                baseCalculo,
                aliquota,
                valorLiquido,
                ZERO, ZERO
        );

        final InfRpsRequestDto.ServicoRequest servicoReq = new InfRpsRequestDto.ServicoRequest(
                valoresReq,
                emp.getItemListaServico(),
                emp.getCodigoTributacaoMunicipio(),
                base.getDiscriminacao() != null ? base.getDiscriminacao() : "Serviço",
                emp.getCodigoMunicipio()
        );

        final InfRpsRequestDto.PrestadorRequest prestadorReq = new InfRpsRequestDto.PrestadorRequest(
                emp.getCnpj(),
                emp.getInscricaoMunicipal()
        );

        // CORREÇÃO: O construtor esperava 2 argumentos (cnpj, inscricaoMunicipal) e estava recebendo 3.
        final InfRpsRequestDto.TomadorRequest tomadorReq = new InfRpsRequestDto.TomadorRequest(
                new InfRpsRequestDto.TomadorRequest.IdentificacaoTomadorRequest( base.getTomadorCpf(), null),
                base.getTomadorRazaoSocial(), null, null
        );

        return new InfRpsRequestDto(
                base.getId(),
                1,
                emp.getRegimeEspecialTributacao(),
                emp.getOptanteSimplesNacional(),
                emp.getIncentivadorCultural(),
                1,
                servicoReq,
                prestadorReq,
                tomadorReq
        );
    }

    /* =========================================================
       ENVIO EM LOTES (assinatura + SOAP) → protocolos
       ========================================================= */
    public List<String> emitirRpsEmLotes(final CabecalhoDto cabecalho, final List<InfRpsRequestDto> lista) {
        if (lista == null || lista.isEmpty()) return List.of();

        final int batchSize = Math.max(1, tamanhoBatch);
        final List<String> protocolos = new ArrayList<>();

        int i = 0;
        final int total = lista.size();
        while (i < total) {
            final int end = Math.min(i + batchSize, total);
            final List<InfRpsRequestDto> batch = lista.subList(i, end);

            final LoteRpsDto lote = montarLoteDtoAPartirDoBatch(batch);
            // CORREÇÃO: O construtor de EnviarLoteRpsEnvioDto esperava apenas 1 argumento (o lote).
            // O cabeçalho deve ser tratado dentro do seu mapper ou em outra parte da lógica de envio.
            final EnviarLoteRpsEnvioDto envio = new EnviarLoteRpsEnvioDto(lote);

            try {
                // CORREÇÃO: O método 'marshalEnviarLote' não foi encontrado. Substituído por 'toXml'.
                // Verifique o nome correto do método em sua interface NfseMapper.
                String xmlNaoAssinado = jaxbXmlService.marshal(
                        mapper.toSchema(envio)   // DTO -> objeto JAXB
                );

                final String xmlAssinado = signer.signXml(xmlNaoAssinado);

                // CORREÇÃO: O método 'enviarLoteRpsV3' não foi encontrado. O método padrão do Ginfes é 'recepcionarLoteRpsV3'.
                // Verifique o nome e os parâmetros corretos na sua classe ServiceGinfes gerada.
                final String resposta = servicePort.recepcionarLoteRpsV3(cabecalho.toString(), xmlAssinado); // Exemplo de uso do cabecalho

                final String protocolo = extrairProtocolo(resposta);
                if (protocolo == null || protocolo.isBlank()) {
                    throw new IllegalStateException("Protocolo não retornado pelo provedor. Resposta: " + resposta);
                }
                protocolos.add(protocolo);
                log.debug("Lote enviado com sucesso. protocolo={}", protocolo);
            } catch (Exception e) {
                log.error("Falha no envio do lote ({}..{}): {}", i, end - 1, e.getMessage(), e);
                throw new RuntimeException("Falha ao enviar lote NFSe: " + e.getMessage(), e);
            }

            i = end;
        }

        return protocolos;
    }

    private LoteRpsDto montarLoteDtoAPartirDoBatch(final List<InfRpsRequestDto> batch) {
        final InfRpsRequestDto first = batch.get(0);
        final String numeroLote = gerarNumeroLote();

        final int n = batch.size();
        final List<RpsDto> rpsList = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            rpsList.add(toRpsDto(batch.get(i)));
        }

        // CORREÇÃO: O construtor de LoteRpsDto esperava 6 argumentos, incluindo um Long id no início.
        // Adicionado 'null' para o id.
        return new LoteRpsDto(
                null, // id do lote
                numeroLote,
                first.prestador().cnpj(),
                first.prestador().inscricaoMunicipal(),
                n,
                rpsList
        );
    }

    private static String gerarNumeroLote() {
        return "1" + System.currentTimeMillis();
    }

    private RpsDto toRpsDto(final InfRpsRequestDto req) {
        final InfRpsDto inf = new InfRpsDto(
                new IdentificacaoRpsDto(
                        req.id().toString(),
                        "A",
                        1
                ),
                java.time.LocalDateTime.now(),
                req.naturezaOperacao(),
                req.regimeEspecialTributacao(),
                req.optanteSimplesNacional(),
                req.incentivadorCultural(),
                req.status(),
                new ServicoDto(
                        // CORREÇÃO: Os valores de serviço estão em um objeto aninhado 'valores'.
                        // O acesso foi corrigido de req.servico().{campo} para req.servico().valores().{campo}.
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
                ),
                new PrestadorDto(req.prestador().cnpj(), req.prestador().inscricaoMunicipal()),
                new TomadorDto(
                        new IdentificacaoTomadorDto(
                                req.tomador().identificacaoTomador().cpf(),
                                null,
                                req.tomador().identificacaoTomador().inscricaoMunicipal()
                        ),
                        req.tomador().razaoSocial(),
                        null, null
                )
        );
        // CORREÇÃO: O construtor de RpsDto esperava 2 argumentos (InfRpsDto e um Long).
        // Adicionado 'null' como segundo argumento.
        return new RpsDto(inf, null);
    }

    /* =========================================================
       ATUALIZA STATUS / PROTOCOLO
       ========================================================= */
    @Transactional
    public void atualizarStatusPosEnvio(final List<Long> rpsIds, final List<String> protocolos) {
        final List<RpsEntity> list = rpsRepository.findAllById(rpsIds);
        final String protocolo = protocolos.isEmpty() ? null : protocolos.get(0);

        for (RpsEntity rps : list) { // CORREÇÃO: Loop aprimorado
            rps.setStatus(RpsEntity.Status.ENVIADO);
            rps.setProtocolo(protocolo);
            rps.setMensagemErro(null);
        }
        rpsRepository.saveAll(list);
    }

    /* =========================================================
       UTILITÁRIOS
       ========================================================= */
    private static String onlyDigits(final String s) {
        return s == null ? null : s.replaceAll("\\D+", "");
    }

    private static final Pattern PROTOCOLO_TAG = Pattern.compile("<(?:ns2:)?Protocolo>([^<]+)</(?:ns2:)?Protocolo>");

    private static String extrairProtocolo(final Object resposta) {
        if (resposta == null) return null;

        if (resposta instanceof String xml) {
            final Matcher m = PROTOCOLO_TAG.matcher(xml);
            if (m.find()) return m.group(1).trim();
            return null;
        }

        // if (resposta instanceof EnviarLoteRpsResposta r) { // Adapte para sua classe de resposta JAXB
        //     return r.getNumeroProtocolo();
        // }

        return null;
    }
}