package com.partners.weather.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.partners.annotation.UserAction;
import com.partners.annotation.UserPermission;
import com.partners.annotation.UserPermissionEnum;
import com.partners.entity.JsonResult;
import com.partners.entity.Schedulehistory;
import com.partners.view.entity.VSchedule;
import com.partners.weather.request.RequestHelper;
import com.partners.weather.service.IScheduleHistoryService;

@Controller
@RequestMapping("/schedule")	
@UserPermission(value=UserPermissionEnum.SCHEDULE)
public class ScheduleController {
	
	@Resource
	IScheduleHistoryService scheduleHistoryService;
 
	@RequestMapping
	@UserPermission(value = UserPermissionEnum.SCHEDULE)
	@UserAction(Action = UserPermissionEnum.SCHEDULE, Description = "任务执行报表")
	public String manage(HttpServletRequest request) {
		return "schedules";
	}
	
	@RequestMapping("/logs")	
	@ResponseBody
	public JsonResult logs(HttpServletRequest request) {
		VSchedule vSchedule=new VSchedule(RequestHelper.prepareRequest(request,true));
		List<Schedulehistory> scheduleHistories=scheduleHistoryService.getSchedules(vSchedule);
		int draw = StringUtils.isBlank(request.getParameter("draw")) ? 1 : Integer.parseInt(request.getParameter("draw"));
		int count = scheduleHistoryService.getScheduleCount();
		JsonResult jsonResult = new JsonResult();
		jsonResult.setDraw(draw);
		jsonResult.setRecordsTotal(count);
		jsonResult.setRecordsFiltered(count);
		jsonResult.setData(scheduleHistories.toArray());
		return jsonResult;
	}
}