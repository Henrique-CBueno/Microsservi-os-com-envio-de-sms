package com.henrique.analiseCredito.service;

import com.henrique.analiseCredito.domain.Proposta;
import com.henrique.analiseCredito.service.strategy.CalculoPonto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnaliseCreditoService {

    @Value("${queues.exchange.proposta-concluida}")
    private String propostaConcluidaExchange;

    // Spring já pega todas as implementacoes do calculoPonto pela injecao de dependencia,
    // Ele injeta em ordem alfabetica com a injecao de dependecias, devo usar o @Order na impl para resolver
    private final List<CalculoPonto> calculoPontoList;

    private final NotificacaoRabbitMqService notificacaoRabbitMqService;

    public void analizar(Proposta proposta) {
        try {
            boolean aprovada = calculoPontoList.stream()
                    .mapToInt(impl -> impl.calcular(proposta))
                    .sum() > 350;

            if (!aprovada) log.error("PROPOSTA NAO FOI APROVADA POR PONTUACAO");

            log.info("PROPOSTA APROVADA");
            proposta.setAprovada(aprovada);
        } catch (Exception e) {
            log.error("PROPOSTA NAO FOI APROVADA POR: {}", e.getMessage());
            proposta.setAprovada(false);
            proposta.setObservacao(e.getMessage());
        }

        notificacaoRabbitMqService.notificar(propostaConcluidaExchange, proposta);

    }

}

