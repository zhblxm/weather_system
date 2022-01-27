package com.partners.weather.service;

import java.util.List;

import com.partners.entity.Schedulehistory;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VSchedule;

public interface IScheduleHistoryService {
	public ResponseMsg insertHistory(Schedulehistory schedulehistory);

	public List<Schedulehistory> getSchedules(VSchedule vschedule);

	public int getScheduleCount();
}
