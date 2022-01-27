package com.partners.weather.controller;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.partners.annotation.UserPermissionEnum;
import com.partners.entity.Adminuser;
import com.partners.entity.Heartbeat;
import com.partners.entity.MessageNotice;
import com.partners.entity.Notification;
import com.partners.entity.Permission;
import com.partners.view.entity.VMessageNotice;
import com.partners.weather.common.CommonResources;
import com.partners.weather.encrypt.HexUtil;
import com.partners.weather.redis.RedisPoolManager;
import com.partners.weather.serialize.ListSerializeTransfer;
import com.partners.weather.service.IMessageNoticeService;
import com.partners.weather.service.IPermissionService;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Controller
@RequestMapping("/home")
public class HomeController {

	@Resource
	IPermissionService permissionService;
	@Autowired
	JedisPool jedisPool;
	@Autowired
	IMessageNoticeService messageNoticeService;

	@RequestMapping
	public String home(HttpServletRequest request) throws ParseException {
		return "home";
	}
	@RequestMapping("/info")
	public String info(HttpServletRequest request) throws ParseException {
		return "info";
	}
	@RequestMapping("/header")
	public String header(HttpServletRequest request) throws ParseException {
//		Adminuser adminuser = (Adminuser) request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
//		List<Permission> permissions = null;
		request.setAttribute("Notification", 0);
//		if (!adminuser.getPermissions().isEmpty()) {
//			permissions = permissionService.getUserNavPermissions(adminuser.getPermissions());
//			Map<Integer, Permission> categoryMap = Maps.uniqueIndex(permissions, new Function<Permission, Integer>() {
//				@Override
//				public Integer apply(Permission input) {
//					return input.getPermissionId();
//				}
//			});
//			if(categoryMap.containsKey(UserPermissionEnum.NOTIFICATION.getId()))
//			{
//				request.setAttribute("Notification", 1);
//			}
//		}
		return "pageheader";
	}

	@RequestMapping("/navigation")
	public String navigation(HttpServletRequest request, HttpServletResponse response) {
		Adminuser adminuser = (Adminuser) request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
		List<Permission> permissions = null;
		if (!adminuser.getPermissions().isEmpty()) {
			permissions = permissionService.getUserNavPermissions(adminuser.getPermissions());
		}
		request.setAttribute("navigations", permissions == null ? new ArrayList<Permission>(0) : permissions);
		return "navigation";
	}
	
	@RequestMapping("/pageline")
	public String line(HttpServletRequest request, HttpServletResponse response) {
		return "pageline";
	}

	@RequestMapping("/notifications")
	@ResponseBody
	public List<MessageNotice> getNotifications(HttpServletRequest request, HttpServletResponse response) {
		/*
		Jedis client = null;
		List<Notification> cacheNotifications = new ArrayList<>();
		try {
			RedisPoolManager.Init(jedisPool);
			client = RedisPoolManager.getJedis();
			ListSerializeTransfer<Heartbeat> lsSerializeTransfer = new ListSerializeTransfer<>();
			byte[] key = CommonResources.NOTIFICATIONS.getBytes();
			if (client.exists(key)) {
				cacheNotifications = (List<Notification>) lsSerializeTransfer.deserialize(client.get(key));
			}
			if (!cacheNotifications.isEmpty()) {
				Collection<Notification> notificationFilterList = Collections2.filter(cacheNotifications, new Predicate<Notification>() {
					public boolean apply(Notification notification) {
						return notification.getIsChecked() == (byte) 1 ? true : false;
					}
				});
				cacheNotifications = Lists.newArrayList(notificationFilterList);
			}
		} finally {
			RedisPoolManager.close(client);
		}
		return cacheNotifications;
		*/
		
		VMessageNotice vMessageNotice = new VMessageNotice(null, "", 0, "", "", 0, 1);
		List<MessageNotice> messageNotices = messageNoticeService.getMessageNotices(vMessageNotice);
		for (MessageNotice messageNotice : messageNotices) {
			messageNotice.setUniqueMessageNoticeId(HexUtil.IntToHex(messageNotice.getMessageNoticeId()));
			messageNotice.setMessageNoticeId(0);
		}
		messageNotices = messageNotices == null ? new ArrayList<MessageNotice>(0) : messageNotices;
		return messageNotices;
	}
}
