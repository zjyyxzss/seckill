package com.seckill.service;

import com.seckill.pojo.Result;
import com.seckill.pojo.User;

public interface UserService {

    Result register(User user);

    Result login(String username, String password);

}
