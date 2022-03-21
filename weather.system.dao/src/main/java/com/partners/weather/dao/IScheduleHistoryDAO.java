package com.partners.weather.dao;

import java.util.List;

import com.partners.entity.Schedulehistory;
import com.partners.view.entity.VSchedule;

interface IScheduleHistoryDAO {
	
	int insertHistory(Schedulehistory schedulehistory);
	
	List<Schedulehistory> getSchedules(VSchedule vschedule);
	
	int getScheduleCount();
}
