package com.partners.weather.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.max.InternalMax;
import org.elasticsearch.search.aggregations.metrics.min.InternalMin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.partners.entity.AggregationEntity;
import com.partners.entity.WeatherStationTerminal;
import com.partners.entity.Weatherstation;
import com.partners.view.entity.VAgg;
import com.partners.weather.common.CommonResources;
import com.partners.weather.dao.IWeatherStationDAO;
import com.partners.weather.search.SearchIndexUtil;
import com.partners.weather.service.IAggregationService;

@Service
public class AggregationServiceImp implements IAggregationService {
	private static final Logger logger = LoggerFactory.getLogger(AggregationServiceImp.class);
	@Autowired
	IWeatherStationDAO weatherStationDAO;

	@Override
	public List<AggregationEntity> getAggregationRecords(VAgg vagg,	List<WeatherStationTerminal> terminals,Map<String, String> paramMap) {
		List<AggregationEntity> aggregationEntities = new ArrayList<>();
		try {
			SearchRequestBuilder requestBuilder = SearchIndexUtil.getClient().prepareSearch(CommonResources.WEATHERSYSTEMALIAS).setFrom(0).setSize(0).setQuery(QueryBuilders.boolQuery().filter(QueryBuilders
					.andQuery(QueryBuilders.rangeQuery("systemDate").format("yyyy-MM-dd HH:mm:ss").gt(vagg.getStartDate()).lt(vagg.getEndDate()), QueryBuilders.termsQuery("stationId", vagg.getWeatherStations()))));
			if (!terminals.isEmpty()) {
				AggregationBuilder aggregation = AggregationBuilders.terms("stationId").field("stationId");
				for (WeatherStationTerminal weatherStationTerminal : terminals) {
					for (String parameter : vagg.getParameters()) {
						aggregation.subAggregation(
								AggregationBuilders.max("max_" + parameter + "_" + weatherStationTerminal.getTeminalParameterCategoryId()).field(parameter + "_" + weatherStationTerminal.getTeminalParameterCategoryId()));
						aggregation.subAggregation(
								AggregationBuilders.min("min_" + parameter + "_" + weatherStationTerminal.getTeminalParameterCategoryId()).field(parameter + "_" + weatherStationTerminal.getTeminalParameterCategoryId()));
					}
				}
				requestBuilder.addAggregation(aggregation);
			}
			List<Weatherstation> stations = weatherStationDAO.getMutliWeatherStation(vagg.getWeatherStations());
			Map<Long, Weatherstation> stationMap=Maps.uniqueIndex(stations, new Function<Weatherstation,Long>(){
				@Override
				public Long apply(Weatherstation input) {
					return (long)input.getWeatherStationId();
				}
				
			});
			SearchResponse response = requestBuilder.execute().get();
			Aggregations aggregations = response.getAggregations();
			List<Terms.Bucket> buckets = null;
			if (aggregations != null) {
				LongTerms terms = (LongTerms) aggregations.asList().get(0);
				buckets = terms.getBuckets();
				InternalMax max;
				InternalMin min;
				AggregationEntity aggEntity;
				String tempKey = null, tempName = null,tempKeyParam=null;
				double value = 0;
				Map<String, AggregationEntity> maxOrMinMap = new HashMap<>();
				List<Aggregation> aggs;
				long bucketKey;
				for (Terms.Bucket bucket : buckets) {
					bucketKey=(long) bucket.getKey();
					if(!stationMap.containsKey(bucketKey)){
						continue;
					}
					aggs = bucket.getAggregations().asList();
					for (Aggregation childAgg : aggs) {
						if (childAgg instanceof InternalMax) {
							tempKey = "max_";
							max = (InternalMax) childAgg;
							value = max.getValue();
							tempName = max.getName();
						}
						if (childAgg instanceof InternalMin) {
							tempKey = "min_";
							min = (InternalMin) childAgg;
							value = min.getValue();
							tempName = min.getName();
						}
						if("-Infinity".indexOf(String.valueOf(value))!=-1){
							continue;
						}
						for (String parameter : vagg.getParameters()) {
							tempKeyParam=tempKey + parameter;
							if (tempName.indexOf(tempKeyParam) != -1) {
								if (maxOrMinMap.containsKey(tempKeyParam)) {
									aggEntity=maxOrMinMap.get(tempKeyParam);
								}else {
									aggEntity=new AggregationEntity();
								}
								if (maxOrMinMap.containsKey(tempKeyParam) && value <= maxOrMinMap.get(tempKeyParam).getValue()) {
									continue;
								}
								aggEntity.setWeatherStationName(stationMap.get(bucketKey).getWeatherStationName());
								aggEntity.setWeatherStationNumber(stationMap.get(bucketKey).getWeatherStationNumber());
								aggEntity.setTerminalName(parameter);
								aggEntity.setTerminalDesc(paramMap.get(tempName.split("_")[1]));
								aggEntity.setTerminalCategoryId(Integer.valueOf(tempName.split("_")[2]));
								aggEntity.setWeatherStationId((int)bucketKey);
								aggEntity.setType(tempKeyParam.startsWith("max_")?"最高值":"最低值");
								aggEntity.setValue(value);
								maxOrMinMap.put(tempKeyParam, aggEntity);
								break;
							}
						}
					}
				}
				Iterator<String> maxOrMinIterator=maxOrMinMap.keySet().iterator();
				while(maxOrMinIterator.hasNext()){
					aggEntity=maxOrMinMap.get(maxOrMinIterator.next());
					aggEntity.setLastDate(this.getLastDate(aggEntity.getWeatherStationId(),aggEntity.getTerminalName()+"_"+aggEntity.getTerminalCategoryId(),aggEntity.getValue(),vagg));
					aggEntity.setTerminalCategoryId(0);
					aggEntity.setWeatherStationId(0);
					aggregationEntities.add(aggEntity);
				}
//				Collections.sort(aggregationEntities, new Comparator() {
//					@Override
//					public int compare(Object v1, Object v2) {
//						AggregationEntity t1 = (AggregationEntity) v1;
//						AggregationEntity t2 = (AggregationEntity) v2;
//						return String.co(t1.getTerminalDesc(), t2.getTerminalDesc());
//					}
//				});
			}
		
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Error in {}", e);
		}
		return aggregationEntities==null?new ArrayList<AggregationEntity>(0):aggregationEntities;
	}
	private String getLastDate(int stationId,String field,double value,VAgg vagg) throws InterruptedException, ExecutionException {
		SearchResponse response=SearchIndexUtil.getClient().prepareSearch(CommonResources.WEATHERSYSTEMALIAS).setFrom(0).setSize(1).setQuery(
				QueryBuilders.boolQuery().must(QueryBuilders.termQuery("stationId", stationId))
				.must(QueryBuilders.termQuery(field, value)).filter(QueryBuilders.rangeQuery("systemDate").format("yyyy-MM-dd HH:mm:ss").gt(vagg.getStartDate()).lt(vagg.getEndDate()))).setExplain(false).execute().get();
		String systemDate=(String) response.getHits().getAt(0).getSource().get("systemDate");
		return systemDate;
	}
	
