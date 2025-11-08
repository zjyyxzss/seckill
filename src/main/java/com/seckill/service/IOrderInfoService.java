package com.seckill.service;

import com.seckill.pojo.Result;
import com.seckill.pojo.SeckillGoods;

public interface IOrderInfoService {
    Result createOrder(SeckillGoods seckillGoods, Long userId);

    boolean hasOrder(Long userId, Long goodsId);
}