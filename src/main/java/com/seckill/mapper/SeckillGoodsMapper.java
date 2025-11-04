package com.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.pojo.SeckillGoods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SeckillGoodsMapper extends BaseMapper<SeckillGoods> {

    /**
     * 扣减商品库存
     * @param goodsId 商品ID
     * @return 更新的行数
     */
    @Update("UPDATE seckill_goods SET stock_count = stock_count - 1 WHERE id = #{goodsId} AND stock_count > 0")
    int decreaseStock(Long goodsId);
}