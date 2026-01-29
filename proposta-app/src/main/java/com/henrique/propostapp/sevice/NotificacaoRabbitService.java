package com.henrique.propostapp.sevice;

import com.henrique.propostapp.entity.Proposta;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacaoRabbitService {

    private final RabbitTemplate rabbitTemplate;

    public void notificar(Proposta proposta, String exchange) {

        rabbitTemplate.convertAndSend(exchange, "", proposta);
        log.info("OBJETO {} ENVIADO PARA A EXCHANGE {} COM SUCESSO!!", proposta, exchange);
    }
}
