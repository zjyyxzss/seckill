package com.seckill;

import com.seckill.pojo.Result;
import com.seckill.service.ISeckillService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@SpringBootTest
@TestPropertySource(properties = {
    "spring.rabbitmq.host=192.168.100.129",
    "spring.rabbitmq.port=5672",
    "spring.rabbitmq.username=guest",
    "spring.rabbitmq.password=guest"
})
public class JMeterSolutionTest {

    @Autowired
    private ISeckillService seckillService;

    @Test
    public void demonstrateJMeterProblemAndSolution() throws InterruptedException {
        log.info("=== JMeter压测问题分析与解决方案 ===");
        
        // 问题1: 控制台没有输出
        log.info("【问题1】JMeter压测时控制台没有输出");
        log.info("原因：系统采用异步消息队列处理订单，控制台日志主要在消息消费者端输出");
        log.info("解决方案：增加更详细的日志配置，或使用同步模式测试");
        
        // 问题2: 数据库没有数据
        log.info("【问题2】数据库订单表没有数据");
        log.info("原因：订单是异步创建的，需要等待消息队列处理完成");
        log.info("解决方案：压测后等待一段时间再检查数据，或增加数据检查接口");
        
        // 演示同步模式测试
        log.info("=== 同步模式测试（便于观察结果）===");
        syncModeTest();
        
        // 演示异步模式测试
        log.info("=== 异步模式测试（模拟真实JMeter压测）===");
        asyncModeTest();
        
        log.info("=== JMeter压测建议 ===");
        log.info("1. 在RabbitMQ管理界面监控消息队列消费情况");
        log.info("2. 压测后等待30秒再检查数据库订单数据");
        log.info("3. 关注Redis库存变化：seckill:stock:1");
        log.info("4. 关注已购买用户集合：seckill:users:1");
        log.info("5. 使用日志文件分析工具查看完整日志");
    }

    private void syncModeTest() throws InterruptedException {
        log.info("同步模式测试开始...");
        
        // 重置库存
        seckillService.preheatStock(1L, 3);
        log.info("库存重置为3件");
        
        // 顺序执行，便于观察
        for (int i = 0; i < 5; i++) {
            Long userId = 60000L + i;
            log.info("用户{} 开始秒杀...", userId);
            
            Result result = seckillService.doSeckill(1L, userId);
            
            log.info("用户{} 秒杀结果: code={}, message={}", 
                    userId, result.getCode(), result.getMessage());
            
            // 等待消息处理
            Thread.sleep(1000);
        }
        
        log.info("同步模式测试完成，等待额外2秒让消息处理完成...");
        Thread.sleep(2000);
    }

    private void asyncModeTest() throws InterruptedException {
        log.info("异步模式测试开始（模拟JMeter压测）...");
        
        // 重置库存
        seckillService.preheatStock(1L, 10);
        log.info("库存重置为10件");
        
        int threadCount = 50;
        int requestsPerThread = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount * requestsPerThread);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger duplicateCount = new AtomicInteger(0);
        AtomicInteger stockOutCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            executor.submit(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    try {
                        Long userId = 70000L + threadIndex * requestsPerThread + j;
                        Result result = seckillService.doSeckill(1L, userId);
                        
                        // 统计结果
                        if (result.getCode() == 200) {
                            if (result.getMessage().contains("抢购成功")) {
                                successCount.incrementAndGet();
                            } else if (result.getMessage().contains("队列")) {
                                successCount.incrementAndGet(); // 进入队列也算成功
                            }
                        } else if (result.getMessage().contains("已抢购过")) {
                            duplicateCount.incrementAndGet();
                        } else if (result.getMessage().contains("已售罄")) {
                            stockOutCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                        }
                        
                        log.debug("用户{} 结果: {}", userId, result.getMessage());
                        
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        log.error("用户秒杀异常", e);
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        
        latch.await();
        long totalTime = System.currentTimeMillis() - startTime;
        
        log.info("异步模式测试完成:");
        log.info("总耗时: {}ms", totalTime);
        log.info("总请求数: {}", threadCount * requestsPerThread);
        log.info("成功(进入队列): {}", successCount.get());
        log.info("重复购买: {}", duplicateCount.get());
        log.info("库存不足: {}", stockOutCount.get());
        log.info("异常错误: {}", errorCount.get());
        
        log.info("等待10秒让消息队列处理完成...");
        Thread.sleep(10000);
        
        log.info("现在可以检查数据库订单表和Redis数据了！");
    }

    @Test
    public void showJMeterBestPractices() {
        log.info("=== JMeter压测最佳实践 ===");
        
        log.info("【压测前准备】");
        log.info("1. 确保RabbitMQ正常运行: http://192.168.100.129:15672");
        log.info("2. 清空Redis数据: redis-cli FLUSHALL");
        log.info("3. 清空订单表: TRUNCATE TABLE order_info");
        log.info("4. 重置商品库存: UPDATE seckill_goods SET stock_count = 100 WHERE id = 1");
        
        log.info("【JMeter配置建议】");
        log.info("1. HTTP请求默认值: 服务器地址192.168.100.129，端口8080");
        log.info("2. 线程组设置: 线程数100，循环次数1，Ramp-up时间1秒");
        log.info("3. HTTP请求: GET /seckill/v1/execute?goodsId=1");
        log.info("4. 添加HTTP信息头管理器: Authorization Bearer token");
        
        log.info("【监控指标】");
        log.info("1. RabbitMQ队列消息数: seckill.queue");
        log.info("2. Redis库存: seckill:stock:1");
        log.info("3. Redis已购用户: seckill:users:1");
        log.info("4. 数据库订单数: SELECT COUNT(*) FROM order_info");
        
        log.info("【结果验证】");
        log.info("1. 压测后立即查看RabbitMQ队列消费情况");
        log.info("2. 等待30秒后查询数据库订单数据");
        log.info("3. 检查Redis库存是否为0");
        log.info("4. 检查订单数量是否等于初始库存");
    }
}