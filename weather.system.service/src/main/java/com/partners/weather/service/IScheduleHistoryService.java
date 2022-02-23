package com.partners.weather.service;

import java.util.List;

import com.partners.entity.Schedulehistory;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VSchedule;

public interface IScheduleHistoryService {
    ResponseMsg insertHistory(Schedulehistory schedulehistory);

    List<Schedulehistory> getSchedules(VSchedule vschedule);

    int getScheduleCount();
}
