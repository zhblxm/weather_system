package com.partners.weather.service.impl;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.partners.entity.ParameterAttribute;
import com.partners.entity.TerminalParametersAttrs;
import com.partners.entity.WeatherStationTerminal;
import com.partners.entity.Weatherstation;
import com.partners.view.entity.VStationSearch;
import com.partners.weather.common.CommonResources;
import com.partners.weather.dao.IWeatherStationDAO;
import com.partners.weather.search.SearchIndexUtil;
import com.partners.weather.service.IWeatherSearchService;

@Service
public class WeatherSearchServiceImp implements IWeatherSearchService {
	private static final Logger logger = LoggerFactory.getLogger(WeatherSearchServiceImp.class);
	@Autowired
	IWeatherStationDAO weatherStationDAO;

	@Override
	public List<String> getWeathers(VStationSearch stationSearch, Map<String, TerminalParametersAttrs> parameterMap,
			List<WeatherStationTerminal> terminals) {
		List<String> searchResponses = new ArrayList<>();
		try {
			StringBuilder headerBuilder = new StringBuilder();
			Iterator<String> iterator = parameterMap.keySet().iterator();
			Map<String, Integer> parameterIndexMap = new HashMap<>(parameterMap.size());
			Map<String, ParameterAttribute> parameterDataMap = new HashMap<>(parameterMap.size());
			Map<String, Object> hitSeachMap = new HashMap<>();
			int index = 0, stationId;
			String key, searchKey, templete, searchData = "", datatype;
			Object value;
			headerBuilder.append("{");
			while (iterator.hasNext()) {
				key = iterator.next();
				headerBuilder.append("\"" + key + "value\":\"{" + index + "}\",");
				parameterIndexMap.put(key, index);
				index++;
				parameterDataMap = Maps.uniqueIndex(parameterMap.get(key).getParameterAttributes(),
						new Function<ParameterAttribute, String>() {
							@Override
							public String apply(ParameterAttribute input) {
								return input.getName();
							}
						});
			}
			headerBuilder.append("\"weatherstationnumber\":\"[NUMBER]\",");
			headerBuilder.append("\"weatherstationname\":\"[NAME]\",");
			headerBuilder.append("\"datetime\":\"[DATETIME]\"");
			headerBuilder.append("}");
			templete = headerBuilder.toString();
			@SuppressWarnings("deprecation")
			SearchResponse response = SearchIndexUtil.getClient().prepareSearch(CommonResources.WEATHERSYSTEMALIAS)
					.setFrom(
							stationSearch
									.getStartIndex())
					.setSize(stationSearch.getSize())
					.setQuery(QueryBuilders.boolQuery()
							.filter(QueryBuilders.andQuery(
									QueryBuilders.rangeQuery("systemDate").format("yyyy-MM-dd HH:mm:ss")
											.gt(stationSearch.getStartDate()).lt(stationSearch.getEndDate()),
									QueryBuilders.termsQuery("stationId", stationSearch.getWeatherStations()))))
					.execute().get();
			SearchHits hits = response.getHits();
			stationSearch.setTotalRecords((int) hits.getTotalHits());
			List<Weatherstation> weatherstations = weatherStationDAO
					.getMutliWeatherStation(stationSearch.getWeatherStations());
			Map<Integer, Weatherstation> stationMap = Maps.uniqueIndex(weatherstations,
					new Function<Weatherstation, Integer>() {
						@Override
						public Integer apply(Weatherstation input) {
							return input.getWeatherStationId();
						}
					});
			for (SearchHit searchHit : hits) {
				hitSeachMap = searchHit.getSource();
				iterator = hitSeachMap.keySet().iterator();
				searchData = templete;
				stationId = (int) hitSeachMap.get("stationId");
				while (iterator.hasNext()) {
					key = iterator.next();
					index = key.lastIndexOf('_');
					if (-1 == index) {
						continue;
					}
					searchKey = key.substring(0, index);
					if (parameterIndexMap.containsKey(searchKey)) {
						datatype = parameterDataMap.get("datatype").getValue();
						if ("double".equals(datatype)) {
							value = (double) hitSeachMap.get(key);
						} else if ("integer".equals(datatype)) {
							value = (int) hitSeachMap.get(key);
						} else {
							value = (String) hitSeachMap.get(key);
						}
						searchData = searchData.replaceAll("\\{" + parameterIndexMap.get(searchKey) + "\\}",
								value.toString());
					}
				}
				searchData = searchData.replaceAll("\\[NUMBER\\]", stationMap.get(stationId).getWeatherStationNumber());
				searchData = searchData.replaceAll("\\[NAME\\]", stationMap.get(stationId).getWeatherStationName());
				searchData = searchData.replaceAll("\\[DATETIME\\]", (String) hitSeachMap.get("systemDate"));
				// headerBuilder.append("\"terminalname\":\"[TERMINALNAME]\",");
				searchData = searchData.replaceAll("\\{\\d+\\}", "N/A");
				searchResponses.add(searchData);
			}
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Error in {}", e);
		}
		return searchResponses;
	}

