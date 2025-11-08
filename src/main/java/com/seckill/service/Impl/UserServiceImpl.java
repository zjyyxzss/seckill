package com.seckill.service.Impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.mapper.UserMapper;
import com.seckill.pojo.LoginResponse;
import com.seckill.pojo.Result;
import com.seckill.pojo.User;
import com.seckill.service.UserService;
import com.seckill.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Result register(User user) {
        // 1. 参数验证
        if (user == null) {
            return Result.error("用户信息不能为空");
        }
        
        // 手机号验证 (作为ID)
        if (user.getId() == null || user.getId().toString().length() != 11) {
            return Result.error("手机号必须是11位数字");
        }
        
        // 用户名验证
        if (StrUtil.isBlank(user.getUsername())) {
            return Result.error("用户名不能为空");
        }
        
        if (user.getUsername().length() < 3 || user.getUsername().length() > 20) {
            return Result.error("用户名长度必须在3-20位之间");
        }
        
        // 密码验证
        if (StrUtil.isBlank(user.getPassword())) {
            return Result.error("密码不能为空");
        }
        
        if (user.getPassword().length() < 6 || user.getPassword().length() > 20) {
            return Result.error("密码长度必须在6-20位之间");
        }
        
        // 2. 判断手机号是否已存在 (手机号作为ID)
        if (userMapper.selectById(user.getId()) != null) {
            return Result.error("该手机号已注册");
        }
        
        // 3. 判断用户名是否已存在
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername())) != null) {
            return Result.error("用户名已存在");
        }
        
        // 4. 密码加密
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        
        // 5. 注册用户
        userMapper.insert(user);
        
        return Result.success("注册成功");
    }

    @Override
    public Result login(String username, String password) {
        // 1. 参数验证
        if (StrUtil.isBlank(username)) {
            return Result.error("用户名不能为空");
        }
        if (StrUtil.isBlank(password)) {
            return Result.error("密码不能为空");
        }
        
        // 2. 判断用户是否存在
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 3. 判断密码是否正确 (使用密码编码器验证)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.error("密码错误");
        }
        
        // 4. 生成JWT Token
        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
        
        // 5. 创建登录响应对象
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        
        // 创建用户对象（不包含密码）
        User userInfo = new User();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        loginResponse.setUser(userInfo);
        
        // 6. 返回Token和用户信息
        return Result.success(loginResponse);
    }
}