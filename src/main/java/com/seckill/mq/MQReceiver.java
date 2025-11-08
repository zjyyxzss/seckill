package com.seckill.mq;


import com.seckill.pojo.SeckillGoods;
import com.seckill.pojo.SeckillMessage;
import com.seckill.service.Impl.OrderInfoServiceImpl;
import com.seckill.service.Impl.SeckillServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.seckill.Config.RabbitMQConfig.SECKILL_QUEUE;

@Slf4j
@Service
public class MQReceiver {
    @Autowired
    private OrderInfoServiceImpl orderInfoService;
    @Autowired
    private SeckillServiceImpl seckillService;
    @RabbitListener(queues = SECKILL_QUEUE)
    public void receive(SeckillMessage message){
        log.info("接收秒杀消息: {}", message);
        // 1. 从消息中获取用户ID和商品ID
        Long userId = message.getUserId();
        Long goodsId = message.getGoodsId();
        //2.获取商品信息
        SeckillGoods seckillGoods = seckillService.getById(goodsId);
        if (seckillGoods.getStockCount() <= 0) {
            log.error("商品库存不足: {}", goodsId);
            return;
        }
        //3.检查是否重复抢购
        if (orderInfoService.hasOrder(userId, goodsId)) {
            log.error("用户{}已抢购商品{}", userId, goodsId);
            return;
        }
        //4.创建订单
        orderInfoService.createOrder(seckillGoods, userId);
    }
}
