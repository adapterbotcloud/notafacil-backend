package br.com.notafacil.mapping;

import br.com.notafacil.dto.*;
import br.com.notafacil.schemas.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import java.util.List;

@Mapper(componentModel = "spring")
public interface NfseMapper {
    @Mapping(target = "loteRps.listaRps", source = "loteRps.listaRps", qualifiedByName = "mapListaRps")
    EnviarLoteRpsEnvio toSchema(EnviarLoteRpsEnvioDto dto);

    default EnviarLoteRpsEnvio toSchema(EnviarLoteRpsEnvioDto dto, String correlationId) {
        EnviarLoteRpsEnvio envio = toSchema(dto);
        if (envio.getLoteRps() != null) {
            envio.getLoteRps().setId(correlationId);
        }
        return envio;
    }

    @Mapping(target = "listaRps", source = "listaRps", qualifiedByName = "mapListaRps")
    TcLoteRps toSchema(LoteRpsDto dto);
    TcRps toSchema(RpsDto dto);
    TcInfRps toSchema(InfRpsDto dto);
    TcIdentificacaoRps toSchema(IdentificacaoRpsDto dto);
    TcDadosServico toSchema(ServicoDto dto);
    TcValores toSchema(ValoresDto dto);
    TcDadosPrestador toSchema(PrestadorDto dto);
    TcDadosTomador toSchema(TomadorDto dto);
    @Mapping(target = "cpfCnpj", source = "dto", qualifiedByName = "toCpfCnpj")
    @Mapping(target = "inscricaoMunicipal", source = "inscricaoMunicipal")
    TcIdentificacaoTomador toSchema(IdentificacaoTomadorDto dto);
    TcEndereco toSchema(EnderecoDto dto);
    TcContato toSchema(ContatoDto dto);
    Cabecalho toSchema(CabecalhoDto dto);

    @Named("mapListaRps")
    default TcLoteRps.ListaRps mapListaRps(List<RpsDto> lista) {
        TcLoteRps.ListaRps wrapper = new TcLoteRps.ListaRps();
        for (RpsDto rpsDto : lista) {
            wrapper.getRps().add(toSchema(rpsDto));
        }
        return wrapper;
    }

    @Named("toCpfCnpj")
    default TcCpfCnpj toCpfCnpj(IdentificacaoTomadorDto dto) {
        if (dto == null) return null;

        String cpf  = onlyDigits(dto.cpf());
        String cnpj = onlyDigits(dto.cnpj());

        if (isNotBlank(cpf)) {
            if (cpf.length() != 11)
                throw new IllegalArgumentException("CPF inválido (11 dígitos): " + dto.cpf());
            TcCpfCnpj out = new TcCpfCnpj();
            out.setCpf(cpf);     // <<< AQUI (string simples)
            return out;
        }

        if (isNotBlank(cnpj)) {
            if (cnpj.length() != 14)
                throw new IllegalArgumentException("CNPJ inválido (14 dígitos): " + dto.cnpj());
            TcCpfCnpj out = new TcCpfCnpj();
            out.setCnpj(cnpj);   // <<< AQUI (string simples)
            return out;
        }

        return null; // minOccurs=0; ajuste se quiser forçar presença de um dos dois
    }

    private static String onlyDigits(String s) {
        return s == null ? null : s.replaceAll("\\D+", "");
    }
    private static boolean isNotBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}