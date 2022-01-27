package com.partners.weather.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.partners.entity.SystemOption;
import com.partners.view.entity.ResponseMsg;
import com.partners.weather.common.RedisKey;
import com.partners.weather.dao.ISystemOptionDAO;
import com.partners.weather.redis.RedisPoolManager;
import com.partners.weather.serialize.ObjectSerializeTransfer;
import com.partners.weather.service.ISystemOptionService;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
@Transactional
public class SystemOptionServiceImp implements ISystemOptionService {
	private static final Logger logger = LoggerFactory.getLogger(SystemOptionServiceImp.class);
	@Autowired
	private ISystemOptionDAO systemOptionDAO;
	@Autowired
	JedisPool jedisPool;

	@Override
	public List<SystemOption> getSystemOptions() {
		List<SystemOption> systemOptions = null;
		try {
			systemOptions = systemOptionDAO.getSystemOptions();
		} catch (Exception e) {
			logger.error("Error in {}", e);
		}
		return systemOptions == null ? new ArrayList<SystemOption>(0) : systemOptions;
	}

	@Override
	public SystemOption getSystemOption(String optionId) {
		SystemOption systemOption = null;
		Jedis client = null;
		byte[] optionIdBs=optionId.getBytes();
		String defaultMainTitle="气象信息服务平台";
		try {
			RedisPoolManager.Init(jedisPool);
			client = RedisPoolManager.getJedis();
			ObjectSerializeTransfer<SystemOption> objectSerializeTransfer = new ObjectSerializeTransfer<>();
			if (client.exists(optionIdBs)) {
				systemOption=(SystemOption) objectSerializeTransfer.deserialize(client.get(optionIdBs));
			}else {
				systemOption=systemOptionDAO.getSystemOption(optionId);
				if(systemOption==null)
				{
					systemOption=new SystemOption();
					systemOption.setOptionId(RedisKey.LOGINMAINTITLE);
					systemOption.setOptionValue(defaultMainTitle);
					systemOption.setIsSystem('N');
					this.addSystemOption(systemOption);
				}else {
					if (!client.exists(optionIdBs)) {
						client.set(optionIdBs, objectSerializeTransfer.serialize(systemOption));
					}					
				}
				
			}			
		} catch (Exception e) {
			logger.error("Error in {}", e);
		}finally {
			RedisPoolManager.close(client);
		}
		return systemOption;
	}

	@Override
	public ResponseMsg addSystemOption(SystemOption systemOption) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		Jedis client = null;
		try {
			RedisPoolManager.Init(jedisPool);
			client = RedisPoolManager.getJedis();
			if (client.exists(systemOption.getOptionId().getBytes())) {
				client.del(systemOption.getOptionId().getBytes());
			}
			ObjectSerializeTransfer<SystemOption> objectSerializeTransfer = new ObjectSerializeTransfer<>();
			systemOptionDAO.insertSystemOption(systemOption);
			client.set(systemOption.getOptionId().getBytes(), objectSerializeTransfer.serialize(systemOption));
		} catch (Exception e) {
			responseMsg.setStatusCode(1);		
			responseMsg.setMessage(e.getMessage());
			logger.error("Error in {}", e);
		}finally {
			RedisPoolManager.close(client);
		}
		return responseMsg;
	}

	@Override
	public SystemOption getSystemOption(List<SystemOption> systemOptions,String optionId) {
		SystemOption systemOption=null;
		if(systemOptions==null)
		{
			return systemOption;
		}
		try {
			for (SystemOption optionItem : systemOptions) {
				if(optionId.equals(optionItem.getOptionId()))
				{
					systemOption=optionItem;
					break;
				}
			}
		} catch (Exception e) {
			logger.error("Error in {}", e);
		}
		return systemOption;
	}

	@Override
	public ResponseMsg delSystemOption(String optionId) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		Jedis client = null;
		try {
			client = RedisPoolManager.getJedis();
			systemOptionDAO.delSystemOption(optionId);
			if (client.exists(optionId.getBytes())) {
				client.del(optionId.getBytes());
			}
		} catch (Exception ex) {
			responseMsg.setMessage("删除系统失败！");
			responseMsg.setStatusCode(1);
			logger.error("Erron in {}", ex);
		}finally {
			RedisPoolManager.close(client);
		}
		return responseMsg;
	}
}
