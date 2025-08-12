package br.com.notafacil.mapping;

import br.com.notafacil.dto.*;
import br.com.notafacil.dto.prestador.PrestadorDto;
import br.com.notafacil.dto.tomador.ContatoDto;
import br.com.notafacil.dto.tomador.EnderecoDto;
import br.com.notafacil.dto.tomador.IdentificacaoTomadorDto;
import br.com.notafacil.dto.tomador.TomadorDto;
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
}