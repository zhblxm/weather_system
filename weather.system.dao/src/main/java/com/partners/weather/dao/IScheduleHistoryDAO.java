package com.partners.weather.dao;

import java.util.List;

import com.partners.entity.Schedulehistory;
import com.partners.view.entity.VSchedule;

public interface IScheduleHistoryDAO {
	
	public int insertHistory(Schedulehistory schedulehistory);
	
	public List<Schedulehistory> getSchedules(VSchedule vschedule);
	
	public int getScheduleCount();
}
