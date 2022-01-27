package com.partners.weather.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;
import com.partners.annotation.UserAction;
import com.partners.annotation.UserPermission;
import com.partners.annotation.UserPermissionEnum;
import com.partners.entity.Adminuser;
import com.partners.entity.AggregationEntity;
import com.partners.entity.JsonResult;
import com.partners.entity.TerminalParametersAttrs;
import com.partners.entity.Terminalparameters;
import com.partners.entity.WeatherStationTerminal;
import com.partners.entity.Weatherstation;
import com.partners.entity.Weatherstationcategory;
import com.partners.view.entity.VAgg;
import com.partners.weather.common.CommonResources;
import com.partners.weather.redis.RedisPoolManager;
import com.partners.weather.request.RequestHelper;
import com.partners.weather.serialize.ObjectSerializeTransfer;
import com.partners.weather.service.IAggregationService;
import com.partners.weather.service.IWeatherStationService;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Controller
@RequestMapping("/agg")
@UserPermission(value = UserPermissionEnum.AGGS)
public class AggregationController {
	private static final Logger logger = LoggerFactory.getLogger(AggregationController.class);
	@Autowired
	IAggregationService aggregationService;
	@Autowired
	IWeatherStationService weatherStationService;
	@Autowired
	JedisPool jedisPool;

	@RequestMapping
	@UserPermission(value = UserPermissionEnum.AGGS)
	@UserAction(Action = UserPermissionEnum.AGGS, Description = "数据统计")
	public String manage(HttpServletRequest request) {
		Adminuser adminuser = (Adminuser) request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
		List<Weatherstationcategory> weatherstationcategories = weatherStationService.getAllWeatherStationCategory();
		weatherstationcategories = weatherStationService.filterWeatherStationCategorys(weatherstationcategories, adminuser.getWeatherStationGroup(), false);
		List<Weatherstation> weatherstations = weatherStationService.getAllWeatherStation();
		weatherstations = weatherStationService.filterWeatherStation(weatherstations, adminuser.getWeatherStation(), false);
		request.setAttribute("Categories", weatherstationcategories);
		request.setAttribute("Stations", weatherstations);
		DateTime now = DateTime.now();
		request.setAttribute("StartDate", now.plusHours(-1).toString("yyyy-MM-dd HH:mm:00"));
		request.setAttribute("EndDate", now.toString("yyyy-MM-dd HH:mm:00"));
		return "aggregation";
	}

