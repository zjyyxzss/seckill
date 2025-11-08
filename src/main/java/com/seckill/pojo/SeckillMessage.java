package com.seckill.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long userId;
    private Long goodsId;
}