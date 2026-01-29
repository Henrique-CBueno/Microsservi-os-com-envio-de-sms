package com.henrique.propostapp.dto;

public record PropostaCreateResDTO(Long id,
                                   String nome,
                                   String sobrenome,
                                   String telefone,
                                   String cpf,
                                   Double renda,
                                   String valorSolicitadoFmt,
                                   int prazoPagamento,
                                   Boolean aprovada,
                                   String observacao) {
}
