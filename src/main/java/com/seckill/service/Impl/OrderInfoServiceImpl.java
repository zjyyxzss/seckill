package com.seckill.service.Impl;

import com.seckill.mapper.OrderInfoMapper;
import com.seckill.pojo.OrderInfo;
import com.seckill.pojo.Result;
import com.seckill.pojo.SeckillGoods;
import com.seckill.service.IOrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class OrderInfoServiceImpl implements IOrderInfoService {
@Autowired
private OrderInfoMapper orderInfoMapper;


        // 创建订单

        
        @Override
        public Result createOrder(SeckillGoods seckillGoods, Long userId) {
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

    private String generateOrderSn(Long userId, Long id) {
        return "SK" + System.currentTimeMillis() + userId + id;
    }
}