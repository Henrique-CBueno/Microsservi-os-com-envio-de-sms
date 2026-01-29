package com.henrique.propostapp.sevice;

import com.henrique.propostapp.dto.PropostaCreateReqDTO;
import com.henrique.propostapp.dto.PropostaCreateResDTO;
import com.henrique.propostapp.entity.Proposta;
import com.henrique.propostapp.mapper.PropostaMapper;
import com.henrique.propostapp.repository.PropostaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PropostaService {

    @Value("${queues.exchange.proposta-pendente}")
    private String exchangeQueuePropostaPendente;

    private final PropostaRepository propostaRepository;
    private final NotificacaoRabbitService notificacaoRabbitService;
    private final WebSocketService webSocketService;

    public PropostaCreateResDTO criarProposta(PropostaCreateReqDTO reqDto) {
        Proposta proposta = PropostaMapper.INSTANCE.toProposta(reqDto);
        propostaRepository.saveAndFlush(proposta);

        notificarRabbitMQ(proposta);

        log.info("PROPOSTA DO {} SALVA COM ID {}!", proposta.getUsuario().getNome(), proposta.getId());
        return PropostaMapper.INSTANCE.entityToResDTO(proposta);
    }

    private void notificarRabbitMQ(Proposta proposta) {
        try {
            notificacaoRabbitService.notificar(proposta, exchangeQueuePropostaPendente);
        } catch (RuntimeException e) {
            log.error("ERRO AO NOTIFICAR RABBITMQ DA PROPOSTA {}: {}", proposta.getId(), e.getMessage());

            proposta.setIntegrada(false);

            propostaRepository.saveAndFlush(proposta);
        }
    }

    public List<PropostaCreateResDTO> getAll() {
        List<Proposta> todasPropostas = propostaRepository.findAll();
        return PropostaMapper.INSTANCE.converteListEntityToListDTO(todasPropostas);
    }

    public void atualizarPropostaNoBancoEnviandoAoWebSocket(Proposta proposta) {
        propostaRepository.atualizarProposta(proposta.getId(), proposta.getAprovada(), proposta.getObservacao());

        PropostaCreateResDTO response = PropostaMapper.INSTANCE.entityToResDTO(proposta);
        webSocketService.notificar(response);
    }
}
