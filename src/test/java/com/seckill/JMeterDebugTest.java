package com.seckill;

import com.seckill.pojo.Result;
import com.seckill.service.ISeckillService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

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
public class JMeterDebugTest {

    @Autowired
    private ISeckillService seckillService;

    @Test
    public void testSingleSeckill() {
        log.info("=== 开始单个秒杀测试 ===");
        
        // 预热库存
        Result preheatResult = seckillService.preheatStock(1L, 10);
        log.info("预热库存结果: {}", preheatResult);
        
        // 模拟用户ID
        Long userId = 10001L;
        
        // 执行秒杀
        Result result = seckillService.doSeckill(1L, userId);
        log.info("秒杀结果: {}", result);
        
        log.info("=== 单个秒杀测试完成 ===");
    }

    @Test
    public void testConcurrentSeckill() throws InterruptedException {
        log.info("=== 开始并发秒杀测试 ===");
        
        // 预热库存
        Result preheatResult = seckillService.preheatStock(1L, 10);
        log.info("预热库存结果: {}", preheatResult);
        
        int threadCount = 20;
        int requestsPerThread = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount * requestsPerThread);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            executor.submit(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    try {
                        // 模拟不同的用户ID
                        Long userId = 20000L + threadIndex * requestsPerThread + j;
                        Result result = seckillService.doSeckill(1L, userId);
                        
                        if (result.getCode() == 200) {
                            successCount.incrementAndGet();
                            log.info("用户{}秒杀成功: {}", userId, result.getMessage());
                        } else {
                            failCount.incrementAndGet();
                            log.info("用户{}秒杀失败: {}", userId, result.getMessage());
                        }
                    } catch (Exception e) {
                        log.error("秒杀异常", e);
                        failCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        log.info("=== 并发测试完成 ===");
        log.info("成功请求: {}", successCount.get());
        log.info("失败请求: {}", failCount.get());
        log.info("总请求数: {}", successCount.get() + failCount.get());
        
        // 等待消息处理
        Thread.sleep(3000);
    }

    @Test
    public void testDirectSeckillWithoutAuth() {
        log.info("=== 测试无认证直接调用秒杀服务 ===");
        
        // 预热库存
        Result preheatResult = seckillService.preheatStock(1L, 10);
        log.info("预热库存结果: {}", preheatResult);
        
        // 直接使用服务层，绕过Controller的认证
        for (int i = 0; i < 5; i++) {
            Long userId = 30000L + i;
            Result result = seckillService.doSeckill(1L, userId);
            log.info("用户{}秒杀结果: code={}, message={}", 
                    userId, result.getCode(), result.getMessage());
        }
        
        log.info("=== 无认证测试完成 ===");
    }
}