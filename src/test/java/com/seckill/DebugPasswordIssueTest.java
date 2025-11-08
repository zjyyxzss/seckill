package com.seckill;

import com.seckill.pojo.User;
import com.seckill.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class DebugPasswordIssueTest {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    public void debugPasswordIssue() {
        System.out.println("=== 调试密码问题 ===");
        
        // 1. 清理现有用户数据
        try {
            userMapper.deleteById(13800138002L);
            System.out.println("清理了现有用户数据");
        } catch (Exception e) {
            System.out.println("用户不存在或清理失败: " + e.getMessage());
        }
        
        // 2. 注册新用户
        User user = new User();
        user.setId(13800138002L);
        user.setUsername("debuguser");
        user.setPassword("123456");
        
        System.out.println("注册前密码: " + user.getPassword());
        
        ResponseEntity<String> registerResponse = restTemplate.postForEntity(
            "/user/register", user, String.class);
        System.out.println("注册响应: " + registerResponse.getBody());
        
        // 3. 查询数据库中的用户数据
        User dbUser = userMapper.selectById(13800138002L);
        if (dbUser != null) {
            System.out.println("数据库中的用户名: " + dbUser.getUsername());
            System.out.println("数据库中的密码: " + dbUser.getPassword());
            System.out.println("数据库密码长度: " + dbUser.getPassword().length());
            
            // 4. 验证密码
            boolean matches = passwordEncoder.matches("123456", dbUser.getPassword());
            System.out.println("密码验证结果: " + matches);
            
            // 5. 尝试登录
            String loginData = "{\"username\":\"debuguser\",\"password\":\"123456\"}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(loginData, headers);
            
            ResponseEntity<String> loginResponse = restTemplate.exchange(
                "/user/login", HttpMethod.POST, request, String.class);
            
            System.out.println("登录响应: " + loginResponse.getBody());
            System.out.println("登录状态码: " + loginResponse.getStatusCode());
            
        } else {
            System.out.println("数据库中未找到用户！");
        }
    }
    
    @Test
    public void checkExistingUsers() {
        System.out.println("=== 检查现有用户数据 ===");
        
        // 检查几个已存在的用户
        long[] userIds = {13800138000L, 13800138001L, 13800138002L};
        String[] passwords = {"123456", "123456", "123456"};
        
        for (int i = 0; i < userIds.length; i++) {
            User user = userMapper.selectById(userIds[i]);
            if (user != null) {
                System.out.println("=== 用户 " + userIds[i] + " ===");
                System.out.println("用户名: " + user.getUsername());
                System.out.println("数据库密码: " + user.getPassword());
                
                boolean matches = passwordEncoder.matches(passwords[i], user.getPassword());
                System.out.println("密码 " + passwords[i] + " 验证结果: " + matches);
                
                // 尝试登录
                String loginData = "{\"username\":\"" + user.getUsername() + "\",\"password\":\"" + passwords[i] + "\"}";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> request = new HttpEntity<>(loginData, headers);
                
                try {
                    ResponseEntity<String> loginResponse = restTemplate.exchange(
                        "/user/login", HttpMethod.POST, request, String.class);
                    System.out.println("登录响应状态: " + loginResponse.getStatusCode());
                    if (loginResponse.getBody().contains("密码错误")) {
                        System.out.println("❌ 登录失败：密码错误");
                    } else if (loginResponse.getBody().contains("success")) {
                        System.out.println("✅ 登录成功");
                    }
                } catch (Exception e) {
                    System.out.println("登录请求异常: " + e.getMessage());
                }
                System.out.println();
            }
        }
    }
}