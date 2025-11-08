package com.seckill.service;

import com.seckill.pojo.Result;

public interface ISeckillService {


    Result doSeckill(Long goodsId, Long userId);


    //将库存预热到缓存中
    Result preheatStock(Long goodsId, Integer stockCount);
}
