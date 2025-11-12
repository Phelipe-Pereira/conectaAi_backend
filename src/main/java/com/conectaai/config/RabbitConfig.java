package com.conectaai.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "webhook.exchange";
    public static final String DLX = "webhook.dlx";

    public static final String Q_PAYMENT = "webhook.payment.q";
    public static final String Q_SUBSCRIPTION = "webhook.subscription.q";
    public static final String Q_INSTALLMENT = "webhook.installment.q";

    public static final String DLQ_PAYMENT = "webhook.payment.dlq";
    public static final String DLQ_SUBSCRIPTION = "webhook.subscription.dlq";
    public static final String DLQ_INSTALLMENT = "webhook.installment.dlq";

    private static final String ARG_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
    private static final String ARG_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";

    @Bean
    public TopicExchange webhookExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange webhookDlx() {
        return new TopicExchange(DLX, true, false);
    }

    @Bean
    public Queue paymentQueue() {
        return QueueBuilder.durable(Q_PAYMENT)
                .withArgument(ARG_DEAD_LETTER_EXCHANGE, DLX)
                .withArgument(ARG_DEAD_LETTER_ROUTING_KEY, Q_PAYMENT)
                .build();
    }

    @Bean
    public Queue subscriptionQueue() {
        return QueueBuilder.durable(Q_SUBSCRIPTION)
                .withArgument(ARG_DEAD_LETTER_EXCHANGE, DLX)
                .withArgument(ARG_DEAD_LETTER_ROUTING_KEY, Q_SUBSCRIPTION)
                .build();
    }

    @Bean
    public Queue installmentQueue() {
        return QueueBuilder.durable(Q_INSTALLMENT)
                .withArgument(ARG_DEAD_LETTER_EXCHANGE, DLX)
                .withArgument(ARG_DEAD_LETTER_ROUTING_KEY, Q_INSTALLMENT)
                .build();
    }

    @Bean
    public Queue paymentDlq() {
        return QueueBuilder.durable(DLQ_PAYMENT).build();
    }

    @Bean
    public Queue subscriptionDlq() {
        return QueueBuilder.durable(DLQ_SUBSCRIPTION).build();
    }

    @Bean
    public Queue installmentDlq() {
        return QueueBuilder.durable(DLQ_INSTALLMENT).build();
    }

    @Bean
    public Binding bindPayment(TopicExchange webhookExchange, Queue paymentQueue) {
        return BindingBuilder.bind(paymentQueue).to(webhookExchange).with("payment.*.*");
    }

    @Bean
    public Binding bindSubscription(TopicExchange webhookExchange, Queue subscriptionQueue) {
        return BindingBuilder.bind(subscriptionQueue).to(webhookExchange).with("subscription.*.*");
    }

    @Bean
    public Binding bindInstallment(TopicExchange webhookExchange, Queue installmentQueue) {
        return BindingBuilder.bind(installmentQueue).to(webhookExchange).with("installment.*.*");
    }

    @Bean
    public Binding bindPaymentDlq(TopicExchange webhookDlx, Queue paymentDlq) {
        return BindingBuilder.bind(paymentDlq).to(webhookDlx).with(Q_PAYMENT);
    }

    @Bean
    public Binding bindSubscriptionDlq(TopicExchange webhookDlx, Queue subscriptionDlq) {
        return BindingBuilder.bind(subscriptionDlq).to(webhookDlx).with(Q_SUBSCRIPTION);
    }

    @Bean
    public Binding bindInstallmentDlq(TopicExchange webhookDlx, Queue installmentDlq) {
        return BindingBuilder.bind(installmentDlq).to(webhookDlx).with(Q_INSTALLMENT);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}


