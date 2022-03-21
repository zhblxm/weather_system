package com.partners.weather.dao;

import java.util.List;

import com.partners.entity.Notification;
import com.partners.view.entity.VNotification;

interface INotificationDAO {

	List<Notification> getNotifications(VNotification vnotification);

	int inserNotification(Notification notification);

	void updateNotification(int notificationId);
	
	void batchUpdateNotification(List<Integer> notifications);
	
	int getNotificationCount();
}
