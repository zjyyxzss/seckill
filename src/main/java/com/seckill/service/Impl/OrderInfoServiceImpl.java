package com.seckill.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seckill.mapper.OrderInfoMapper;
import com.seckill.mapper.SeckillGoodsMapper;
import com.seckill.pojo.OrderInfo;
import com.seckill.pojo.Result;
import com.seckill.pojo.SeckillGoods;
import com.seckill.service.IOrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements IOrderInfoService {
@Autowired
private OrderInfoMapper orderInfoMapper;
@Autowired
private SeckillGoodsMapper seckillGoodsMapper;


        // 创建订单

        
        @Override
        @Transactional
        public Result createOrder(SeckillGoods seckillGoods, Long userId) {
            //扣减库存
            seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
            boolean success = seckillGoodsMapper.updateById(seckillGoods) > 0;

            if (!success) {
                // 如果乐观锁更新失败 (比如重复消费), 就不创建订单
                return Result.error("秒杀失败,请稍后重试");
            }

            //创建订单
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setUserId(userId);
            orderInfo.setGoodsId(seckillGoods.getId());
            orderInfo.setGoodsName(seckillGoods.getGoodsName());
            orderInfo.setGoodsCount(1);
            orderInfo.setSeckillPrice(seckillGoods.getSeckillPrice());
            orderInfo.setCreateDate(new Date());
            orderInfo.setStatus("0");
            //生成订单编号
            orderInfo.setOrderSn(generateOrderSn(userId, seckillGoods.getId()));
            orderInfoMapper.insert(orderInfo);
            return Result.success(orderInfo);
        }

    @Override
    public boolean hasOrder(Long userId, Long goodsId) {
            //判断用户是否已经生成过订单
            OrderInfo orderInfo = orderInfoMapper.selectOne(new LambdaQueryWrapper<OrderInfo>()
                    .eq(OrderInfo::getUserId, userId)
                    .eq(OrderInfo::getGoodsId, goodsId));
            return orderInfo != null;
    }

    private String generateOrderSn(Long userId, Long id) {
        return "SK" + System.currentTimeMillis() + userId + id;
    }
}