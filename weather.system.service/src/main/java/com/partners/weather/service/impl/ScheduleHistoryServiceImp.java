package com.partners.weather.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.partners.entity.Schedulehistory;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VSchedule;
import com.partners.weather.dao.IScheduleHistoryDAO;
import com.partners.weather.service.IScheduleHistoryService;

@Service
@Transactional
public class ScheduleHistoryServiceImp implements IScheduleHistoryService {
	private static final Logger logger = LoggerFactory.getLogger(ScheduleHistoryServiceImp.class);
	@Autowired
	private IScheduleHistoryDAO scheduleHistoryDAO;

	@Override
	public ResponseMsg insertHistory(Schedulehistory schedulehistory) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		try {
			scheduleHistoryDAO.insertHistory(schedulehistory);
		} catch (Exception ex) {
			responseMsg.setMessage("添加任务历史记录失败！");
			responseMsg.setStatusCode(1);
			logger.error("Erron in {}", ex);
		}
		return responseMsg;
	}

	@Override
	public List<Schedulehistory> getSchedules(VSchedule vschedule) {
		List<Schedulehistory> schedulehistories = null;
		try {
			schedulehistories = scheduleHistoryDAO.getSchedules(vschedule);
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		return schedulehistories == null ? new ArrayList<Schedulehistory>(0) : schedulehistories;
	}

	@Override
	public int getScheduleCount() {
		int count=0;
		try {
			count=scheduleHistoryDAO.getScheduleCount();
		} catch (Exception e) {
			logger.error("Error in {}", e);
		}
		return count;
	}
}
