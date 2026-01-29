package com.henrique.analiseCredito.service.strategy.impl;

import com.henrique.analiseCredito.domain.Proposta;
import com.henrique.analiseCredito.service.strategy.CalculoPonto;
import org.springframework.stereotype.Component;

@Component
public class RendaMaiorValorSolicitadoImpl implements CalculoPonto {
    @Override
    public int calcular(Proposta proposta) {
        return rendaMaiorQueValorSolicitado(proposta) ? 100 : 0;
    }

    private boolean rendaMaiorQueValorSolicitado(Proposta proposta) {
        return
                proposta.getUsuario().getRenda() > proposta.getValorSolicitado();
    }
}
