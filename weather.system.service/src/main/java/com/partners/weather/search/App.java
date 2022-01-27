package com.partners.weather.search;

import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalHistogram;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalHistogram.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.metrics.max.InternalMax;

import com.partners.weather.common.CommonResources;

public class App {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException, ParseException {
		TransportClient client = null;
		try {
			//以下就是查询指定时间段内的，指定站点条件后，多查询出来的数据进行聚合。
			//聚合的按照小时或分钟，取单位时间内最大值，然后查询结果有站点ID，时间，最大值
			DateHistogramBuilder dateHistogramBuilder = AggregationBuilders.dateHistogram("CollectDate").field("fullCollectDate")
					.interval(DateHistogramInterval.HOUR)//如果用分钟，就改成DateHistogramInterval.MINUTE
					.minDocCount(1);
			//maxValue可以随意，但后面要相应替换，fild就是需要查询的要素。
			dateHistogramBuilder.subAggregation(AggregationBuilders.max("maxValue").field("temperature_21"))
			.subAggregation(AggregationBuilders.terms("station").field("stationId"));
			
			client = TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
			SearchResponse response = client.prepareSearch(CommonResources.WEATHERSYSTEM).setFrom(0).setSize(10000).// 最大是1万，也是ES的最大值
					setQuery(QueryBuilders.boolQuery()
							.filter(QueryBuilders.andQuery(
									// 日期区间，该后面的值就行
									QueryBuilders.rangeQuery("fullCollectDate").format("yyyy-MM-dd HH:mm:ss").gt("2014-11-1 11:12:22").lt("2017-11-1 11:12:22"),
									// 站点ID，23和30是站点的ID，可传递1个或多个，不需要可将这段代码删除
									QueryBuilders.termsQuery("stationId", new int[] { 23, 30 })
					// 设备编号，可以传递一个或多个，用不到这个条件，可以删除
					// ,QueryBuilders.termsQuery("terminalModel", new String[] {
					// "1006" })
					))).addAggregation(dateHistogramBuilder).setExplain(false).get();
//			SearchHit[] hits = response.getHits().getHits();
//			for (SearchHit hit : hits) {
//				String fullCollectDate = (String) hit.getSource().get("fullCollectDate");
//				if (StringUtils.isBlank(fullCollectDate)) {
//					continue;
//				}
//				System.out.println(fullCollectDate);
//			}
//			System.out.println("-----------------------------------");
			Aggregations aggs = response.getAggregations();
			if (aggs != null) {
				Map<String, Aggregation> map = aggs.asMap();
				Iterator<Entry<String, Aggregation>> iter = map.entrySet().iterator();
				while (iter.hasNext()) {
					// 只有一次循环，因为只有一个key
					Entry<String, Aggregation> entry = iter.next();
					Object key = entry.getKey();
					InternalHistogram<Bucket> val = (InternalHistogram<Bucket>) entry.getValue();
					List<Bucket> list = val.getBuckets();
					Map<String, Aggregation> childmap; 
					Number stationId;
					double maxVal;					
					InternalMax internalMax;
					for (int i = 0; i < list.size(); i++) {
						System.out.println(list.get(i).getKeyAsString());
						childmap = list.get(i).getAggregations().asMap();
						stationId= ((LongTerms) childmap.get("station")).getBuckets().get(0).getKeyAsNumber();		
						internalMax=(InternalMax) childmap.get("maxValue");
						if(internalMax.getValueAsString().equalsIgnoreCase("-Infinity")){
							maxVal=0;
						}else {
							maxVal=internalMax.value();
						}					
						System.out.println(String.format("stationid is %s, max value is %s",stationId,maxVal));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		client.close();

	}
}
