package com.partners.weather.redis;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.RedisTemplate;

import com.partners.weather.service.impl.ParseReqeustServiceImp;

@Slf4j
public class RedisQueue<T> implements InitializingBean, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(RedisQueue.class);
    private RedisTemplate redisTemplate;
    private String key;
    private byte[] rawKey;
    private RedisConnectionFactory factory;
    private RedisConnection connection;
    private BoundListOperations<String, T> listOperations;
    private Lock lock = new ReentrantLock();
    private RedisQueueListener listener;
    private Thread listenerThread;
    private boolean isClosed;

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setListener(RedisQueueListener listener) {
        this.listener = listener;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        factory = redisTemplate.getConnectionFactory();
        connection = RedisConnectionUtils.getConnection(factory);
        rawKey = redisTemplate.getKeySerializer().serialize(key);
        listOperations = redisTemplate.boundListOps(key);
        if (listener != null) {
            listenerThread = new ListenerThread();
            listenerThread.setDaemon(true);
            listenerThread.start();
        }
    }

    public T takeFromTail(int timeout) throws InterruptedException {
        lock.lockInterruptibly();
        try {
            List<byte[]> results = connection.bRPop(timeout, rawKey);
            if (CollectionUtils.isEmpty(results)) {
                return null;
            }
            return (T) redisTemplate.getValueSerializer().deserialize(results.get(1));
        } finally {
            lock.unlock();
        }
    }

    public T takeFromTail() throws InterruptedException {
        return takeFromTail(0);
    }

    public void pushFromHead(T value) {
        listOperations.leftPush(value);
    }

    public void pushFromTail(T value) {
        listOperations.rightPush(value);
    }

    public T removeFromHead() {
        return listOperations.leftPop();
    }

    public T removeFromTail() {
        return listOperations.rightPop();
    }

    public T takeFromHead(int timeout) throws InterruptedException {
        lock.lockInterruptibly();
        try {
            List<byte[]> results = connection.bLPop(timeout, rawKey);
            if (CollectionUtils.isEmpty(results)) {
                return null;
            }
            return (T) redisTemplate.getValueSerializer().deserialize(results.get(1));
        } finally {
            lock.unlock();
        }
    }

    public T takeFromHead() throws InterruptedException {
        return takeFromHead(0);
    }

    @Override
    public void destroy() throws Exception {
        if (isClosed) {
            return;
        }
        shutdown();
        RedisConnectionUtils.releaseConnection(connection, factory);
    }

    private void shutdown() {
        try {
            listenerThread.interrupt();
        } catch (Exception e) {
        }
    }

    class ListenerThread extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    T value = takeFromHead();
                    if (Objects.nonNull(value)) {
                        listener.onMessage(value);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

        }
    }

}