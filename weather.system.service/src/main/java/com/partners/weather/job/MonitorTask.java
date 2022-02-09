package com.partners.weather.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.partners.entity.Emalilandsmssettings;
import com.partners.entity.Heartbeat;
import com.partners.entity.Notification;
import com.partners.entity.Weatherstation;
import com.partners.weather.common.CommonResources;
import com.partners.weather.common.NotificationType;
import com.partners.weather.mail.EmailHelper;
import com.partners.weather.service.IWeatherStationService;

public class MonitorTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MonitorTask.class);

    @Override
    public void run() {
        IWeatherStationService weatherStationService = TaskFactory.getInstance().getWeatherStationService();
        List<Weatherstation> weatherStations = weatherStationService.getAllWeatherStation();
        final DateTime now = DateTime.now();
        List<Heartbeat> heartbeats = weatherStationService.getHeartbeats();
        Map<String, Heartbeat> heartbeatMap = Maps.uniqueIndex(heartbeats, new Function<Heartbeat, String>() {
            @Override
            public String apply(Heartbeat input) {
                return input.getWeatherStationNumber();
            }
        });
        List<Notification> notifications = new ArrayList<>();
        Emalilandsmssettings settings = TaskFactory.getInstance().getEmailSettingService().getEmalilandsmssetting();
        Notification notification;
        weatherStations = weatherStations.stream().filter(weatherstation -> !heartbeatMap.containsKey(weatherstation.getWeatherStationNumber()) ||
                now.compareTo(heartbeatMap.get(weatherstation.getWeatherStationNumber()).getCreateDate().plusMinutes(CommonResources.ONOROFFLINECOUNT)) > 0).collect(Collectors.toList());

        for (Weatherstation weatherstation : weatherStations) {
            notification = Notification.builder().notificationType(NotificationType.DataMissAlert.getValue())
                    .notificationDesc(NotificationType.DataMissAlert.getDescription())
                    .message(String.format("站点编号为%s，长时间未收到数据", weatherstation.getWeatherStationNumber()))
                    .isChecked((byte) 1).build();
            EmailHelper.SendEmail(settings, notification, NotificationType.DataMissAlert);
            notifications.add(notification);
        }
        if (!notifications.isEmpty()) {
            TaskFactory.getInstance().getNotificationService().batchInserNotification(notifications);
        }
    }

}
