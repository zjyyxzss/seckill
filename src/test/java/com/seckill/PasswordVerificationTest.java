package com.seckill;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.*;
import com.seckill.pojo.User;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class PasswordVerificationTest {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    public void testPasswordEncoding() {
        System.out.println("=== 测试密码加密过程 ===");
        
        String rawPassword = "123456";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密后密码: " + encodedPassword);
        System.out.println("密码长度: " + encodedPassword.length());
        
        // 验证同一个密码每次加密结果是否不同（BCrypt特性）
        String encodedPassword2 = passwordEncoder.encode(rawPassword);
        System.out.println("再次加密结果: " + encodedPassword2);
        System.out.println("两次加密结果相同: " + encodedPassword.equals(encodedPassword2));
        
        // 验证密码匹配
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        System.out.println("密码验证结果: " + matches);
        
        boolean matches2 = passwordEncoder.matches(rawPassword, encodedPassword2);
        System.out.println("密码验证结果2: " + matches2);
    }
    
    @Test
    public void testManualLoginVerification() {
        System.out.println("=== 手动验证登录过程 ===");
        
        // 1. 注册用户
        User user = new User();
        user.setId(13800138001L);
        user.setUsername("testuser2");
        user.setPassword("123456");
        
        ResponseEntity<String> registerResponse = restTemplate.postForEntity(
            "/user/register", user, String.class);
        System.out.println("注册响应: " + registerResponse.getBody());
        
        // 2. 立即尝试登录
        String loginUrl = "/user/login";
        String loginData = "{\"username\":\"testuser2\",\"password\":\"123456\"}";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(loginData, headers);
        
        ResponseEntity<String> loginResponse = restTemplate.exchange(
            loginUrl, HttpMethod.POST, request, String.class);
        
        System.out.println("登录响应: " + loginResponse.getBody());
        System.out.println("登录状态码: " + loginResponse.getStatusCode());
        
        // 3. 检查数据库中的用户密码
        System.out.println("=== 检查数据库用户 ===");
        String queryUrl = "/user/testuser2"; // 假设有这样的查询接口
        try {
            ResponseEntity<String> userResponse = restTemplate.getForEntity(queryUrl, String.class);
            System.out.println("用户查询响应: " + userResponse.getBody());
        } catch (Exception e) {
            System.out.println("用户查询接口不存在，这是正常的");
        }
    }
    
    @Test
    public void testPasswordMismatch() {
        System.out.println("=== 测试密码不匹配情况 ===");
        
        String rawPassword = "123456";
        String wrongPassword = "1234567";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        System.out.println("原始密码: " + rawPassword);
        System.out.println("错误密码: " + wrongPassword);
        System.out.println("加密后密码: " + encodedPassword);
        
        // 验证正确密码
        boolean correctMatch = passwordEncoder.matches(rawPassword, encodedPassword);
        System.out.println("正确密码验证结果: " + correctMatch);
        
        // 验证错误密码
        boolean wrongMatch = passwordEncoder.matches(wrongPassword, encodedPassword);
        System.out.println("错误密码验证结果: " + wrongMatch);
    }
}