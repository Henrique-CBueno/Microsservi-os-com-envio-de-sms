package com.henrique.propostapp.agendador;

import com.henrique.propostapp.entity.Proposta;
import com.henrique.propostapp.repository.PropostaRepository;
import com.henrique.propostapp.sevice.NotificacaoRabbitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class PropostaSemIntegracao {

    @Value("${queues.exchange.proposta-pendente}")
    private String exchangeQueuePropostaPendente;

    private final PropostaRepository propostaRepository;
    private final NotificacaoRabbitService notificacaoRabbitService;
    private final PageRequest pageRequest = PageRequest.of(0, 100);

    // RODA A CADA 10 SEGUNDOS
    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void buscarPropostasSemIntegracao() {
        try {
            log.info("RODANDO O SCHEDULE");
            propostaRepository.findAllByIntegradaFalse(pageRequest)
                    .forEach(proposta -> {
                        try {
                            notificacaoRabbitService.notificar(proposta, exchangeQueuePropostaPendente);
                            atualizarPropostaComIntegrada(proposta);
                            log.info("PROPOSTA DE ID {} ENVIADA PARA FILA COM SUCESSO PELO AGENDADOR", proposta.getId());
                        } catch (Exception e) {
                            log.error("PROPOSTA DE ID {} NÃO FOI INTEGRADA. Tentará novamente na próxima execução. Erro: {}", proposta.getId(), e.getMessage());
                            // Não atualiza como integrada, tentará novamente no próximo ciclo
                        }
                    });
        } catch (Exception e) {
            log.error("Erro ao executar busca de propostas sem integração: {}", e.getMessage(), e);
            // Continua a executar mesmo se houver erro
        }
    }

    private void atualizarPropostaComIntegrada(Proposta proposta) {
        propostaRepository.atualizarStatusIntegrada(proposta.getId(), true);
    }
}
