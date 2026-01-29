package com.henrique.propostapp.dto;

public record PropostaCreateReqDTO(String nome,
                                   String sobrenome,
                                   String telefone,
                                   String cpf,
                                   Double renda,
                                   Double valorSolicitado,
                                   int prazoPagamento) {
}
