package com.seckill.service.Impl;

import cn.hutool.core.util.StrUtil;
import com.seckill.mapper.OrderInfoMapper;
import com.seckill.mapper.SeckillGoodsMapper;
import com.seckill.pojo.Result;
import com.seckill.pojo.SeckillGoods;
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
public class SeckillServiceImpl implements ISeckillService {
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private IOrderInfoService orderInfoService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setScriptSource(new ResourceScriptSource(new ClassPathResource("seckill.lua")));
        SECKILL_SCRIPT.setResultType(Long.class);
    }



    @Override
    @Transactional
    public Result doSeckill(Long goodsId) {
        long userId = 0;
        try {
            //1.获取用户ID - 使用随机数模拟不同用户
            // 当前实现
            userId = (long) (Math.random() * 1000000) + 1;
            
            // 建议改为更可靠的生成方式
            userId = System.currentTimeMillis() * 1000 + (long)(Math.random() * 1000);
            
            //2.执行lua脚本
            long result = stringRedisTemplate.execute(
                    SECKILL_SCRIPT,// 脚本
                    Arrays.asList(
                            "seckill:stock:" + goodsId,   // KEYS[1]
                            "seckill:users:" + goodsId    // KEYS[2]
                    ),              // 两个 KEY
                    Long.toString(userId)  // ARGV[1]
            );
            // 3. 分析 Lua 脚本的返回结果
            if (result == 1L) {
                return Result.error("您已抢购过，请勿重复下单");
            }
            if (result == 2L) {
                return Result.error("商品已售罄");
            }
            // 4. 如果 result == 0, 代表抢购成功！
            // 同步扣减数据库库存并创建订单
            SeckillGoods seckillGoods = seckillGoodsMapper.selectById(goodsId);
            if (seckillGoods == null) {
                return Result.error("商品不存在");
            }

            // 扣减数据库库存
            int updatedRows = seckillGoodsMapper.decreaseStock(goodsId);
            if (updatedRows <= 0) {
                // 回滚Redis库存
                stringRedisTemplate.opsForValue().increment("seckill:stock:" + goodsId);
                stringRedisTemplate.opsForSet().remove("seckill:users:" + goodsId, Long.toString(userId));
                return Result.error("库存不足");
            }
            // 创建订单
            orderInfoService.createOrder(seckillGoods, userId);

            return Result.success("抢购成功");
        } catch (Exception e) {
            // 发生异常时回滚Redis操作
            stringRedisTemplate.opsForValue().increment("seckill:stock:" + goodsId);
            stringRedisTemplate.opsForSet().remove("seckill:users:" + goodsId, Long.toString(userId));
            return Result.error("系统异常，请稍后重试");
        }
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