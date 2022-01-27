package com.partners.weather.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.partners.entity.ParameterAttribute;
import com.partners.entity.TerminalParametersAttrs;
import com.partners.entity.Terminalparameters;
import com.partners.entity.WeatherStationTerminal;
import com.partners.weather.common.CommonResources;
import com.partners.weather.redis.RedisPoolManager;
import com.partners.weather.search.SearchIndexUtil;
import com.partners.weather.serialize.ObjectSerializeTransfer;
import com.partners.weather.service.IMonitorService;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class MonitorServiceImp implements IMonitorService {
	private static final Logger logger = LoggerFactory.getLogger(MonitorServiceImp.class);

	@Autowired
	JedisPool jedisPool;

	@Override
	public Map<String, String> getLastWeatherDetail(int stationId, ArrayList<WeatherStationTerminal> weatherStationTerminals) {
		Jedis client = null;
		Map<String, String> stationMonitorMap = new HashMap<>();
		try {
			ObjectSerializeTransfer<Terminalparameters> objectSerializeTransfer = new ObjectSerializeTransfer<>();
			RedisPoolManager.Init(jedisPool);
			client = RedisPoolManager.getJedis();
			Map<String, TerminalParametersAttrs> parameterMap = null;
			SearchResponse response = SearchIndexUtil.getClient().prepareSearch(CommonResources.WEATHERSYSTEMALIAS).setFrom(0).setSize(1).setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("stationId", stationId))).addSort(SortBuilders.fieldSort("systemDate").order(SortOrder.DESC)).execute().actionGet();
			SearchHit[] hits = response.getHits().getHits();
			if (hits != null && hits.length > 0) {
				Map<String, Object> searchResultMap = hits[0].getSource();
				final int categoryId = (int) searchResultMap.get("terminalParamterCategoryId");
				final List<String> terminalParameters = new ArrayList<>();
				for (WeatherStationTerminal terminalTemp : weatherStationTerminals) {
					if (terminalTemp.getTeminalParameterCategoryId() == categoryId) {
						if (!StringUtils.isBlank(terminalTemp.getTerminalParameters())) {
							String[] stationTerminalParameters = terminalTemp.getTerminalParameters().split(",");
							for (int i = 0; i < stationTerminalParameters.length; i++) {
								terminalParameters.add(stationTerminalParameters[i].trim());
							}
						}
						break;
					}
				}
				if (client.exists(String.valueOf(categoryId).getBytes())) {
					byte[] parameters = client.get(String.valueOf(categoryId).getBytes());
					if (parameters != null && parameters.length > 0) {
						Terminalparameters terminalparameter = (Terminalparameters) objectSerializeTransfer.deserialize(parameters);
						if (terminalparameter != null && terminalparameter.getTerminalParametersAttrs().size() > 0) {
							List<TerminalParametersAttrs> parametersAttrs = terminalparameter.getTerminalParametersAttrs();
							Collection<TerminalParametersAttrs> parametersAttrList = Collections2.filter(parametersAttrs, new Predicate<TerminalParametersAttrs>() {
								@Override
								public boolean apply(TerminalParametersAttrs input) {
									if (!StringUtils.isBlank(input.getId()) && terminalParameters.contains(input.getId())) {
										return true;
									} else {
										return false;
									}
								}
							});
							parameterMap = Maps.uniqueIndex(parametersAttrList, new Function<TerminalParametersAttrs, String>() {
								@Override
								public String apply(TerminalParametersAttrs input) {
									return input.getName().trim() + (input.isCustomeFiled() ? "" : ("_" + categoryId));
								}
							});
						}
					}
				}
				Iterator<String> paramIterator = parameterMap.keySet().iterator();
				String key, value;
				TerminalParametersAttrs terminalParametersAttrs;
				Map<String, ParameterAttribute> attributeMap;
				while (paramIterator.hasNext()) {
					key = paramIterator.next();
					value = searchResultMap.get(key).toString();
					terminalParametersAttrs = parameterMap.get(key);
					if (!StringUtils.isBlank(terminalParametersAttrs.getDescription())) {
						
						attributeMap = Maps.uniqueIndex(terminalParametersAttrs.getParameterAttributes(), new Function<ParameterAttribute, String>() {
							@Override
							public String apply(ParameterAttribute input) {
								return input.getName();
							}
						});
						if (attributeMap.get("unit") != null) {
							stationMonitorMap.put(String.format("%05d", terminalParametersAttrs.getOrder()) + "_" + terminalParametersAttrs.getDescription(), value + attributeMap.get("unit").getValue());
						} else {
							stationMonitorMap.put(String.format("%05d", terminalParametersAttrs.getOrder()) + "_" + terminalParametersAttrs.getDescription(), value);
						}
					}
				}
				if(searchResultMap.containsKey("fullCollectDate")){
					stationMonitorMap.put("fulldate", searchResultMap.get("fullCollectDate").toString());
				}
			}
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		} finally {
			RedisPoolManager.close(client);
		}
		return stationMonitorMap;
	}

	@Override
	public long getLastHourWeathers() {
		long count = 0;
		try {
			DateTime now = DateTime.now();
			SearchResponse response = SearchIndexUtil.getClient().prepareSearch(CommonResources.WEATHERSYSTEMALIAS).setFrom(0).setSize(0)
					.setQuery(QueryBuilders.boolQuery()
							.filter(QueryBuilders.rangeQuery("systemDate").format("yyyy-MM-dd HH:mm:ss").gt(now.plusDays(-1).toString("yyyy-MM-dd HH:mm:ss")).lt(now.toString("yyyy-MM-dd HH:mm:ss"))))
					.setExplain(false).get();
			count = response.getHits().getTotalHits();
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		return count;
	}
}
