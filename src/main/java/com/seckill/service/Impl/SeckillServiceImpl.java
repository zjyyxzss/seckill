package com.seckill.service.Impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seckill.mapper.OrderInfoMapper;
import com.seckill.mapper.SeckillGoodsMapper;
import com.seckill.mq.MQSender;
import com.seckill.pojo.Result;
import com.seckill.pojo.SeckillGoods;
import com.seckill.pojo.SeckillMessage;
import com.seckill.service.IOrderInfoService;
import com.seckill.service.ISeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class SeckillServiceImpl extends ServiceImpl<SeckillGoodsMapper, SeckillGoods> implements ISeckillService {
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private IOrderInfoService orderInfoService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MQSender mqSender;
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setScriptSource(new ResourceScriptSource(new ClassPathResource("seckill.lua")));
        SECKILL_SCRIPT.setResultType(Long.class);
    }
    /**
     * 秒杀
     * @param goodsId 商品id
     * @param userId 用户id
     * @return 秒杀结果
     */
    @Override
    public Result doSeckill(Long goodsId , Long userId) {
            //2.执行lua脚本
            long result = stringRedisTemplate.execute(
                    SECKILL_SCRIPT,
                    Arrays.asList(
                            "seckill:stock:" + goodsId,
                            "seckill:users:" + goodsId
                    ),
                    Long.toString(userId)
            );
            // 3. 分析 Lua 脚本的返回结果
            if (result == 1L) {
                return Result.error("您已抢购过，请勿重复下单");
            }
            if (result == 2L) {
                return Result.error("商品已售罄");
            }
            // 4. 如果 result == 0, 代表抢购成功！
            //异步创建订单,用MQ发送消息
            SeckillMessage message = new SeckillMessage(userId, goodsId);
            mqSender.sendSeckillMessage(message);
        return Result.queue("您的订单已加入队列，稍后处理");
    }

    //将库存预热到缓存中
    @Override
    public Result preheatStock(Long goodsId, Integer stockCount) {
       //1.从redis中查询缓存
        String stockKey = "seckill:stock:" + goodsId;
        String stock = stringRedisTemplate.opsForValue().get(stockKey);
        //2.判断是否存在
        if (StrUtil.isNotBlank(stock)) {
            //存在,直接返回
            return Result.success(stock);
        }
        //3.不存在,查询数据库
        SeckillGoods seckillGoods = seckillGoodsMapper.selectById(goodsId);
        //4.判断是否存在
        if (seckillGoods == null) {
            //将空值添加到缓存中(避免缓存穿透)
            stringRedisTemplate.opsForValue().set(stockKey, "", 60 * 60 * 1000);
            //不存在,返回错误信息
            return Result.error("商品不存在");
        }
        //5.存在,将库存添加到缓存中
        stringRedisTemplate.opsForValue().set(stockKey, stockCount.toString());
        //6.返回成功信息
        return Result.success(stockCount);
    }
}