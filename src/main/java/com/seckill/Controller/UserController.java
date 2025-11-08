package com.seckill.Controller;


import com.seckill.pojo.LoginRequest;
import com.seckill.pojo.Result;
import com.seckill.pojo.User;
import com.seckill.service.UserService;
import com.seckill.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;


    //登录接口 - JSON格式
    @PostMapping("/login")
    public Result login(@RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest.getUsername(), loginRequest.getPassword());
    }
    
    //登录接口 - 表单格式 (备用)
    @PostMapping("/login/form")
    public Result loginWithForm(@RequestParam String username, @RequestParam String password) {
        return userService.login(username, password);
    }

    //注册接口
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        return userService.register(user);
    }
}