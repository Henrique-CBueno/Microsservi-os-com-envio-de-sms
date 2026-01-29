package com.henrique.propostapp.sevice;

import com.henrique.propostapp.dto.PropostaCreateResDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate template;

    public void notificar(PropostaCreateResDTO proposta) {
        template.convertAndSend("/propostas", proposta);
    }
}
