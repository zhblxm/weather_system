package com.partners.weather.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.partners.annotation.UserAction;
import com.partners.annotation.UserPermission;
import com.partners.annotation.UserPermissionEnum;
import com.partners.entity.Adminuser;
import com.partners.entity.JsonResult;
import com.partners.entity.Statistics;
import com.partners.entity.Weatherstation;
import com.partners.entity.Weatherstationcategory;
import com.partners.view.entity.VStatistics;
import com.partners.weather.common.CommonResources;
import com.partners.weather.request.RequestHelper;
import com.partners.weather.service.IWeatherStationService;
import com.partners.weather.service.IWeatherStatisticsService;

@Controller
@RequestMapping("/statistics")
@UserPermission(value = UserPermissionEnum.STATISTICS)
public class StatisticsController {
	@Autowired
	IWeatherStatisticsService statisticsService;
	@Autowired
	IWeatherStationService weatherStationService;

	@RequestMapping
	@UserPermission(value = UserPermissionEnum.STATISTICS)
	@UserAction(Action = UserPermissionEnum.STATISTICS, Description = "数据统计查询")
	public String manage(HttpServletRequest request) {
		Adminuser adminuser = (Adminuser) request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
		List<Weatherstationcategory> weatherstationcategories = weatherStationService.getAllWeatherStationCategory();	
		weatherstationcategories=weatherStationService.filterWeatherStationCategorys(weatherstationcategories, adminuser.getWeatherStationGroup(),false);
		List<Weatherstation> weatherstations = weatherStationService.getAllWeatherStation();
		weatherstations=weatherStationService.filterWeatherStation(weatherstations, adminuser.getWeatherStation(),false);		
		request.setAttribute("Categories", weatherstationcategories);
		request.setAttribute("Stations", weatherstations);
		request.setAttribute("StartDate", new DateTime().toString("yyyy-MM-dd HH:mm:ss"));
		request.setAttribute("EndDate", new DateTime().plusMonths(1).toString("yyyy-MM-dd HH:mm:ss"));
		return "statistics";
	}

	@RequestMapping("/stations")
	@UserPermission(value = UserPermissionEnum.STATISTICS)
	@UserAction(Action = UserPermissionEnum.STATISTICS, Description = "数据统计查询")
	@ResponseBody
	public JsonResult stations(HttpServletRequest request) throws Exception {
		JsonResult jsonResult = new JsonResult();
		jsonResult.setDraw(0);
		jsonResult.setRecordsTotal(0);
		jsonResult.setRecordsFiltered(0);
		List<Statistics> weatherStations = new ArrayList<>(0);
		jsonResult.setData(weatherStations.toArray());
		String station = request.getParameter("station");
		if (StringUtils.isBlank(station)) {
			return jsonResult;
		}
		try {
			DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
			DateTime.parse(request.getParameter("startdate"), format);
			DateTime.parse(request.getParameter("enddate"), format);
		} catch (Exception e) {
			return jsonResult;
		}
		VStatistics statistics = new VStatistics(RequestHelper.prepareRequest(request,true));
		statistics.setStartDate(request.getParameter("startdate"));
		statistics.setEndDate(request.getParameter("enddate"));
		String[] stations = station.trim().split(",");
		if (stations.length > 0) {
			List<Integer> stationList = new ArrayList<>(stations.length);
			for (String p : stations) {
				stationList.add(Integer.parseInt(p.trim()));
			}
			statistics.setWeatherStations(stationList);
		}
		weatherStations = statisticsService.getStatistics(statistics);
		int draw = StringUtils.isBlank(request.getParameter("draw")) ? 1 : Integer.valueOf(request.getParameter("draw"));
		int count = statistics.getWeatherStations().size();
		jsonResult.setDraw(draw);
		jsonResult.setRecordsTotal(count);
		jsonResult.setRecordsFiltered(count);
		jsonResult.setData(weatherStations.toArray());
		return jsonResult;
	}
	@RequestMapping("/download")
	public void download(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String station = request.getParameter("station");
		if (StringUtils.isBlank(station)) {
			return;
		}
		VStatistics statistics = new VStatistics(RequestHelper.prepareRequest(request,true));
		try {
			statistics.setStartDate(request.getParameter("startdate"));
			statistics.setEndDate(request.getParameter("enddate"));
			DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
			DateTime.parse(statistics.getStartDate(), format);
			DateTime.parse(statistics.getEndDate(), format);
		} catch (Exception e) {
			return;
		}
		String[] stations = station.trim().split(",");
		if (stations.length > 0) {
			List<Integer> stationList = new ArrayList<>(stations.length);
			for (String p : stations) {
				stationList.add(Integer.parseInt(p.trim()));
			}
			statistics.setWeatherStations(stationList);
		}
		statistics.setSize(10000);
		List<Statistics> weatherStations = statisticsService.getStatistics(statistics);
		File downloadFile = new File(request.getSession().getServletContext().getRealPath("/resources/download/及逾时统计(" + System.currentTimeMillis() + ").csv"));
		try (FileWriter fw = new FileWriter(downloadFile)) {
			fw.write("\"站点名称\",\"站点编号\",\"所属分类\",\"及时数\",\"及时率\",\"逾时数\",\"逾时率\",\"缺报数\",\"缺报率\",\"总数\"\r\n");
			for (Statistics statistic : weatherStations) {
				fw.write(String.format("\"%s\",\"%s\",\"%s\",%d,\"%s\",%d,\"%s\",%d,\"%s\",%d\r\n", statistic.getWeatherStationName(), statistic.getWeatherStationNumber(),statistic.getWeatherStationCategoryName(),
						statistic.getOnTimeCount(), statistic.getOnTimePercent(), statistic.getDelayedTimeCount(),statistic.getDelayedTimePercent(),statistic.getLoseCount(),statistic.getLosePercent(),statistic.getTotalCount()));
			}
		}
		String mimeType = "application/octet-stream; charset=utf-8";
		response.setContentType(mimeType);
		String agent = request.getHeader("User-Agent");
		boolean isMSIE = (agent != null && agent.indexOf("MSIE") != -1);
		if (isMSIE) {
			response.setHeader("Content-Disposition", String.format("inline; filename=\"" + java.net.URLEncoder.encode(downloadFile.getName(), "utf-8") + "\""));
		} else {

			response.setHeader("Content-Disposition", String.format("inline; filename=\"" + new String(downloadFile.getName().getBytes("UTF-8"), "ISO-8859-1") + "\""));
		}
		response.setContentLength((int) downloadFile.length());
		try (InputStream inputStream = new BufferedInputStream(new FileInputStream(downloadFile))) {
			FileCopyUtils.copy(inputStream, response.getOutputStream());
			downloadFile.delete();
		}
	}
}
