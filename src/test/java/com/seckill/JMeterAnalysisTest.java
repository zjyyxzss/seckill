package com.seckill;

import com.seckill.pojo.Result;
import com.seckill.service.ISeckillService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@SpringBootTest
@TestPropertySource(properties = {
    "spring.rabbitmq.host=192.168.100.129",
    "spring.rabbitmq.port=5672",
    "spring.rabbitmq.username=guest",
    "spring.rabbitmq.password=guest"
})
public class JMeterAnalysisTest {

    @Autowired
    private ISeckillService seckillService;

    @Test
    public void analyzeJMeterProblem() throws InterruptedException {
        log.info("=== JMeter问题分析测试 ===");
        
        // 1. 预热库存
        log.info("步骤1: 预热库存到10件");
        Result preheatResult = seckillService.preheatStock(1L, 10);
        log.info("预热结果: {}", preheatResult);
        
        // 2. 模拟JMeter并发测试
        log.info("步骤2: 模拟JMeter并发测试 - 100个用户同时抢购");
        int totalRequests = 100;
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(totalRequests);
        
        Map<Integer, Result> results = new ConcurrentHashMap<>();
        Map<Integer, String> errors = new ConcurrentHashMap<>();
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < totalRequests; i++) {
            final int requestId = i;
            executor.submit(() -> {
                try {
                    Long userId = 40000L + requestId;
                    Result result = seckillService.doSeckill(1L, userId);
                    results.put(requestId, result);
                    
                    // 模拟JMeter的响应时间记录
                    long responseTime = System.currentTimeMillis() - startTime;
                    log.info("请求{} - 用户{} - 响应时间:{}ms - 结果: {}", 
                            requestId, userId, responseTime, result.getMessage());
                    
                } catch (Exception e) {
                    errors.put(requestId, e.getMessage());
                    log.error("请求{} 异常: {}", requestId, e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        long totalTime = System.currentTimeMillis() - startTime;
        
        // 3. 统计结果
        log.info("步骤3: 统计结果");
        log.info("总耗时: {}ms", totalTime);
        log.info("总请求数: {}", totalRequests);
        log.info("成功响应数: {}", results.size());
        log.info("异常数: {}", errors.size());
        
        // 分析响应结果
        Map<String, AtomicInteger> resultStats = new HashMap<>();
        for (Result result : results.values()) {
            String key = result.getCode() + " - " + result.getMessage();
            resultStats.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
        }
        
        log.info("响应结果分布:");
        resultStats.forEach((key, count) -> {
            log.info("  {}: {}次", key, count.get());
        });
        
        // 4. 等待异步消息处理完成
        log.info("步骤4: 等待异步消息处理完成...");
        Thread.sleep(5000); // 等待5秒让消息队列处理完
        
        log.info("=== 分析完成 ===");
        
        // 5. 输出JMeter压测建议
        log.info("=== JMeter压测建议 ===");
        log.info("1. 由于系统是异步处理，JMeter会立即收到响应，但实际订单是异步创建的");
        log.info("2. 控制台看不到输出是正常的，因为订单创建在消息队列消费者中处理");
        log.info("3. 数据库订单表数据是异步写入的，需要等待一段时间才能看到");
        log.info("4. 建议压测时关注Redis库存变化和消息队列消费情况");
        log.info("5. 可以在MQReceiver中添加更详细的日志来跟踪订单创建");
    }

    @Test
    public void testWithDetailedLogging() throws InterruptedException {
        log.info("=== 带详细日志的测试 ===");
        
        // 清空之前的日志干扰
        log.info("开始新的秒杀测试...");
        
        // 重置库存
        seckillService.preheatStock(1L, 5);
        log.info("库存已重置为5件");
        
        // 并发测试
        int threads = 10;
        CountDownLatch latch = new CountDownLatch(threads);
        
        for (int i = 0; i < threads; i++) {
            final int userIndex = i;
            new Thread(() -> {
                try {
                    Long userId = 50000L + userIndex;
                    log.info("用户{} 开始秒杀...", userId);
                    
                    Result result = seckillService.doSeckill(1L, userId);
                    
                    log.info("用户{} 秒杀完成 - 结果: code={}, message={}", 
                            userId, result.getCode(), result.getMessage());
                    
                } catch (Exception e) {
                    log.error("用户秒杀异常", e);
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        
        latch.await();
        
        log.info("所有秒杀请求已发送，等待异步处理...");
        Thread.sleep(3000);
        
        log.info("测试完成，检查数据库和日志文件查看订单数据");
    }
}