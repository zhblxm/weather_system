package com.partners.weather.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.partners.entity.Heartbeat;
import com.partners.entity.Notification;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VNotification;
import com.partners.weather.common.CommonResources;
import com.partners.weather.dao.INotificationDAO;
import com.partners.weather.redis.RedisPoolManager;
import com.partners.weather.serialize.ListSerializeTransfer;
import com.partners.weather.service.INotificationService;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
@Transactional
public class NotificationServiceImp implements INotificationService {
	private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImp.class);
 
	@Autowired
	private INotificationDAO notificationDAO;
	@Autowired
	JedisPool jedisPool;
	@Override
	public List<Notification> getNotifications(VNotification vnotification) {
		List<Notification> notifications = null;
		try {
			notifications = notificationDAO.getNotifications(vnotification);
		} catch (Exception e) {
			logger.error("Error in {}", e);
		}
		return notifications == null ? new ArrayList<Notification>(0) : notifications;
	}

	@Override
	public ResponseMsg inserNotification(Notification notification) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		try {
			notificationDAO.inserNotification(notification);
		} catch (Exception e) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage(e.getMessage());
			logger.error("Error in {}", e);
		}
		return responseMsg;
	}

	@Override
	public ResponseMsg updateNotification(int notificationId) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		try {
			notificationDAO.updateNotification(notificationId);
		} catch (Exception e) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage(e.getMessage());
			logger.error("Error in {}", e);
		}
		return responseMsg;
	}

	@Override
	public ResponseMsg batchInserNotification(List<Notification> notifications) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		if(notifications.isEmpty()){
			return responseMsg;
		}
		Jedis client = null;
		try {			
//			RedisPoolManager.Init(jedisPool);
//			client = RedisPoolManager.getJedis();
//			ListSerializeTransfer<Heartbeat> lsSerializeTransfer = new ListSerializeTransfer<>();
//			List<Notification> cacheNotifications = new ArrayList<>();
//			byte[] key = CommonResources.NOTIFICATIONS.getBytes();
//			if (client.exists(key)) {
//				cacheNotifications = (List<Notification>) lsSerializeTransfer.deserialize(client.get(key));
//			}
			for (Notification notification : notifications) {
				this.inserNotification(notification);			
			}
			//cacheNotifications.addAll(notifications);			
			//client.set(key, lsSerializeTransfer.serialize(cacheNotifications));	
		} catch (Exception ex) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("添加站点失败！");
			logger.error("Erron in {}", ex);
		} finally {
			RedisPoolManager.close(client);
		}
		return responseMsg;
	}

	@Override
	public ResponseMsg batchUpdateNotification(List<Integer> notifications) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		try {
			notificationDAO.batchUpdateNotification(notifications);
		} catch (Exception e) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage(e.getMessage());
			logger.error("Error in {}", e);
		}
		return responseMsg;
	}

	@Override
	public int getNotificationCount() {
		int count=0;
		try {
			count=notificationDAO.getNotificationCount();
		} catch (Exception e) {
			logger.error("Error in {}", e);
		}
		return count;
	}
	 
}
