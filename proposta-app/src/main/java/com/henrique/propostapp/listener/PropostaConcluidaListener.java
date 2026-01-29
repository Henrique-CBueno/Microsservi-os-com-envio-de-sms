package com.henrique.propostapp.listener;

import com.henrique.propostapp.entity.Proposta;
import com.henrique.propostapp.sevice.PropostaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PropostaConcluidaListener {

    private final PropostaService propostaService;

    @RabbitListener(queues = "${queues.ms-proposta.proposta-concluida}")
    public void propostaConcluida(Proposta proposta) {
        log.info("RECEBIDA A PROPOSTA {} QUE FOI {}", proposta.getId(), proposta.getAprovada() == true ? "APROVADA" : "REPROVADA");

        propostaService.atualizarPropostaNoBancoEnviandoAoWebSocket(proposta);

        log.info("PROPOSTA {} SALVA NO BANCO", proposta.getId());
    }
}
