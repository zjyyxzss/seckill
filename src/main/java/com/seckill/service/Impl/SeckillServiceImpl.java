package com.seckill.service.Impl;

import com.seckill.mapper.OrderInfoMapper;
import com.seckill.mapper.SeckillGoodsMapper;
import com.seckill.pojo.OrderInfo;
import com.seckill.pojo.Result;
import com.seckill.pojo.SeckillGoods;
import com.seckill.service.IOrderInfoService;
import com.seckill.service.ISeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class SeckillServiceImpl implements ISeckillService {
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private IOrderInfoService orderInfoService;



    @Override
    public Result doSeckill(Long goodsId) {
        //1.查询商品信息
        SeckillGoods seckillGoods = seckillGoodsMapper.selectById(goodsId);
        if (seckillGoods.getStockCount() <1) {
            return Result.error("商品已售罄");
        }
        //2.扣减库存
        seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
        seckillGoodsMapper.updateById(seckillGoods);
        //3.创建订单
        orderInfoService.createOrder(seckillGoods);
        return Result.success();

    }


}
