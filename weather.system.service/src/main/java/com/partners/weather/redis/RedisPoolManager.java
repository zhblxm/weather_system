package com.partners.weather.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisPoolManager {
	private static final Logger logger = LoggerFactory.getLogger(RedisPoolManager.class);
	private static JedisPool jedisPool;
	public static volatile RedisPoolManager redisPoolManager;

	public RedisPoolManager(JedisPool jedisPool) {
		RedisPoolManager.jedisPool = jedisPool;
	}

	public static void Init(JedisPool jedisPool) {
		if (redisPoolManager == null) {
			synchronized (RedisPoolManager.class) {
				if (redisPoolManager == null) {
					redisPoolManager = new RedisPoolManager(jedisPool);
				}
			}
		}
	}

	public synchronized static Jedis getJedis() {
		Jedis jedis = null;
		try {
			if (jedisPool != null) {
				jedis = jedisPool.getResource();
			}
		} catch (Exception e) {
			logger.error("Error in {}", e);
		}
		if (jedis == null) {
			throw new NullPointerException("Redis pool cannot be NULL.");
		}
		return jedis;
	}

	public synchronized static void close(Jedis client) {
		try {
			if (client != null) {		
				if (client.isConnected()) {
					try {
						client.quit();
						client.disconnect();
					} catch (Exception e) {
						logger.error("Error in {}", e);
					}
				}
				client.close();
			}
		} catch (Exception e) {
			logger.error("Error in {}", e);
		}
	}
}
