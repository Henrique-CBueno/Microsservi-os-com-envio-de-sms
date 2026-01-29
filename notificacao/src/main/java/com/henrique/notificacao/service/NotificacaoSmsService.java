package com.henrique.notificacao.service;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacaoSmsService {

    private final AmazonSNS amazonSNS;

    public void notificarPorSMS(String telefone, String mensagem) {
        PublishRequest publishRequest = new PublishRequest()
                .withMessage(mensagem)
                .withPhoneNumber(telefone);

        amazonSNS.publish(publishRequest);

        log.info("MENSAGEM {} ENVIADA COM SUCESSO PARA O NUMERO {}", mensagem, telefone);
    }
}
