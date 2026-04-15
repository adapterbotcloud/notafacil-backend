package br.com.notafacil.schemas.v4;

import br.com.notafacil.dto.IbsCbsDto;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * XmlAdapter que serializa IbsCbsDto → fragmento XML String
 * para injeção no SOAP sem precisar de JAXB gerado pros v4 types.
 */
public class IbsCbsXmlAdapter extends XmlAdapter<String, IbsCbsDto> {

    private static final String NS = "http://www.ginfes.com.br/tipos_v03.xsd";

    @Override
    public IbsCbsDto unmarshal(String v) {
        // Não usado — deserialização não necessária por ora
        return null;
    }

    @Override
    public String marshal(IbsCbsDto dto) {
        if (dto == null) return null;

        StringBuilder xml = new StringBuilder();
        xml.append("<ibsCbs xmlns=\"").append(NS).append("\">");

        // Campos do bloco IbsCbs
        if (dto.codigoIndicadorFinalidadeNFSe() != null) {
            xml.append("<CodigoIndicadorFinalidadeNFSe>")
               .append(dto.codigoIndicadorFinalidadeNFSe())
               .append("</CodigoIndicadorFinalidadeNFSe>");
        }
        if (dto.codigoIndicadorOperacaoUsoConsumoPessoal() != null) {
            xml.append("<CodigoIndicadorOperacaoUsoConsumoPessoal>")
               .append(dto.codigoIndicadorOperacaoUsoConsumoPessoal())
               .append("</CodigoIndicadorOperacaoUsoConsumoPessoal>");
        }
        if (dto.codigoIndicadorOperacao() != null) {
            xml.append("<CodigoIndicadorOperacao>")
               .append(dto.codigoIndicadorOperacao())
               .append("</CodigoIndicadorOperacao>");
        }
        if (dto.tipoOp() != null) {
            xml.append("<TipoOp>").append(dto.tipoOp()).append("</TipoOp>");
        }

        // NFSe Referenciadas
        if (dto.nfseReferenciadas() != null && !dto.nfseReferenciadas().isEmpty()) {
            xml.append("<GrupoNFSeReferenciada>");
            for (String chave : dto.nfseReferenciadas()) {
                xml.append("<ChaveNFSeReferenciada>").append(chave).append("</ChaveNFSeReferenciada>");
            }
            xml.append("</GrupoNFSeReferenciada>");
        }

        if (dto.tipoEnteGovernamental() != null) {
            xml.append("<TipoEnteGovernamental>").append(dto.tipoEnteGovernamental()).append("</TipoEnteGovernamental>");
        }
        if (dto.indDest() != null) {
            xml.append("<IndDest>").append(dto.indDest()).append("</IndDest>");
        }

        // Valores → Tributos IBS/CBS
        xml.append("<Valores>");
        xml.append("<TributosIbsCbs>");
        xml.append("<GrupoIbsCbs>");

        if (dto.cst() != null) {
            xml.append("<CST>").append(dto.cst()).append("</CST>");
        }
        if (dto.codigoClassTrib() != null) {
            xml.append("<CodigoClassTrib>").append(dto.codigoClassTrib()).append("</CodigoClassTrib>");
        }
        if (dto.codigoCreditoPresumido() != null) {
            xml.append("<CodigoCreditoPresumido>").append(dto.codigoCreditoPresumido()).append("</CodigoCreditoPresumido>");
        }

        xml.append("</GrupoIbsCbs>");
        xml.append("</TributosIbsCbs>");
        xml.append("</Valores>");
        xml.append("</ibsCbs>");

        return xml.toString();
    }
}
