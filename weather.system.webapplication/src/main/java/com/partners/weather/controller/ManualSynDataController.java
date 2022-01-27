package com.partners.weather.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.partners.annotation.UserAction;
import com.partners.annotation.UserPermission;
import com.partners.annotation.UserPermissionEnum;
import com.partners.entity.Adminuser;
import com.partners.entity.Weatherstation;
import com.partners.entity.Weatherstationcategory;
import com.partners.weather.common.CommonResources;
import com.partners.weather.job.ManualSynDataTask;
import com.partners.weather.job.ScheduleFactory;
import com.partners.weather.service.IWeatherStationService;

@Controller
@RequestMapping("/manualsyndata")
@UserPermission(value = UserPermissionEnum.MANUALSYNDATA)
public class ManualSynDataController {
	@Autowired
	IWeatherStationService weatherStationService;
	
	@RequestMapping
	@UserPermission(value = UserPermissionEnum.SELECT)
	@UserAction(Action = UserPermissionEnum.SELECT, Description = "数据查询")
	public String manage(HttpServletRequest request) {
		Adminuser adminuser = (Adminuser) request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
		List<Weatherstationcategory> weatherstationcategories = weatherStationService.getAllWeatherStationCategory();
		weatherstationcategories = weatherStationService.filterWeatherStationCategorys(weatherstationcategories, adminuser.getWeatherStationGroup(), false);
		List<Weatherstation> weatherstations = weatherStationService.getAllWeatherStation();
		weatherstations = weatherStationService.filterWeatherStation(weatherstations, adminuser.getWeatherStation(), false);
		request.setAttribute("Categories", weatherstationcategories);
		request.setAttribute("Stations", weatherstations);
		request.setAttribute("StartDate", new DateTime().toString("yyyy-MM-dd HH:00:00"));
		request.setAttribute("EndDate", new DateTime().plusMonths(1).toString("yyyy-MM-dd HH:00:00"));
		return "manualsyndata";
	}
	
	@RequestMapping("/syndata")
	@UserPermission(value = UserPermissionEnum.SELECT)
	@UserAction(Action = UserPermissionEnum.SELECT, Description = "手动同步数据")
	@ResponseBody
	public String syndata(HttpServletRequest request) throws Exception {
		String synDateFrom, synDateTo;
		String station = request.getParameter("station");
		try {
			synDateFrom = request.getParameter("startdate");
			synDateTo = request.getParameter("enddate");
			station = request.getParameter("station");
			
			String[] stationArray = station.trim().split(",");
			List<Integer> stations = new ArrayList<>(stationArray.length);
			if (stationArray.length > 0) {			
				for (String p : stationArray) {
					stations.add(Integer.parseInt(p.trim()));
				}
			}
			
			DateTime now=new DateTime();
			now.plusSeconds(2);
			now = now.plusSeconds(2);
			String trigger=String.format("%d %d %d %d %d *",now.getSecondOfMinute(),now.getMinuteOfHour(),now.getHourOfDay(),now.getDayOfMonth(),now.getMonthOfYear());
			ScheduleFactory.addSchedule(CommonResources.TERMINALDATETASK, new CronTrigger(trigger),new ManualSynDataTask(synDateFrom, synDateTo, stations));
			
		} catch (Exception e) {
			return "0";
		}
		
		return "1";
	}
}
