package com.henrique.propostapp.mapper;

import com.henrique.propostapp.dto.PropostaCreateReqDTO;
import com.henrique.propostapp.dto.PropostaCreateResDTO;
import com.henrique.propostapp.entity.Proposta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Mapper(componentModel = "spring")
public interface PropostaMapper {

    PropostaMapper INSTANCE = Mappers.getMapper(PropostaMapper.class);

    @Mapping(source = "nome", target = "usuario.nome")
    @Mapping(source = "sobrenome", target = "usuario.sobrenome")
    @Mapping(source = "cpf", target = "usuario.cpf")
    @Mapping(source = "telefone", target = "usuario.telefone")
    @Mapping(source = "renda", target = "usuario.renda")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "aprovada", ignore = true)
    @Mapping(target = "integrada", constant = "true")
    @Mapping(target = "observacao", ignore = true)
    Proposta toProposta(PropostaCreateReqDTO propostaCreateReqDTO);


    @Mapping(source = "usuario.nome", target = "nome")
    @Mapping(source = "usuario.sobrenome", target = "sobrenome")
    @Mapping(source = "usuario.telefone", target = "telefone")
    @Mapping(source = "usuario.cpf", target = "cpf")
    @Mapping(source = "usuario.renda", target = "renda")
    @Mapping(expression = "java(setValorSolicitadoFmt(proposta))", target = "valorSolicitadoFmt")
    @Mapping(source = "prazoPagamento", target = "prazoPagamento")
    @Mapping(source = "aprovada", target = "aprovada")
    @Mapping(source = "observacao", target = "observacao")
    PropostaCreateResDTO entityToResDTO(Proposta proposta);

    List<PropostaCreateResDTO> converteListEntityToListDTO(Iterable<Proposta> propostas);

    default String setValorSolicitadoFmt(Proposta proposta) {
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(proposta.getValorSolicitado());
    }

}
