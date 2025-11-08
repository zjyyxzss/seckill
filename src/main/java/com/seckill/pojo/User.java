package com.seckill.pojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    /**
     * 手机号作为主键ID
     */
    @TableId(type = IdType.INPUT)
    private Long id;  // 手机号
    private String username;
    private String password;
    private String salt;
}