package com.seckill.mq;

import com.seckill.Config.RabbitMQConfig;
import com.seckill.pojo.SeckillMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQSender {
    @Autowired
    private RabbitTemplate rabbitTemplate;
     // 发送秒杀订单消息
    public void sendSeckillMessage(SeckillMessage message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.SECKILL_QUEUE, message);
    }



}
