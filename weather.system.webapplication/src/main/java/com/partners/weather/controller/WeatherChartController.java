package com.partners.weather.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.partners.annotation.UserAction;
import com.partners.annotation.UserPermission;
import com.partners.annotation.UserPermissionEnum;
import com.partners.entity.Adminuser;
import com.partners.entity.TerminalParametersAttrs;
import com.partners.entity.Terminalparameters;
import com.partners.entity.WeatherStationTerminal;
import com.partners.entity.Weatherstation;
import com.partners.entity.Weatherstationcategory;
import com.partners.view.entity.VStationSearch;
import com.partners.weather.common.CommonResources;
import com.partners.weather.encrypt.HexUtil;
import com.partners.weather.redis.RedisPoolManager;
import com.partners.weather.request.RequestHelper;
import com.partners.weather.serialize.ObjectSerializeTransfer;
import com.partners.weather.service.IWeatherSearchService;
import com.partners.weather.service.IWeatherStationService;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Controller
@RequestMapping("/weatherchart")
@UserPermission(value = UserPermissionEnum.ANALYSIS)
public class WeatherChartController {
	private static final Logger logger = LoggerFactory.getLogger(WeatherChartController.class);
	@Autowired
	IWeatherSearchService weatherSearchService;
	@Autowired
	IWeatherStationService weatherStationService;
	@Autowired
	JedisPool jedisPool;

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
		return "weatherchart";
	}
	@RequestMapping("/parameters")
	@UserPermission(value = UserPermissionEnum.SELECT)
	@UserAction(Action = UserPermissionEnum.SELECT, Description = "数据查询")
	@ResponseBody
	public List<TerminalParametersAttrs> columns(HttpServletRequest request) throws Exception {
		List<TerminalParametersAttrs> parametersAttrs=new ArrayList<>();
		String station = request.getParameter("station");
		if (StringUtils.isBlank(station)) {
			return parametersAttrs;
		}
		String[] stationArray = station.trim().split(",");
		List<Integer> stations = new ArrayList<>(stationArray.length);
		if (stationArray.length > 0) {			
			for (String p : stationArray) {
				stations.add(Integer.parseInt(p.trim()));
			}
		}
		Jedis client = null;
		try{
			ObjectSerializeTransfer<Terminalparameters> objectSerializeTransfer = new ObjectSerializeTransfer<>();
			RedisPoolManager.Init(jedisPool);
			client = RedisPoolManager.getJedis();
			List<WeatherStationTerminal> terminals = weatherStationService.getTerminalsByStationId(stations);
			byte[] params;
			Terminalparameters terminalParams;
			List<Integer> terminalSelectedParams;
			Set<String> paramSet=new HashSet<>();
			for (WeatherStationTerminal terminal : terminals) {
				terminalSelectedParams = Lists.transform( Arrays.asList(terminal.getTerminalParameters().split(",")), new Function<String, Integer>() {
					public Integer apply(String e) {
						return HexUtil.HexToInt(e);
					}
				});
				params = client.get(String.valueOf(terminal.getTeminalParameterCategoryId()).getBytes());
				if (params != null && params.length > 0) {
					terminalParams = (Terminalparameters) objectSerializeTransfer.deserialize(params);
					for (TerminalParametersAttrs tp : terminalParams.getTerminalParametersAttrs()) {
						if (terminalSelectedParams.contains(HexUtil.HexToInt(tp.getId())) && !paramSet.contains(tp.getName()) && !"N".equalsIgnoreCase(tp.getShowInPage())) {
							paramSet.add(tp.getName());
							tp.setCustomeFiled(false);
							tp.setId(tp.getId());
							tp.setOrderDesc("");
							tp.setParameterAttributes(null);
							tp.setShowInPage("");
							parametersAttrs.add(tp);
						}
					}
				}
			}
			paramSet.clear();
			paramSet=null;
		} catch (Exception e) {
			logger.error("Error in {}", e);
		} finally {
			RedisPoolManager.close(client);
		}
		return parametersAttrs;
	}
	
	@RequestMapping("/stations")
	@UserPermission(value = UserPermissionEnum.SELECT)
	@UserAction(Action = UserPermissionEnum.SELECT, Description = "数据查询")
	@ResponseBody
	public String stations(HttpServletRequest request) throws Exception {
		Jedis client = null;
		String station = request.getParameter("station");
		String terminalParamName = request.getParameter("parameters");
		String result = "";
		int lineChartType = Integer.parseInt(request.getParameter("linecharttype"));
		TerminalParametersAttrs terminalParameters = null;
		if (StringUtils.isBlank(station)) {
			return "";
		}
		VStationSearch stationSearch = new VStationSearch(RequestHelper.prepareRequest(request, true));
		try {
			DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
			DateTime startDate = DateTime.parse(request.getParameter("startdate"), format);
			
			if(lineChartType == 1){
				stationSearch.setStartDate(startDate.toString("yyyy-MM-dd 00:00:00"));
				stationSearch.setEndDate(startDate.plusDays(1).withTimeAtStartOfDay().toString("yyyy-MM-dd HH:mm:ss"));
			} else {
				stationSearch.setStartDate(request.getParameter("startdate"));
				stationSearch.setEndDate(startDate.plusHours(3).toString("yyyy-MM-dd HH:mm:ss"));
			}
			
//			DateTime.parse(stationSearch.getStartDate(), format);
//			DateTime.parse(stationSearch.getEndDate(), format);			
		} catch (Exception e) {
			return "";
		}
		String[] stationArray = station.trim().split(",");
		List<Integer> stations = new ArrayList<>(stationArray.length);
		if (stationArray.length > 0) {			
			for (String p : stationArray) {
				stations.add(Integer.parseInt(p.trim()));
			}
			stationSearch.setWeatherStations(stations);
		}
		try{
			ObjectSerializeTransfer<Terminalparameters> objectSerializeTransfer = new ObjectSerializeTransfer<>();
			RedisPoolManager.Init(jedisPool);
			client = RedisPoolManager.getJedis();
			List<WeatherStationTerminal> terminals = weatherStationService.getTerminalsByStationId(stations);
			byte[] params;
			Terminalparameters terminalParams;
			for (WeatherStationTerminal terminal : terminals) {
				params = client.get(String.valueOf(terminal.getTeminalParameterCategoryId()).getBytes());
				if (params != null && params.length > 0) {
					terminalParams = (Terminalparameters) objectSerializeTransfer.deserialize(params);
					for (TerminalParametersAttrs tp : terminalParams.getTerminalParametersAttrs()) {
						if (terminalParamName.equalsIgnoreCase(tp.getId())){
							terminalParameters = tp;
							break;
						}
					}
					if(terminalParameters != null){
						break;
					}
				}
			}
			stationSearch.setSize(10000);
			
			result = weatherSearchService.getWeathersByLineChart(stationSearch, terminalParameters, lineChartType);
		} catch (Exception e) {
			logger.error("Error in {}", e);
		} finally {
			RedisPoolManager.close(client);
		}
		
		return result;
	}
}
