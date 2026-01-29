package com.henrique.notificacao.listener;

import com.henrique.notificacao.constants.MensagemConstante;
import com.henrique.notificacao.domain.Proposta;
import com.henrique.notificacao.service.NotificacaoSmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PropostaConcluidaListener {

    private final NotificacaoSmsService notificacaoSmsService;

    @RabbitListener(queues = "${queues.ms-notificacao.proposta-concluida}")
    public void notificar(Proposta proposta) {
        log.info("RECEBIDO A PROPOSTA {}", proposta);

        String mensagem = proposta.getAprovada()
                ? String.format(MensagemConstante.PROPOSTA_APROVADA, proposta.getUsuario().getNome())
                : String.format(MensagemConstante.PROPOSTA_NEGADA, proposta.getUsuario().getNome(), proposta.getObservacao());

        notificacaoSmsService.notificarPorSMS(proposta.getUsuario().getTelefone(), mensagem);
    }
}
