package com.partners.weather.redis;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Objects;

@Slf4j
public class RedisPoolManager {
    private static JedisPool jedisPool;
    public static volatile RedisPoolManager redisPoolManager;

    public RedisPoolManager(JedisPool jedisPool) {
        RedisPoolManager.jedisPool = jedisPool;
    }

    public static void Init(JedisPool jedisPool) {
        if (Objects.isNull(redisPoolManager)) {
            synchronized (RedisPoolManager.class) {
                if (Objects.isNull(redisPoolManager)) {
                    redisPoolManager = new RedisPoolManager(jedisPool);
                }
            }
        }
    }

    public synchronized static Jedis getJedis() {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
        } catch (Exception e) {
            log.error("Error in {}", e);
        }
        if (Objects.isNull(jedis)) {
            throw new NullPointerException("Redis pool cannot be NULL.");
        }
        return jedis;
    }

    public synchronized static void close(Jedis client) {
        try {
            if (client != null && client.isConnected()) {
                client.quit();
                client.close();
            }
        } catch (Exception e) {
            log.error("Error in {}", e);
        }
    }
}
