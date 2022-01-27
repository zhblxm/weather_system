package com.partners.weather.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import com.alibaba.fastjson.JSON;
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
@RequestMapping("/weathersearch")
@UserPermission(value = UserPermissionEnum.EXCEPTIONDATA)
public class WeatherSearchController {
	private static final Logger logger = LoggerFactory.getLogger(WeatherSearchController.class);
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
		request.setAttribute("StartDateReport", new DateTime().toString("yyyy-MM-dd HH:00:00"));
		request.setAttribute("EndDateReport", new DateTime().plusMonths(1).toString("yyyy-MM-dd HH:00:00"));
		return "weathersearch";
	}
	@RequestMapping("/cols")
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
							tp.setId("");
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
	/**
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/stations")
	@UserPermission(value = UserPermissionEnum.SELECT)
	@UserAction(Action = UserPermissionEnum.SELECT, Description = "数据查询")
	@ResponseBody
	public String stations(HttpServletRequest request) throws Exception {
		StringBuilder weatherJsonResponse=new StringBuilder();
		int draw = StringUtils.isBlank(request.getParameter("draw")) ? 1 : Integer.valueOf(request.getParameter("draw"));
		weatherJsonResponse.append("{");
		weatherJsonResponse.append(String.format("\"draw\":%d,",draw));
		weatherJsonResponse.append("\"recordsTotal\":%d,");
		weatherJsonResponse.append("\"recordsFiltered\":%d,");
		weatherJsonResponse.append("\"data\":[%s]");
		weatherJsonResponse.append("}");
		Jedis client = null;	
		Map<String, TerminalParametersAttrs> paramMap = new HashMap<>();
		String station = request.getParameter("station");
		if (StringUtils.isBlank(station)) {
			return String.format(weatherJsonResponse.toString(), 0,0,"");
		}
		VStationSearch stationSearch = new VStationSearch(RequestHelper.prepareRequest(request, true));
		try {
			stationSearch.setStartDate(request.getParameter("startdate"));
			stationSearch.setEndDate(request.getParameter("enddate"));
			DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
			DateTime.parse(stationSearch.getStartDate(), format);
			DateTime.parse(stationSearch.getEndDate(), format);			
		} catch (Exception e) {
			return String.format(weatherJsonResponse.toString(), 0,0,"");
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
			List<Integer> terminalSelectedParams;
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
						if (terminalSelectedParams.contains(HexUtil.HexToInt(tp.getId())) && !paramMap.containsKey(tp.getName()) && !"N".equalsIgnoreCase(tp.getShowInPage())) {
							paramMap.put(tp.getName(), tp);
						}
					}
				}
			}
			List<String> searchWeathers=weatherSearchService.getWeathers(stationSearch, paramMap,terminals);			
			return String.format(weatherJsonResponse.toString(), stationSearch.getTotalRecords(),stationSearch.getTotalRecords(),StringUtils.join(searchWeathers,','));
		} catch (Exception e) {
			logger.error("Error in {}", e);
		} finally {
			RedisPoolManager.close(client);
		}
		return "";
	}
	@RequestMapping("/download")
	public void download(HttpServletRequest request,HttpServletResponse response) throws Exception {
		Jedis client = null;	
		Map<String, TerminalParametersAttrs> paramMap = new HashMap<>();
		Map<Integer, TerminalParametersAttrs> paramOrderMap = new TreeMap<>(new Comparator<Integer>(){  
            public int compare(Integer o1, Integer o2) {  
                return o1==o2?0:(o1>o2?1:-1);  
            }     
        });
		String station = request.getParameter("station");
		if (StringUtils.isBlank(station)) {
			return;
		}
		VStationSearch stationSearch = new VStationSearch(RequestHelper.prepareRequest(request, true));
		try {
			stationSearch.setStartDate(request.getParameter("startdate"));
			stationSearch.setEndDate(request.getParameter("enddate"));
			DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
			DateTime.parse(stationSearch.getStartDate(), format);
			DateTime.parse(stationSearch.getEndDate(), format);			
		} catch (Exception e) {
			return;
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
			List<Integer> terminalSelectedParams;
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
						if (terminalSelectedParams.contains(HexUtil.HexToInt(tp.getId())) && !paramMap.containsKey(tp.getName()) && !"N".equalsIgnoreCase(tp.getShowInPage())) {
							paramMap.put(tp.getName(), tp);
							paramOrderMap.put(tp.getOrder(),tp);
						}
					}
				}
			}
			stationSearch.setSize(10000);
			List<String> searchWeathers=weatherSearchService.getWeathers(stationSearch, paramMap,terminals);
			File downloadFile = new File(request.getSession().getServletContext().getRealPath("/resources/download/气象数据(" + System.currentTimeMillis() + ").csv"));
			try (FileWriter fw = new FileWriter(downloadFile)) {
				StringBuilder jsonBuilder=new StringBuilder();
				jsonBuilder.append("\"站点名称\",\"站点编号\",\"日期时间\"");
				String headerTemplete="\"[weatherstationname]\",\"[weatherstationnumber]\",\"[datetime]\"",tempWeather,jsonKey;		
				Iterator<Integer> paramOrderItr=paramOrderMap.keySet().iterator();
				int orderKey;
				while (paramOrderItr.hasNext()) {
					orderKey=paramOrderItr.next();
					jsonBuilder.append(",\""+paramOrderMap.get(orderKey).getDescription()+"\"");
					headerTemplete+=",["+paramOrderMap.get(orderKey).getName()+"value]";
				}
				jsonBuilder.append("\r\n");
				headerTemplete+="\r\n";
				JSONObject jsonObj;  
				Iterator<String> searchJsonItr;
				for (String weatherJson : searchWeathers) {
					jsonObj = JSON.parseObject(weatherJson);  
					searchJsonItr=jsonObj.keySet().iterator();
					tempWeather=headerTemplete;
					while (searchJsonItr.hasNext()) {
						jsonKey=searchJsonItr.next();
						tempWeather=tempWeather.replaceAll("\\["+jsonKey+"\\]", jsonObj.getString(jsonKey));
					}
					jsonBuilder.append(tempWeather);
				}
				fw.write(jsonBuilder.toString());
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
		} catch (Exception e) {
			logger.error("Error in {}", e);
		} finally {
			RedisPoolManager.close(client);
		}
	}
}
