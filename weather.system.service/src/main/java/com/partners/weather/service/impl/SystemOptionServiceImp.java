package com.partners.weather.service.impl;

import com.google.common.collect.Lists;

import com.partners.entity.SystemOption;
import com.partners.view.entity.ResponseMsg;
import com.partners.weather.common.RedisKey;
import com.partners.weather.dao.ISystemOptionDAO;
import com.partners.weather.redis.RedisPoolManager;
import com.partners.weather.serialize.ObjectSerializeTransfer;
import com.partners.weather.service.ISystemOptionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
@Transactional
@Slf4j
public class SystemOptionServiceImp implements ISystemOptionService {
    @Autowired
    private ISystemOptionDAO systemOptionDAO;
    @Autowired
    JedisPool jedisPool;

    @Override
    public List<SystemOption> getSystemOptions() {
        try {
            return systemOptionDAO.getSystemOptions();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Lists.newArrayListWithCapacity(0);
    }

    @Override
    public SystemOption getSystemOption(String optionId) {
        RedisPoolManager.Init(jedisPool);
        Jedis client = RedisPoolManager.getJedis();
        ObjectSerializeTransfer<SystemOption> objectSerializeTransfer = new ObjectSerializeTransfer<>();
        byte[] optionIdBs = optionId.getBytes();
        String defaultMainTitle = "气象信息服务平台";
        try {
            if (client.exists(optionIdBs)) {
                return (SystemOption) objectSerializeTransfer.deserialize(client.get(optionIdBs));
            }
            SystemOption systemOption = systemOptionDAO.getSystemOption(optionId);
            if (Objects.isNull(systemOption)) {
                systemOption = SystemOption.builder().optionId(RedisKey.LOGINMAINTITLE)
                        .optionValue(defaultMainTitle)
                        .isSystem('N').build();
                this.addSystemOption(systemOption);
            }
            client.set(optionIdBs, objectSerializeTransfer.serialize(systemOption));
            return systemOption;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            RedisPoolManager.close(client);
        }
        return null;
    }

    @Override
    public ResponseMsg addSystemOption(SystemOption systemOption) {
        ResponseMsg responseMsg = new ResponseMsg();
        responseMsg.setStatusCode(0);
        RedisPoolManager.Init(jedisPool);
        Jedis client = RedisPoolManager.getJedis();
        try {
            if (client.exists(systemOption.getOptionId().getBytes())) {
                client.del(systemOption.getOptionId().getBytes());
            }
            ObjectSerializeTransfer<SystemOption> objectSerializeTransfer = new ObjectSerializeTransfer<>();
            systemOptionDAO.insertSystemOption(systemOption);
            client.set(systemOption.getOptionId().getBytes(), objectSerializeTransfer.serialize(systemOption));
        } catch (Exception e) {
            responseMsg.setStatusCode(1);
            responseMsg.setMessage(e.getMessage());
            log.error(e.getMessage(), e);
        } finally {
            RedisPoolManager.close(client);
        }
        return responseMsg;
    }

    @Override
    public SystemOption getSystemOption(List<SystemOption> systemOptions, String optionId) {
        AtomicReference<SystemOption> systemOptionRef = new AtomicReference<>(null);
        if (CollectionUtils.isEmpty(systemOptions)) {
            return null;
        }
        try {
            systemOptions.stream().filter(optionItem -> optionId.equals(optionItem.getOptionId())).findFirst().ifPresent(systemOptionRef::set);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return systemOptionRef.get();
    }

    @Override
    public ResponseMsg delSystemOption(String optionId) {
        ResponseMsg responseMsg = new ResponseMsg();
        responseMsg.setStatusCode(0);
        Jedis client = RedisPoolManager.getJedis();
        try {
            systemOptionDAO.delSystemOption(optionId);
            if (client.exists(optionId.getBytes())) {
                client.del(optionId.getBytes());
            }
        } catch (Exception ex) {
            responseMsg.setMessage("删除系统失败！");
            responseMsg.setStatusCode(1);
            log.error(ex.getLocalizedMessage(), ex);
        } finally {
            RedisPoolManager.close(client);
        }
        return responseMsg;
    }
}