	@Override
	public String getWeathersByLineChart(VStationSearch stationSearch, TerminalParametersAttrs terminalParameters, int lineChartType) {
		Map<String, Object> hitSeachMap = new HashMap<>();
		Iterator<String> iterator;
		String param = terminalParameters.getName() + "_";
		int stationId;
		double rainfall = 0.0, avgRainfall;
		String key, datatype, lineChartDataKey, result = "", rainfallText;
		DateTime fullCollectDate, startDate, endDate;
		double value;
		DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		Map<String, ParameterAttribute> parameterDataMap = new HashMap<>();
		parameterDataMap = Maps.uniqueIndex(terminalParameters.getParameterAttributes(),
				new Function<ParameterAttribute, String>() {
					@Override
					public String apply(ParameterAttribute input) {
						return input.getName();
					}
				});
		try {
			@SuppressWarnings("deprecation")
			SearchResponse response = SearchIndexUtil.getClient().prepareSearch(CommonResources.WEATHERSYSTEMALIAS)
					.setFrom(
							stationSearch
									.getStartIndex())
					.setSize(stationSearch.getSize())
					.addSort("stationId", SortOrder.ASC)
					.addSort("fullCollectDate", SortOrder.ASC)
					.setQuery(QueryBuilders.boolQuery()
							.filter(QueryBuilders.andQuery(
									QueryBuilders.rangeQuery("fullCollectDate").format("yyyy-MM-dd HH:mm:ss")
											.gt(stationSearch.getStartDate()).lt(stationSearch.getEndDate()),
									QueryBuilders.termsQuery("stationId", stationSearch.getWeatherStations()))))
					.execute().get();
					
			SearchHits hits = response.getHits();
			startDate = DateTime.parse(stationSearch.getStartDate(), format);
			endDate = DateTime.parse(stationSearch.getEndDate(), format);
			Map<Integer, Map<String, Double>> lineChartData = getLineChartData(lineChartType, startDate, stationSearch.getWeatherStations()); 
			
			for (SearchHit searchHit : hits) {
				hitSeachMap = searchHit.getSource();
				iterator = hitSeachMap.keySet().iterator();
				stationId = (int) hitSeachMap.get("stationId");
				fullCollectDate = DateTime.parse(hitSeachMap.get("fullCollectDate").toString(), format);
				
				Map<String, Double> lineTimeData = lineChartData.get(stationId);
				
				if(lineChartType == 1){
					lineChartDataKey = fullCollectDate.toString("ddHH00");
				}
				else{
					lineChartDataKey = fullCollectDate.toString("ddHHmm").substring(0, 5) + "0";
				}
				
				if(!lineTimeData.containsKey(lineChartDataKey) || lineTimeData.get(lineChartDataKey) > 0.00001)
					continue;
				value = 0.0;
				while (iterator.hasNext()) {
					key = iterator.next();
					if (key.contains(param)) {
						datatype = parameterDataMap.get("datatype").getValue();
						if ("double".equals(datatype) || "integer".equals(datatype)) {
							value = (double) hitSeachMap.get(key);
							rainfall += value;
						} else {
							value = 0.0;
						}
						break;
					}
				}
				
				lineTimeData.put(lineChartDataKey, value);
				lineChartData.put(stationId, lineTimeData);
			}
			List<Weatherstation> weatherstations = weatherStationDAO
					.getMutliWeatherStation(stationSearch.getWeatherStations());
			Map<Integer, Weatherstation> stationMap = Maps.uniqueIndex(weatherstations,
					new Function<Weatherstation, Integer>() {
						@Override
						public Integer apply(Weatherstation input) {
							return input.getWeatherStationId();
						}
					});
			
			@SuppressWarnings({ "rawtypes", "unchecked" })
			List<Double> timeData = new ArrayList();
			for (Integer chartDataKey : lineChartData.keySet()) {  
				result += "{\"name\":\"" + stationMap.get(chartDataKey).getWeatherStationName() + "\",";
				timeData.clear();
				Map<String, Double> timeDataMap = lineChartData.get(chartDataKey);
			    for(String timeKey : timeDataMap.keySet()){
			    	timeData.add(timeDataMap.get(timeKey));
			    }
			    result += "\"data\":[" + StringUtils.join(timeData,',') + "]},";
			} 
			
			result = "[" + result.substring(0, result.length() - 1) + "]";
			
			rainfallText = ", \"rainfalltext\":";
			if(terminalParameters.getDescription().equals("雨量")){
				Period p = new Period(startDate, endDate, PeriodType.hours());
				avgRainfall = rainfall / p.getHours();
				rainfallText += "\"总雨量：" + String.format("%.2f", rainfall) + ",   平均雨量：" + String.format("%.2f", avgRainfall) + "\"";
			} else {
				rainfallText += "\"\"";
			}
			
			result = "{\"categories\":" + getLineCategories(lineChartType, startDate) + ", \"series\":" + result + rainfallText + "}" ;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	private Map<Integer, Map<String, Double>> getLineChartData(int dataType, DateTime lineChartDate, List<Integer> weatherStations){
		Map<Integer, Map<String, Double>> lineChartData = new LinkedHashMap<>();
		for(int stationId : weatherStations) {
			Map<String, Double> lineDateMap = getLineDate(dataType, lineChartDate);
			lineChartData.put(stationId, lineDateMap);
		}
		
		return lineChartData;
	}
	
	private Map<String, Double> getLineDate(int dataType, DateTime lineChartDate){
		Map<String, Double> lineChart = new LinkedHashMap<>();
		DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		if(dataType == 1){
			DateTime beginDate = DateTime.parse(lineChartDate.toString("yyyy-MM-dd 00:00:00"), format);
			DateTime endDate = beginDate.plusDays(1);
			for(DateTime i = beginDate; i.isBefore(endDate); i=i.plusHours(1)){
				lineChart.put(i.toString("ddHHmm"), 0.0);
			}
		} else {
			DateTime beginDate = DateTime.parse(lineChartDate.toString("yyyy-MM-dd HH:00:00"), format);
			DateTime endDate = beginDate.plusHours(3);
			for(DateTime i = beginDate; i.isBefore(endDate); i=i.plusMinutes(10)){
				lineChart.put(i.toString("ddHHmm"), 0.0);
			}
		}
						
		return lineChart;
	}
	
	private String getLineCategories(int dataType, DateTime lineChartDate){
		DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		List<String> categories = new ArrayList();
		if(dataType == 1){
			DateTime beginDate = DateTime.parse(lineChartDate.toString("yyyy-MM-dd 00:00:00"), format);
			DateTime endDate = beginDate.plusDays(1);
			for(DateTime i = beginDate; i.isBefore(endDate); i=i.plusHours(1)){
				categories.add(i.toString("\"HH\""));
			}
		} else {
			DateTime beginDate = DateTime.parse(lineChartDate.toString("yyyy-MM-dd HH:00:00"), format);
			DateTime endDate = beginDate.plusHours(3);
			for(DateTime i = beginDate; i.isBefore(endDate); i=i.plusMinutes(10)){
				categories.add(i.toString("\"dd HH:mm\""));
			}
		}
						
		return "[" + StringUtils.join(categories,',') + "]";
	}
}
