package com.partners.weather.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.partners.annotation.UserAction;
import com.partners.annotation.UserPermission;
import com.partners.annotation.UserPermissionEnum;
import com.partners.entity.Adminuser;
import com.partners.entity.TerminalParametersAttrs;
import com.partners.entity.Weatherstation;
import com.partners.entity.Weatherstationcategory;
import com.partners.view.entity.VImageMonitor;
import com.partners.weather.common.CommonResources;
import com.partners.weather.service.IImageMonitorService;
import com.partners.weather.service.IWeatherStationService;

@Controller
@RequestMapping("/imagemonitor")
@UserPermission(value = UserPermissionEnum.ANALYSIS)
public class ImageMonitorController {
	@Autowired
	IWeatherStationService weatherStationService;
	@Autowired
	IImageMonitorService imageMonitorService;

	@RequestMapping
	@UserPermission(value = UserPermissionEnum.IMAGEMONITOR)
	@UserAction(Action = UserPermissionEnum.IMAGEMONITOR, Description = "图像监控")
	public String Index(HttpServletRequest request) {
		Adminuser adminuser = (Adminuser) request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
		List<Weatherstationcategory> weatherstationcategories = weatherStationService.getAllWeatherStationCategory();
		weatherstationcategories = weatherStationService.filterWeatherStationCategorys(weatherstationcategories, adminuser.getWeatherStationGroup(), false);
		List<Weatherstation> weatherstations = weatherStationService.getAllWeatherStation();
		weatherstations = weatherStationService.filterWeatherStation(weatherstations, adminuser.getWeatherStation(), false);
//		String imagePath =  request.getSession().getServletContext().getRealPath("/resources/images/");
//		List<VImageMonitor> imageMonitors = imageMonitorService.getImageMonitors(null, imagePath);
		
		Map<Integer, Weatherstationcategory> categoryMap = Maps.uniqueIndex(weatherstationcategories, new Function<Weatherstationcategory, Integer>() {
			@Override
			public Integer apply(Weatherstationcategory input) {
				return input.getWeatherStationCategoryId();
			}
		});
		List<Integer> stationList=new ArrayList();
		for (Weatherstation weatherstation : weatherstations) {
			if(!stationList.contains(weatherstation.getWeatherStationCategoryId())){
				stationList.add(weatherstation.getWeatherStationCategoryId());
			}
		}
		Iterator<Integer> iterator = categoryMap.keySet().iterator();
		weatherstationcategories.clear();
		int key;
		while (iterator.hasNext()) {
			key = iterator.next();
			if (stationList.contains(key)) {
				weatherstationcategories.add(categoryMap.get(key));
			}
		}
		
//		for(VImageMonitor item : imageMonitors){
//			Weatherstation station = stationByNumberMap.get(item.getWeatherStationNumber());
//			if(station != null){
//				item.setWeatherStationCategoryId(station.getWeatherStationCategoryId());
//				item.setWeatherStationId(station.getWeatherStationId());
//				item.setWeatherStationName(station.getWeatherStationName());
//			}
//		}
		request.setAttribute("Categories", weatherstationcategories);
		request.setAttribute("Stations", weatherstations);
//		request.setAttribute("ImageMonitor", imageMonitors);
		
		return "imagemonitor";
	}
	
	@RequestMapping("/getimage")
	@UserPermission(value = UserPermissionEnum.SELECT)
	@UserAction(Action = UserPermissionEnum.SELECT, Description = "数据查询")
	@ResponseBody
	public List<VImageMonitor> getImageInfo(HttpServletRequest request) throws Exception {
		List<VImageMonitor> ImageInfos=new ArrayList<>();
		String wsNumber = request.getParameter("wsNumber");
		String wsCategoryId = request.getParameter("wsCategoryId");
		List<String> stationNumbers = new ArrayList<String>();
		
		List<Integer> categories = new ArrayList<>();
		categories.add(Integer.parseInt(wsCategoryId));
		List<Weatherstation> weatherstations = weatherStationService.getWeatherStationsByCategory(categories);
		
		if(!StringUtils.isBlank(wsNumber)) {
			stationNumbers.add(wsNumber);
		} else if(!StringUtils.isBlank(wsCategoryId)) {
			for(Weatherstation item : weatherstations) {
				stationNumbers.add(item.getWeatherStationNumber());
			}
		}
		else{
			stationNumbers = null;
		}
		
		Map<String, Weatherstation> stationByNumberMap = Maps.uniqueIndex(weatherstations, new Function<Weatherstation, String>() {
			@Override
			public String apply(Weatherstation input) {
				return input.getWeatherStationNumber();
			}
		});
		
		String imagePath =  request.getSession().getServletContext().getRealPath("/resources/images/");
		ImageInfos = imageMonitorService.getImageMonitors(stationNumbers, imagePath);
		
		for(VImageMonitor item : ImageInfos){
			Weatherstation station = stationByNumberMap.get(item.getWeatherStationNumber());
			if(station != null){
				item.setWeatherStationCategoryId(station.getWeatherStationCategoryId());
				item.setWeatherStationId(station.getWeatherStationId());
				item.setWeatherStationName(station.getWeatherStationName());
			}
		}
		
		return ImageInfos;
	}
}
