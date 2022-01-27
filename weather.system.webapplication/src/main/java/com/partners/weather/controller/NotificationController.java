package com.partners.weather.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.partners.annotation.UserAction;
import com.partners.annotation.UserPermission;
import com.partners.annotation.UserPermissionEnum;
import com.partners.entity.Heartbeat;
import com.partners.entity.JsonResult;
import com.partners.entity.Notification;
import com.partners.view.entity.VNotification;
import com.partners.weather.common.CommonResources;
import com.partners.weather.redis.RedisPoolManager;
import com.partners.weather.request.RequestHelper;
import com.partners.weather.serialize.ListSerializeTransfer;
import com.partners.weather.service.INotificationService;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Controller
@RequestMapping("/notification")	
@UserPermission(value=UserPermissionEnum.NOTIFICATION)
public class NotificationController {
	
	@Resource
	INotificationService notificationService;
	@Autowired
	JedisPool jedisPool;
	@RequestMapping
	@UserPermission(value = UserPermissionEnum.NOTIFICATION)
	@UserAction(Action = UserPermissionEnum.NOTIFICATION, Description = "查询系统通知")
	public String manage(HttpServletRequest request) {
		return "notifications";
	}
	
	@RequestMapping("/logs")	
	@ResponseBody
	public JsonResult logs(HttpServletRequest request) {
		Jedis client = null;
		VNotification vnotification=new VNotification(RequestHelper.prepareRequest(request,true));
		List<Notification> notifications=notificationService.getNotifications(vnotification);
		int draw = StringUtils.isBlank(request.getParameter("draw")) ? 1 : Integer.valueOf(request.getParameter("draw"));
		if(notifications.size()>0)
		{
			List<Integer> notificationIdList=new ArrayList<>(notifications.size());
			for (Notification nt : notifications) {
				notificationIdList.add(nt.getNotificationId());
			}
			notificationService.batchUpdateNotification(notificationIdList);
			try {
				RedisPoolManager.Init(jedisPool);
				client = RedisPoolManager.getJedis();
				List<Notification> cacheNotifications = new ArrayList<>();
				ListSerializeTransfer<Heartbeat> lsSerializeTransfer = new ListSerializeTransfer<>();
				byte[] key = CommonResources.NOTIFICATIONS.getBytes();
				if (client.exists(key)) {
					cacheNotifications = (List<Notification>) lsSerializeTransfer.deserialize(client.get(key));
				}
				List<Notification> newCacheNotifications=new ArrayList<>(cacheNotifications.size());
				for (Notification nt : cacheNotifications) {
					if(!notificationIdList.contains(nt.getNotificationId())){
						newCacheNotifications.add(nt);
					}
				}
				client.set(key, lsSerializeTransfer.serialize(newCacheNotifications));
			} finally {
				RedisPoolManager.close(client);
			}
		}
		int count = notificationService.getNotificationCount();
		JsonResult jsonResult = new JsonResult();
		jsonResult.setDraw(draw);
		jsonResult.setRecordsTotal(count);
		jsonResult.setRecordsFiltered(count);
		jsonResult.setData(notifications.toArray());	
	

		
		return jsonResult;
	}
}