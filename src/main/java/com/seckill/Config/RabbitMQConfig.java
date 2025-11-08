package com.seckill.Config;

import com.seckill.pojo.SeckillMessage;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.AllowedListDeserializingMessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RabbitMQConfig {
    // 声明我们秒杀订单的队列名称
    public static final String SECKILL_QUEUE = "seckill.queue";
    
    @Bean
    public Queue seckillQueue() {
        // Queue(name, durable)
        // durable: true 表示持久化，RabbitMQ 重启后队列不会丢失
        return new Queue(SECKILL_QUEUE, true);
    }

    /**
     * 配置消息转换器为JSON格式
     * 用于消息的序列化和反序列化
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置RabbitTemplate
     * 设置消息转换器
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }


}