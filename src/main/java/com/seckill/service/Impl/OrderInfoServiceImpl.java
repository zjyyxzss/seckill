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
        public Result createOrder(SeckillGoods seckillGoods) {
            //创建订单
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setUserId(1L);
            orderInfo.setGoodsId(seckillGoods.getId());
            orderInfo.setGoodsName(seckillGoods.getGoodsName());
            orderInfo.setGoodsCount(1);
            orderInfo.setSeckillPrice(seckillGoods.getSeckillPrice());
            orderInfo.setCreateDate(new Date());
            orderInfo.setStatus("0");
            orderInfoMapper.insert(orderInfo);
            return Result.success(orderInfo);
        }
    }

