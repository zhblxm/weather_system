package com.partners.weather.service;

import java.util.List;

import com.partners.entity.Notification;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VNotification;

public interface INotificationService {
	public List<Notification> getNotifications(VNotification vnotification);

	public ResponseMsg inserNotification(Notification notification);

	public ResponseMsg updateNotification(int notificationId);
	
	public ResponseMsg batchInserNotification(List<Notification> notifications);
	
	public ResponseMsg batchUpdateNotification(List<Integer> notifications);
	
	public int getNotificationCount();
}
