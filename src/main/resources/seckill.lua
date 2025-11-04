---@diagnostic disable: undefined-global
-- seckill.lua 脚本
--
-- 接收的参数：
-- KEYS[1]: 秒杀库存的 Key (例如: "seckill:stock:1")
-- KEYS[2]: 秒杀用户集合的 Key (例如: "seckill:users:1")
-- ARGV[1]: 当前抢购的 userId

-- 1. 检查该用户是否已经抢购过
-- SISMEMBER: 检查 userId 是否在 "seckill:users:1" 集合中
if (redis.call('sismember', KEYS[2], ARGV[1]) == 1) then
    -- 如果是 1, 代表已抢过
    return 1 -- 1 代表: 重复抢购
end

-- 2. 获取当前库存
local stock = tonumber(redis.call('get', KEYS[1]))

-- 3. 检查库存
if (stock <= 0) then
    -- 如果库存不足
    return 2 -- 2 代表: 库存不足
end

-- 4. 【执行秒杀】
-- 4.1 扣减 Redis 库存 (原子操作)
redis.call('decr', KEYS[1])

-- 4.2 将用户ID添加到 "已抢购" 集合 (原子操作)
redis.call('sadd', KEYS[2], ARGV[1])

return 0 -- 0 代表: 抢购成功