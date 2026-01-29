package com.henrique.analiseCredito.listener;

import com.henrique.analiseCredito.domain.Proposta;
import com.henrique.analiseCredito.service.AnaliseCreditoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PropostaEmAnaliseListener {

    private final AnaliseCreditoService analiseCreditoService;

    @RabbitListener(queues = "${queues.ms-analise.proposta-pendente}")
    public void propostaEmAnalise(Proposta proposta) {

        log.info("PROPOSTA DE ID {} SERA ENVIADA PARA ANALISE", proposta.getId());
        analiseCreditoService.analizar(proposta);
    }
}
