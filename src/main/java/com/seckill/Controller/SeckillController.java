package com.seckill.Controller;

import com.seckill.pojo.Result;

import com.seckill.service.ISeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seckill")
public class SeckillController {
    @Autowired
    private ISeckillService seckillService;

    // 创建订单
    @GetMapping("/v1/execute")
    public Result doSeckill(@RequestParam("goodsId") Long goodsId, @RequestParam("userId") Long userId){
        return seckillService.doSeckill(goodsId,userId);
    }
    //查询商品,添加到缓存中
    @GetMapping("/v1/preheat")
    public Result preheatStock(Long goodsId, Integer stockCount){
        return seckillService.preheatStock(goodsId,stockCount);
    }

}