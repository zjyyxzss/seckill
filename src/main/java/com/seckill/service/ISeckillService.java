package com.seckill.service;

import com.seckill.pojo.Result;

public interface ISeckillService {


    Result doSeckill(Long goodsId);


    //将库存预热到缓存中
    Result preheatStock(Long goodsId, Integer stockCount);
}