	@Override
	public double getAggRainfall(VAgg vagg,	WeatherStationTerminal terminal) {
		List<AggregationEntity> aggregationEntities = new ArrayList<>();
		double value = 0;
		try {
			SearchRequestBuilder requestBuilder = SearchIndexUtil.getClient().prepareSearch(CommonResources.WEATHERSYSTEMALIAS).setFrom(0).setSize(0).setQuery(QueryBuilders.boolQuery().filter(QueryBuilders
					.andQuery(QueryBuilders.rangeQuery("systemDate").format("yyyy-MM-dd HH:mm:ss").gt(vagg.getStartDate()).lt(vagg.getEndDate()), QueryBuilders.termsQuery("stationId", vagg.getWeatherStations()))));
			if (terminal != null) {
				AggregationBuilder aggregation = AggregationBuilders.dateHistogram("MaxSystemDate").field("systemDate").interval(DateHistogramInterval.HOUR).format("yyyy-MM-dd HH:mm:ss").minDocCount(1);
					for (String parameter : vagg.getParameters()) {
						aggregation.subAggregation(
								AggregationBuilders.max("max_" + parameter + "_" + terminal.getTeminalParameterCategoryId()).field(parameter + "_" + terminal.getTeminalParameterCategoryId()));
					}
				requestBuilder.addAggregation(aggregation);
			}

			SearchResponse response = requestBuilder.execute().get();
			Aggregations aggregations = response.getAggregations();
			  
			Histogram h=(Histogram)aggregations.asList().get(0);  
			List<Histogram.Bucket> buckets = (List<Histogram.Bucket>) h.getBuckets(); 
			List<Aggregation> aggs;
			InternalMax max;
			
			for(Histogram.Bucket bucket:buckets){ 
	            aggs = bucket.getAggregations().asList();
	            
	            for (Aggregation childAgg : aggs) {
					if (childAgg instanceof InternalMax) {
						max = (InternalMax) childAgg;
						value += max.getValue();
					}
	            }
			}
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Error in {}", e);
		}
		return value;
	}
}
