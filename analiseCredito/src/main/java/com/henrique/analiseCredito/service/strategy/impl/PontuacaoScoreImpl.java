package com.henrique.analiseCredito.service.strategy.impl;

import com.henrique.analiseCredito.constantes.MensagemConstante;
import com.henrique.analiseCredito.domain.Proposta;
import com.henrique.analiseCredito.exceptions.StrategyException;
import com.henrique.analiseCredito.service.strategy.CalculoPonto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Random;

@Order(2)
@Slf4j
@Component
public class PontuacaoScoreImpl implements CalculoPonto {

    @Override
    public int calcular(Proposta proposta) {
        int score = score();

        if (score < 200) {
            log.error("SCORE BAIXO {}", score);
            throw new StrategyException(String.format(MensagemConstante.PONTUACAO_SERASA_BAIXA, proposta.getUsuario().getNome()));
        } else if (score <= 400) {
            log.info("SCORE MEDIO {}", score);
            return 150;
        } else if (score <= 600) {
            log.info("SCORE ALTO {}", score);
            return 180;
        }

        log.info("SCORE ALTISSIMO {}", score);
        return 220;
    }

    private int score() {
        return new Random().nextInt(0, 1000);
    }
}
