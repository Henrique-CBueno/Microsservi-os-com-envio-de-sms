package com.henrique.analiseCredito.service.strategy.impl;

import com.henrique.analiseCredito.domain.Proposta;
import com.henrique.analiseCredito.service.strategy.CalculoPonto;
import org.springframework.stereotype.Component;

@Component
public class PrazoPagamentoMenorDezAnosImpl implements CalculoPonto {
    @Override
    public int calcular(Proposta proposta) {
        return proposta.getPrazoPagamento() < 120 ? 80 : 0;
    }
}
