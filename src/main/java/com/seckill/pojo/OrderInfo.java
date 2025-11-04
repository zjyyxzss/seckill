package com.seckill.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("order_info")
public class OrderInfo {
    private Long id;
    private Long userId;
    private Long goodsId;
    private String orderSn;
    private String goodsName;
    private Integer goodsCount;
    private Integer seckillPrice;
    private Date createDate;
    private String status;

}
