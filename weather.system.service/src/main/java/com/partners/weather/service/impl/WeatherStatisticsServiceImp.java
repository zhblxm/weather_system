package com.partners.weather.service.impl;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.partners.entity.Autofrequencyhistory;
import com.partners.entity.Statistics;
import com.partners.entity.Weatherstation;
import com.partners.view.entity.VStatistics;
import com.partners.weather.common.CommonResources;
import com.partners.weather.dao.IWeatherStationDAO;
import com.partners.weather.search.SearchIndexUtil;
import com.partners.weather.service.IAutoFrequencyHistoryService;
import com.partners.weather.service.IWeatherStatisticsService;

@Service
public class WeatherStatisticsServiceImp implements IWeatherStatisticsService {

	private static final Logger logger = LoggerFactory.getLogger(WeatherStatisticsServiceImp.class);

	@Autowired
	IAutoFrequencyHistoryService autoFrequencyHistoryService;
	@Autowired
	IWeatherStationDAO weatherStationDAO;

	@SuppressWarnings("deprecation")
	@Override
	public List<Statistics> getStatistics(VStatistics statistics) {
		List<Weatherstation> weatherstations = null;
		List<Statistics> statisticList = null;
		try {
			List<Autofrequencyhistory> autofrequencyhistories = autoFrequencyHistoryService.getAutoFrequencyHistory(statistics);
			weatherstations = weatherStationDAO.getMutliWeatherStation(statistics.getWeatherStations());
			List<Autofrequencyhistory> autofrequencyTempList;
			Map<Integer, List<Autofrequencyhistory>> frequencyMap = new HashMap<>();
			for (Autofrequencyhistory autofrequencyhistory : autofrequencyhistories) {
				if (null == frequencyMap.get(autofrequencyhistory.getWeatherStationId())) {
					frequencyMap.put(autofrequencyhistory.getWeatherStationId(), Lists.newArrayList(autofrequencyhistory));
				} else {
					autofrequencyTempList = frequencyMap.get(autofrequencyhistory.getWeatherStationId());
					autofrequencyTempList.add(autofrequencyhistory);
					frequencyMap.put(autofrequencyhistory.getWeatherStationId(), autofrequencyTempList);
				}
			}
			Map<Integer, Weatherstation> stationCounterMap = new HashMap<>();
			Weatherstation station;
			Iterator<Integer> frequencyiteator = frequencyMap.keySet().iterator();
			List<Autofrequencyhistory> frequencies;
			DateTime end, start;
			int size, count, weatherStaionId;
			Period period;
			while (frequencyiteator.hasNext()) {
				weatherStaionId = frequencyiteator.next();
				frequencies = frequencyMap.get(weatherStaionId);
				size = frequencies.size();
				count = 0;
				for (int i = 0; i < size; i++) {
					if (frequencies.get(i).getAutoFrequency() == 0)
						continue;
					start = new DateTime(frequencies.get(i).getCreateDate());
					if (size > i + 1) {
						end = new DateTime(frequencies.get(i + 1).getCreateDate());
					} else {
						end = new DateTime(System.currentTimeMillis());
					}
					switch (frequencies.get(i).getAutoFrequencyUnit()) {
					case "d":
						period = new Period(start, end, PeriodType.days());
						count += period.getDays() / frequencies.get(i).getAutoFrequency();
						break;
					case "h":
						period = new Period(start, end, PeriodType.hours());
						count += period.getHours() / frequencies.get(i).getAutoFrequency();
						break;
					case "m":
						period = new Period(start, end, PeriodType.minutes());
						count += period.getMinutes() / frequencies.get(i).getAutoFrequency();
						break;
					}
					if (size == i + 1) {
						break;
					}
				}
				station = new Weatherstation();
				station.setWeatherStationId(weatherStaionId);
				station.setTotalCount(count);
				stationCounterMap.put(weatherStaionId, station);
			}
			// 查询statistics中站点集合中接收到的数据条数，以站点ID分组
			SearchResponse response = SearchIndexUtil.getClient().prepareSearch(CommonResources.WEATHERSYSTEMALIAS).setFrom(0).setSize(0)
					.setQuery(QueryBuilders.boolQuery().filter(QueryBuilders.andQuery(QueryBuilders.rangeQuery("systemDate").format("yyyy-MM-dd HH:mm:ss").gt(statistics.getStartDate()).lt(statistics.getEndDate()),
							QueryBuilders.termsQuery("stationId", statistics.getWeatherStations()))))
					.addAggregation(AggregationBuilders.terms("stationId").field("stationId")).setExplain(false).get();
			Aggregations aggregations = response.getAggregations();
			LongTerms histogram = null;
			List<Terms.Bucket> buckets = null;
			if (aggregations != null) {
				histogram = (LongTerms) aggregations.asMap().get("stationId");
				if (histogram != null) {
					buckets = histogram.getBuckets();
					if (buckets != null && buckets.size() > 0) {
						for (Terms.Bucket bucket : buckets) {
							weatherStaionId = Integer.parseInt(bucket.getKeyAsString());
							if (stationCounterMap.containsKey(weatherStaionId)) {
								stationCounterMap.get(weatherStaionId).setReceiveCount(bucket.getDocCount());
							} else {
								station = new Weatherstation();
								station.setWeatherStationId(weatherStaionId);
								station.setReceiveCount(bucket.getDocCount());
								stationCounterMap.put(weatherStaionId, station);
							}
						}
					}
				}
			}
			// 查询statistics中站点集合中接收到的【准时】数据条数，以站点ID分组
			response = SearchIndexUtil.getClient().prepareSearch(CommonResources.WEATHERSYSTEMALIAS).setFrom(0).setSize(0)
					.setQuery(QueryBuilders.boolQuery()
							.filter(QueryBuilders.andQuery(QueryBuilders.rangeQuery("systemDate").format("yyyy-MM-dd HH:mm:ss").gt(statistics.getStartDate()).lt(statistics.getEndDate()),
									QueryBuilders.termsQuery("stationId", statistics.getWeatherStations()), QueryBuilders.termQuery("delayed", 0))))
					.addAggregation(AggregationBuilders.terms("stationId").field("stationId")).setExplain(false).get();
			aggregations = response.getAggregations();
			if (aggregations != null) {
				histogram = (LongTerms) aggregations.asMap().get("stationId");
				if (histogram != null) {
					buckets = histogram.getBuckets();
					if (buckets != null && buckets.size() > 0) {
						for (Terms.Bucket bucket : buckets) {
							weatherStaionId = Integer.parseInt(bucket.getKeyAsString());
							if (stationCounterMap.containsKey(weatherStaionId)) {
								stationCounterMap.get(weatherStaionId).setOnTimeCount(bucket.getDocCount());
							} else {
								station = new Weatherstation();
								station.setWeatherStationId(weatherStaionId);
								station.setOnTimeCount(bucket.getDocCount());
								stationCounterMap.put(weatherStaionId, station);
							}
						}
					}
				}
			}
			// 查询statistics中站点集合中接收到的【逾时】数据条数，以站点ID分组
			response = SearchIndexUtil.getClient().prepareSearch(CommonResources.WEATHERSYSTEMALIAS).setFrom(0).setSize(0)
					.setQuery(QueryBuilders.boolQuery()
							.filter(QueryBuilders.andQuery(QueryBuilders.rangeQuery("systemDate").format("yyyy-MM-dd HH:mm:ss").gt(statistics.getStartDate()).lt(statistics.getEndDate()),
									QueryBuilders.termsQuery("stationId", statistics.getWeatherStations()), QueryBuilders.termQuery("delayed", 1))))
					.addAggregation(AggregationBuilders.terms("stationId").field("stationId")).setExplain(false).get();
			aggregations = response.getAggregations();
			if (aggregations != null) {
				histogram = (LongTerms) aggregations.asMap().get("stationId");
				if (histogram != null) {
					buckets = histogram.getBuckets();
					if (buckets != null && buckets.size() > 0) {
						for (Terms.Bucket bucket : buckets) {
							weatherStaionId = Integer.parseInt(bucket.getKeyAsString());
							if (stationCounterMap.containsKey(weatherStaionId)) {
								stationCounterMap.get(weatherStaionId).setDelayedTimeCount(bucket.getDocCount());
							} else {
								station = new Weatherstation();
								station.setWeatherStationId(weatherStaionId);
								station.setDelayedTimeCount(bucket.getDocCount());
								stationCounterMap.put(weatherStaionId, station);
							}
						}
					}
				}
			}
			long loseCount;
			statisticList = new ArrayList<>(weatherstations.size());
			Statistics statisticTEMP;
			NumberFormat numberFormat= NumberFormat.getPercentInstance();     
			for (Weatherstation weatherstation : weatherstations) {
				weatherStaionId = weatherstation.getWeatherStationId();
				if (stationCounterMap.containsKey(weatherStaionId)) {
					station = stationCounterMap.get(weatherStaionId);
					loseCount = station.getTotalCount() - station.getReceiveCount();
					statisticTEMP = new Statistics(weatherstation.getWeatherStationName(), weatherstation.getWeatherStationNumber(), weatherstation.getWeatherStationCategoryName());
					statisticTEMP.setLoseCount(loseCount < 0 ? 0 : loseCount);
					statisticTEMP.setDelayedTimeCount(station.getDelayedTimeCount());
					statisticTEMP.setOnTimeCount(station.getOnTimeCount());
					statisticTEMP.setTotalCount(station.getReceiveCount()+statisticTEMP.getLoseCount());	
					
					statisticTEMP.setOnTimePercent(numberFormat.format(statisticTEMP.getOnTimeCount()/(statisticTEMP.getTotalCount()==0?1:statisticTEMP.getTotalCount())));
					statisticTEMP.setDelayedTimePercent(numberFormat.format(statisticTEMP.getDelayedTimeCount()/(statisticTEMP.getTotalCount()==0?1:statisticTEMP.getTotalCount())));
					statisticTEMP.setLosePercent(numberFormat.format(statisticTEMP.getLoseCount()/(statisticTEMP.getTotalCount()==0?1:statisticTEMP.getTotalCount())));
					statisticList.add(statisticTEMP);
				}
			}
		 
		} catch (Exception exception) {
			logger.error("Exception in {}", exception);
		}
		return statisticList == null ? new ArrayList<Statistics>(0) : statisticList;
	}

}
