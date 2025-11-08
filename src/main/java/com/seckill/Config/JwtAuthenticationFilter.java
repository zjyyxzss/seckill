package com.seckill.Config;


import com.seckill.utils.JwtUtil;
import io.jsonwebtoken.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException, java.io.IOException {

        // 1. 从请求头获取 Token
        String header = request.getHeader("Authorization");
        String token = null;

        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7); // "Bearer " 占 7 位
        }

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 2. 解析 Token, 获取 userId
            Long userId = jwtUtil.getUserIdFromToken(token);

            if (userId != null) {
                // 3. (模拟) 加载用户信息
                // 真实的场景是: UserDetails userDetails = userDetailsService.loadUserById(userId);
                // 为了 V4.0 快速跑通, 我们可以先“信任”Token

                //4. 创建 Authentication
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, null); // principal 存 userId
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        // 5. 放行请求
        filterChain.doFilter(request, response);
    }
}
