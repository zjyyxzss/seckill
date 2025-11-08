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
        log.info("【消息队列】接收秒杀消息: {}", message);
        // 1. 从消息中获取用户ID和商品ID
        Long userId = message.getUserId();
        Long goodsId = message.getGoodsId();
        log.info("【消息队列】处理用户{}对商品{}的秒杀请求", userId, goodsId);
        
        //2.获取商品信息
        SeckillGoods seckillGoods = seckillService.getById(goodsId);
        if (seckillGoods == null) {
            log.error("【消息队列】商品{}不存在", goodsId);
            return;
        }
        
        if (seckillGoods.getStockCount() <= 0) {
            log.error("【消息队列】商品{}库存不足，当前库存: {}", goodsId, seckillGoods.getStockCount());
            return;
        }
        
        //3.检查是否重复抢购
        if (orderInfoService.hasOrder(userId, goodsId)) {
            log.error("【消息队列】用户{}已抢购商品{}，拒绝重复购买", userId, goodsId);
            return;
        }
        
        log.info("【消息队列】用户{}对商品{}的秒杀验证通过，开始创建订单", userId, goodsId);
        //4.创建订单
        try {
            orderInfoService.createOrder(seckillGoods, userId);
            log.info("【消息队列】用户{}对商品{}的订单创建成功！", userId, goodsId);
        } catch (Exception e) {
            log.error("【消息队列】用户{}对商品{}的订单创建失败: {}", userId, goodsId, e.getMessage(), e);
            throw e; // 重新抛出异常，让消息队列重新处理
        }
    }
}