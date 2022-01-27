package com.partners.weather.dao;

import java.util.List;

import com.partners.entity.Notification;
import com.partners.view.entity.VNotification;

public interface INotificationDAO {

	public List<Notification> getNotifications(VNotification vnotification);

	public int inserNotification(Notification notification);

	public void updateNotification(int notificationId);
	
	public void batchUpdateNotification(List<Integer> notifications);
	
	public int getNotificationCount();
}
