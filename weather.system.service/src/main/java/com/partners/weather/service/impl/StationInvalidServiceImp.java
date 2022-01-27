package com.partners.weather.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.partners.entity.Invalid;
import com.partners.entity.Weatherstation;
import com.partners.view.entity.VInvalid;
import com.partners.weather.dao.IWeatherStationDAO;
import com.partners.weather.common.CommonResources;
import com.partners.weather.search.SearchIndexUtil;
import com.partners.weather.service.IStationInvalidService;

@Service
public class StationInvalidServiceImp implements IStationInvalidService {
	private static final Logger logger = LoggerFactory.getLogger(StationInvalidServiceImp.class);
	@Autowired
	IWeatherStationDAO weatherStationDAO;
	@Override
	public List<Invalid> getInvalidRecords(VInvalid invalid) {
		List<Invalid> invalids=new ArrayList<Invalid>();
		try {
			@SuppressWarnings("deprecation")
			SearchResponse response = SearchIndexUtil.getClient().prepareSearch(CommonResources.WEATHERSYSTEMINVALIDALIAS).setFrom(invalid.getStartIndex()).setSize(invalid.getSize())
					.setQuery(QueryBuilders.boolQuery().filter(QueryBuilders.andQuery(QueryBuilders.rangeQuery("systemDate").format("yyyy-MM-dd HH:mm:ss").gt(invalid.getStartDate()).lt(invalid.getEndDate()),
							QueryBuilders.termsQuery("stationId", invalid.getWeatherStations()),QueryBuilders.termsQuery("invalidfield", invalid.getParameters()))))
					.execute().get();
			SearchHits hits=response.getHits();
			invalid.setTotalRecords((int)response.getHits().getTotalHits());
			Invalid invalidTemp;
			DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
			for (SearchHit searchHit : hits) {	
				invalidTemp=new Invalid();
				invalidTemp.setStationId((int)searchHit.getSource().get("stationId"));
				invalidTemp.setInvalidField((String)searchHit.getSource().get("invalidfield"));
				invalidTemp.setInvalidFieldDesc((String)searchHit.getSource().get("invalidfielddesc"));
				invalidTemp.setMinValue((double)searchHit.getSource().get("minvalue"));
				invalidTemp.setMaxValue((double)searchHit.getSource().get("maxvalue"));
				invalidTemp.setValue((double)searchHit.getSource().get("value"));
				invalidTemp.setCollectionDate(new Timestamp((DateTime.parse((String)searchHit.getSource().get("systemDate"),format)).getMillis()));
				invalids.add(invalidTemp);
			}
			if(invalids.size()>0)
			{
				List<Weatherstation> weatherstations=weatherStationDAO.getMutliWeatherStation(invalid.getWeatherStations());
				Map<Integer, Weatherstation> weatherStationMap = Maps.uniqueIndex(weatherstations, new Function<Weatherstation, Integer>() {
					@Override
					public Integer apply(Weatherstation input) {
						return input.getWeatherStationId();
					}
				});
				for (Invalid invalidItem : invalids) {
					if(weatherStationMap.containsKey(invalidItem.getStationId())){
						invalidItem.setWeatherStationName(weatherStationMap.get(invalidItem.getStationId()).getWeatherStationName());
						invalidItem.setWeatherStationNumber(weatherStationMap.get(invalidItem.getStationId()).getWeatherStationNumber());
						invalidItem.setStationId(0);
					}
				}
			}
			// FIXME: 2022/1/25
		}catch (InterruptedException e) {
			logger.error("Error in {}",e);
		}
		catch ( ExecutionException e) {
			logger.error("Error in {}",e);
		}
		return invalids;
	}

}
