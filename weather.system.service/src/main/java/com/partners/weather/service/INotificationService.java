package com.partners.weather.service;

import java.util.List;

import com.partners.entity.Notification;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VNotification;

public interface INotificationService {
    List<Notification> getNotifications(VNotification vnotification);

    ResponseMsg inserNotification(Notification notification);

    ResponseMsg updateNotification(int notificationId);

    ResponseMsg batchInserNotification(List<Notification> notifications);

    ResponseMsg batchUpdateNotification(List<Integer> notifications);

    int getNotificationCount();
}
