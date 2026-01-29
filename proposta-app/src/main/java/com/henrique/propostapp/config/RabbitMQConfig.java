package com.henrique.propostapp.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RabbitMQConfig {

    @Value("${queues.ms-analise-credito.proposta-pendente}")
    private String queuePropostaPendenteMsAnaliseCredito;

    @Value("${queues.ms-analise-credito.proposta-concluida}")
    private String queuePropostaConcluidaMsAnaliseCredito;

    @Value("${queues.ms-notificacao.proposta-pendente}")
    private String queuePropostaPendenteMsNotificacao;

    @Value("${queues.ms-notificacao.proposta-concluida}")
    private String queuePropostaConcluidaMsNotificacao;

    @Value("${queues.ms-proposta.proposta-pendente}")
    private String queuePropostaPendenteMsProposta;

    @Value("${queues.ms-proposta.proposta-concluida}")
    private String queuePropostaConcluidaMsProposta;

    @Value("${queues.deadletter.proposta-pendente}")
    private String queuePropostaPendendeDlq;

    @Value("${queues.deadletter.proposta-pendente-exchange}")
    private String exchangePropostaPendendeDlq;

    @Value("${queues.deadletter.proposta-concluida}")
    private String queuePropostaConcluidaDlq;

    @Value("${queues.deadletter.proposta-concluida-exchange}")
    private String exchangePropostaConcluidaDlq;

    // EXCHANGES

    @Value("${queues.exchange.proposta-pendente}")
    private String exchangeQueuePropostaPendente;

    @Value("${queues.exchange.proposta-concluida}")
    private String exchangeQueuePropostaConcluida;

    @Bean
    public Queue criarFilaPropostaPendenteMsAnaliseCredito() {
        return QueueBuilder
                .durable(queuePropostaPendenteMsAnaliseCredito)
                .deadLetterExchange(exchangePropostaPendendeDlq)
                .build();
    }

    @Bean
    public Queue criarFilaPropostaConcluidaMsAnaliseCredito() {
        return QueueBuilder
                .durable(queuePropostaConcluidaMsAnaliseCredito)
                .deadLetterExchange(exchangePropostaConcluidaDlq)
                .build();
    }

    @Bean
    public Queue criarFilaPropostaPendenteMsNotificacao() {
        return QueueBuilder
                .durable(queuePropostaPendenteMsNotificacao)
                .deadLetterExchange(exchangePropostaPendendeDlq)
                .build();
    }

    @Bean
    public Queue criarFilaPropostaConcluidaMsNotificacao() {
        return QueueBuilder
                .durable(queuePropostaConcluidaMsNotificacao)
                .deadLetterExchange(exchangePropostaConcluidaDlq)
                .build();
    }

    @Bean
    public Queue criarFilaPropostaPendenteMsProposta() {
        return QueueBuilder
                .durable(queuePropostaPendenteMsProposta)
                .deadLetterExchange(exchangePropostaPendendeDlq)
                .build();
    }

    @Bean
    public Queue criarFilaPropostaConcluidaMsProposta() {
        return QueueBuilder
                .durable(queuePropostaConcluidaMsProposta)
                .deadLetterExchange(exchangePropostaConcluidaDlq)
                .build();
    }

    @Bean
    public RabbitAdmin criarRabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> inicializarAdmin(RabbitAdmin rabbitAdmin) {
        return event -> {
            try {
                rabbitAdmin.initialize();
                log.info("RabbitMQ inicializado com sucesso!");
            } catch (Exception e) {
                log.error("Falha ao inicializar RabbitMQ. A aplicação continuará funcionando sem o broker: {}",
                        e.getMessage());
            }
        };
    }

    @Bean
    public FanoutExchange criarFanoutExchangePropostaPendente() {
        return ExchangeBuilder
                .fanoutExchange(exchangeQueuePropostaPendente)
                .build();
    }

    @Bean
    public Binding criarBindingPropostPendenteMsAnaliseCredito() {
        return BindingBuilder
                .bind(criarFilaPropostaPendenteMsAnaliseCredito())
                .to(criarFanoutExchangePropostaPendente());
    }

    @Bean
    public Binding criarBindingPropostPendenteMsNotificacao() {
        return BindingBuilder
                .bind(criarFilaPropostaPendenteMsNotificacao())
                .to(criarFanoutExchangePropostaPendente());
    }

    @Bean
    public FanoutExchange criarFanoutExchangePropostaConcluida() {
        return ExchangeBuilder
                .fanoutExchange(exchangeQueuePropostaConcluida)
                .build();
    }

    @Bean
    public Binding criarBindingPropostaConcluidaMsPropostaApp() {
        return BindingBuilder
                .bind(criarFilaPropostaConcluidaMsProposta())
                .to(criarFanoutExchangePropostaConcluida());
    }

    @Bean
    public Binding criarBindingPropostaConcluidaMsNotificacao() {
        return BindingBuilder
                .bind(criarFilaPropostaConcluidaMsNotificacao())
                .to(criarFanoutExchangePropostaConcluida());
    }

    @Bean
    public Queue criarFilaPropostaPendendeDlq() {
        return QueueBuilder.durable(queuePropostaPendendeDlq).build();
    }

    @Bean
    public FanoutExchange deadLetterExchange() {
        return ExchangeBuilder.fanoutExchange(exchangePropostaPendendeDlq).build();
    }

    @Bean
    public Binding criarBinding() {
        return BindingBuilder
                .bind(criarFilaPropostaPendendeDlq())
                .to(deadLetterExchange());
    }

    @Bean
    public Queue criarFilaPropostaConcluidaDlq() {
        return QueueBuilder.durable(queuePropostaConcluidaDlq).build();
    }

    @Bean
    public FanoutExchange deadLetterExchangePropostaConcluida() {
        return ExchangeBuilder.fanoutExchange(exchangePropostaConcluidaDlq).build();
    }

    @Bean
    public Binding criarBindingPropostaConcluidaDlq() {
        return BindingBuilder
                .bind(criarFilaPropostaConcluidaDlq())
                .to(deadLetterExchangePropostaConcluida());
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());

        return rabbitTemplate;
    }
}
