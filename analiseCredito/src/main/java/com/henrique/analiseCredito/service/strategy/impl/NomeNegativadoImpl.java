package com.henrique.analiseCredito.service.strategy.impl;

import com.henrique.analiseCredito.constantes.MensagemConstante;
import com.henrique.analiseCredito.domain.Proposta;
import com.henrique.analiseCredito.exceptions.StrategyException;
import com.henrique.analiseCredito.service.strategy.CalculoPonto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Random;

@Order(1)
@Slf4j
@Component
public class NomeNegativadoImpl implements CalculoPonto {
    @Override
    public int calcular(Proposta proposta) {
        if (nomeNegativado()) {
            log.error("O NOME {} ESTA NEGATIVADO", proposta.getUsuario().getNome());
            throw new StrategyException(String.format(MensagemConstante.CLIENTE_NEGATIVADO, proposta.getUsuario().getNome()));
        }
        return 100;
    }

    private boolean nomeNegativado() {
        return new Random().nextBoolean();
    }
}