	@RequestMapping("/parameters")
	@UserPermission(value = UserPermissionEnum.AGGS)
	@UserAction(Action = UserPermissionEnum.AGGS, Description = "数据统计")
	@ResponseBody
	public Map<String, String> parameters(HttpServletRequest request) throws Exception {
		Jedis client = null;
		Map<String, String> paramMap = new HashMap<>();
		String station = request.getParameter("station");
		if (StringUtils.isBlank(station)) {
			return paramMap;
		}
		try {
			ObjectSerializeTransfer<Terminalparameters> objectSerializeTransfer = new ObjectSerializeTransfer<>();
			RedisPoolManager.Init(jedisPool);
			client = RedisPoolManager.getJedis();
			String[] stationArray = station.trim().split(",");
			List<Integer> stations = new ArrayList<>(stationArray.length);
			if (stationArray.length > 0) {
				for (String p : stationArray) {
					stations.add(Integer.parseInt(p.trim()));
				}
				List<WeatherStationTerminal> terminals = weatherStationService.getTerminalsByStationId(stations);
				byte[] params;
				Terminalparameters terminalParams;
				for (WeatherStationTerminal terminal : terminals) {
					params = client.get(String.valueOf(terminal.getTeminalParameterCategoryId()).getBytes());
					if (params != null && params.length > 0) {
						terminalParams = (Terminalparameters) objectSerializeTransfer.deserialize(params);
						for (TerminalParametersAttrs tp : terminalParams.getTerminalParametersAttrs()) {
							if (!paramMap.containsKey(tp.getName()) && !"N".equalsIgnoreCase(tp.getShowInPage())) {
								paramMap.put(tp.getName(), tp.getDescription());
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error in {}", e);
		} finally {
			RedisPoolManager.close(client);
		}
		return paramMap;
	}

	@RequestMapping("/stations")
	@UserPermission(value = UserPermissionEnum.AGGS)
	@UserAction(Action = UserPermissionEnum.AGGS, Description = "数据统计")
	@ResponseBody
	public JsonResult stations(HttpServletRequest request) throws Exception {
		JsonResult jsonResult = new JsonResult();
		jsonResult.setDraw(0);
		jsonResult.setRecordsTotal(0);
		jsonResult.setRecordsFiltered(0);
		List<AggregationEntity> aggregationEntities = new ArrayList<>(0);
		jsonResult.setData(aggregationEntities.toArray());
		String station = request.getParameter("station");
		String parameter = request.getParameter("parameters");
		if (StringUtils.isBlank(station) || StringUtils.isBlank(parameter)) {
			return jsonResult;
		}
		VAgg vAgg = new VAgg(RequestHelper.prepareRequest(request, true));
		vAgg.setStartDate(request.getParameter("startdate"));
		vAgg.setEndDate(request.getParameter("enddate"));
		try {
			DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
			DateTime.parse(vAgg.getStartDate(), format);
			DateTime.parse(vAgg.getEndDate(), format);
		} catch (Exception e) {
			return jsonResult;
		}
		String[] stations = station.trim().split(",");
		if (stations.length > 0) {
			List<Integer> stationList = new ArrayList<>(stations.length);
			for (String p : stations) {
				stationList.add(Integer.parseInt(p.trim()));
			}
			vAgg.setWeatherStations(stationList);
		}
		String[] parameters = parameter.trim().split(",");
		if (parameters.length > 0) {
			vAgg.setParameters(Lists.newArrayList(parameters));
		} else {
			return jsonResult;
		}
		Jedis client = null;
		List<WeatherStationTerminal> terminals = null;
		Map<String, String> paramMap = new HashMap<>();
		try {
			ObjectSerializeTransfer<Terminalparameters> objectSerializeTransfer = new ObjectSerializeTransfer<>();
			RedisPoolManager.Init(jedisPool);
			client = RedisPoolManager.getJedis();
			if (vAgg.getWeatherStations().size() > 0) {
				terminals = weatherStationService.getTerminalsByStationId(vAgg.getWeatherStations());
				byte[] params;
				Terminalparameters terminalParams;
				for (WeatherStationTerminal terminal : terminals) {
					params = client.get(String.valueOf(terminal.getTeminalParameterCategoryId()).getBytes());
					if (params != null && params.length > 0) {
						terminalParams = (Terminalparameters) objectSerializeTransfer.deserialize(params);
						for (TerminalParametersAttrs tp : terminalParams.getTerminalParametersAttrs()) {
							if (!paramMap.containsKey(tp.getName()) && !"N".equalsIgnoreCase(tp.getShowInPage()) && ArrayUtils.contains(parameters, tp.getName())) {
								paramMap.put(tp.getName(), tp.getDescription());
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error in {}", e);
		} finally {
			RedisPoolManager.close(client);
		}
		aggregationEntities = aggregationService.getAggregationRecords(vAgg, terminals, paramMap);
		int draw = StringUtils.isBlank(request.getParameter("draw")) ? 1 : Integer.valueOf(request.getParameter("draw"));
		jsonResult.setDraw(draw);
		jsonResult.setRecordsTotal(vAgg.getTotalRecords());
		jsonResult.setRecordsFiltered(vAgg.getTotalRecords());
		jsonResult.setData(aggregationEntities.toArray());
		return jsonResult;
	}
	
	@RequestMapping("/searchReport")
	@UserPermission(value = UserPermissionEnum.AGGS)
	@UserAction(Action = UserPermissionEnum.AGGS, Description = "数据统计")
	@ResponseBody
	public JsonResult searchReport(HttpServletRequest request) throws Exception {
		JsonResult jsonResult = new JsonResult();
		jsonResult.setDraw(0);
		jsonResult.setRecordsTotal(0);
		jsonResult.setRecordsFiltered(0);
		List<AggregationEntity> aggregationEntities = new ArrayList<>(0);
		jsonResult.setData(aggregationEntities.toArray());
		String station = request.getParameter("station");
		String parameter = request.getParameter("parameters");
		if (StringUtils.isBlank(station) || StringUtils.isBlank(parameter)) {
			return jsonResult;
		}
		VAgg vAgg = new VAgg(RequestHelper.prepareRequest(request, true));
		vAgg.setStartDate(request.getParameter("startdate"));
		vAgg.setEndDate(request.getParameter("enddate"));
		try {
			DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
			DateTime.parse(vAgg.getStartDate(), format);
			DateTime.parse(vAgg.getEndDate(), format);
		} catch (Exception e) {
			return jsonResult;
		}
		String[] stations = station.trim().split(",");
		if (stations.length > 0) {
			List<Integer> stationList = new ArrayList<>(stations.length);
			for (String p : stations) {
				stationList.add(Integer.parseInt(p.trim()));
			}
			vAgg.setWeatherStations(stationList);
		}
		String[] parameters = parameter.trim().split(",");
		if (parameters.length > 0) {
			vAgg.setParameters(Lists.newArrayList(parameters));
		} else {
			return jsonResult;
		}
		Jedis client = null;
		List<WeatherStationTerminal> terminals = null;
		Map<String, String> paramMap = new HashMap<>();
		try {
			ObjectSerializeTransfer<Terminalparameters> objectSerializeTransfer = new ObjectSerializeTransfer<>();
			RedisPoolManager.Init(jedisPool);
			client = RedisPoolManager.getJedis();
			if (vAgg.getWeatherStations().size() > 0) {
				terminals = weatherStationService.getTerminalsByStationId(vAgg.getWeatherStations());
				byte[] params;
				Terminalparameters terminalParams;
				for (WeatherStationTerminal terminal : terminals) {
					params = client.get(String.valueOf(terminal.getTeminalParameterCategoryId()).getBytes());
					if (params != null && params.length > 0) {
						terminalParams = (Terminalparameters) objectSerializeTransfer.deserialize(params);
						for (TerminalParametersAttrs tp : terminalParams.getTerminalParametersAttrs()) {
							if (!paramMap.containsKey(tp.getName()) && !"N".equalsIgnoreCase(tp.getShowInPage()) && ArrayUtils.contains(parameters, tp.getName())) {
								paramMap.put(tp.getName(), tp.getDescription());
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error in {}", e);
		} finally {
			RedisPoolManager.close(client);
		}
		aggregationEntities = aggregationService.getAggregationRecords(vAgg, terminals, paramMap);
		vAgg.setParameters(Arrays.asList("rainfall"));
		//vAgg.setParameters(Arrays.asList("pressure"));
		double rainfall = aggregationService.getAggRainfall(vAgg, terminals.get(0));
		
		AggregationEntity aggRainfall = new AggregationEntity();
		aggRainfall.setTerminalName("rainfall");
		aggRainfall.setValue(rainfall);
		aggRainfall.setType("累积雨量");
		aggregationEntities.add(aggRainfall);
		
		int draw = StringUtils.isBlank(request.getParameter("draw")) ? 1 : Integer.valueOf(request.getParameter("draw"));
		jsonResult.setDraw(draw);
		jsonResult.setRecordsTotal(vAgg.getTotalRecords());
		jsonResult.setRecordsFiltered(vAgg.getTotalRecords());
		jsonResult.setData(aggregationEntities.toArray());
		return jsonResult;
	}

	@RequestMapping("/download")
	public void download(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String station = request.getParameter("station");
		String parameter = request.getParameter("parameters");
		if (StringUtils.isBlank(station) || StringUtils.isBlank(parameter)) {
			return;
		}
		VAgg vAgg = new VAgg(RequestHelper.prepareRequest(request, true));
		vAgg.setStartDate(request.getParameter("startdate"));
		vAgg.setEndDate(request.getParameter("enddate"));
		try {
			DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
			DateTime.parse(vAgg.getStartDate(), format);
			DateTime.parse(vAgg.getEndDate(), format);
		} catch (Exception e) {
			return;
		}
		String[] stations = station.trim().split(",");
		if (stations.length > 0) {
			List<Integer> stationList = new ArrayList<>(stations.length);
			for (String p : stations) {
				stationList.add(Integer.parseInt(p.trim()));
			}
			vAgg.setWeatherStations(stationList);
		}
		String[] parameters = parameter.trim().split(",");
		if (parameters.length > 0) {
			vAgg.setParameters(Lists.newArrayList(parameters));
		} else {
			return;
		}
		Jedis client = null;
		List<WeatherStationTerminal> terminals = null;
		Map<String, String> paramMap = new HashMap<>();
		try {
			ObjectSerializeTransfer<Terminalparameters> objectSerializeTransfer = new ObjectSerializeTransfer<>();
			RedisPoolManager.Init(jedisPool);
			client = RedisPoolManager.getJedis();
			if (vAgg.getWeatherStations().size() > 0) {
				terminals = weatherStationService.getTerminalsByStationId(vAgg.getWeatherStations());
				byte[] params;
				Terminalparameters terminalParams;
				for (WeatherStationTerminal terminal : terminals) {
					params = client.get(String.valueOf(terminal.getTeminalParameterCategoryId()).getBytes());
					if (params != null && params.length > 0) {
						terminalParams = (Terminalparameters) objectSerializeTransfer.deserialize(params);
						for (TerminalParametersAttrs tp : terminalParams.getTerminalParametersAttrs()) {
							if (!paramMap.containsKey(tp.getName()) && !"N".equalsIgnoreCase(tp.getShowInPage())) {
								paramMap.put(tp.getName(), tp.getDescription());
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error in {}", e);
		} finally {
			RedisPoolManager.close(client);
		}
		vAgg.setSize(10000);
		NumberFormat nf = NumberFormat.getNumberInstance();
	    nf.setMaximumFractionDigits(2);
		List<AggregationEntity> aggregationEntities = aggregationService.getAggregationRecords(vAgg, terminals, paramMap);
		File downloadFile = new File(request.getSession().getServletContext().getRealPath("/resources/download/数据统计(" + System.currentTimeMillis() + ").csv"));
		try (FileWriter fw = new FileWriter(downloadFile)) {
			fw.write("\"站点名称\",\"站点编号\",\"要素名称\",\"统计类型\",\"最高/低值\",\"日期\"\r\n");
			for (AggregationEntity aggregationEntity : aggregationEntities) {
				fw.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",%s,\"%s\"\r\n", aggregationEntity.getWeatherStationName(), aggregationEntity.getWeatherStationNumber(), aggregationEntity.getTerminalDesc(),
						aggregationEntity.getType(), nf.format(aggregationEntity.getValue()), aggregationEntity.getLastDate()));
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