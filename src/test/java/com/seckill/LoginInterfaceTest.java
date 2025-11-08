package com.seckill;

import com.seckill.pojo.LoginRequest;
import com.seckill.pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class LoginInterfaceTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    public void testLoginWithJson() {
        System.out.println("=== 测试JSON格式登录 ===");
        
        // 先注册一个测试用户
        User user = new User();
        user.setId(13800138000L);
        user.setUsername("testuser");
        user.setPassword("123456");
        
        ResponseEntity<String> registerResponse = restTemplate.postForEntity(
            "/user/register", user, String.class);
        System.out.println("注册响应: " + registerResponse.getBody());
        
        // 测试JSON登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("123456");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            "/user/login", HttpMethod.POST, request, String.class);
        
        System.out.println("JSON登录响应: " + response.getBody());
        System.out.println("状态码: " + response.getStatusCode());
    }
    
    @Test
    public void testLoginWithForm() {
        System.out.println("=== 测试表单格式登录 ===");
        
        // 测试表单登录
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        String formData = "username=testuser&password=123456";
        HttpEntity<String> request = new HttpEntity<>(formData, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            "/user/login/form", HttpMethod.POST, request, String.class);
        
        System.out.println("表单登录响应: " + response.getBody());
        System.out.println("状态码: " + response.getStatusCode());
    }
    
    @Test
    public void testLoginWithWrongPassword() {
        System.out.println("=== 测试错误密码登录 ===");
        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            "/user/login", HttpMethod.POST, request, String.class);
        
        System.out.println("错误密码响应: " + response.getBody());
    }
}