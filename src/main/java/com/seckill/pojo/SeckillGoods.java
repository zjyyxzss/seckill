package com.seckill.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillGoods {
    private Long id;
    private Long goodsId;
    private String goodsName;
    private String goodsTitle;
    private String goodsImg;
    private Integer seckillPrice;
    private Integer originalPrice;
    private Integer stockCount;
    private Date startDate;
    private Date endDate;


}
