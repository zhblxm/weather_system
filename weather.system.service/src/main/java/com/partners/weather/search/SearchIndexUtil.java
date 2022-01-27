package com.partners.weather.search;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.partners.entity.ParameterAttribute;
import com.partners.entity.TerminalParametersAttrs;
import com.partners.entity.Terminalparameters;
import com.partners.weather.common.CommonResources;
import com.partners.weather.common.MappingType;
import com.partners.weather.pinyin.PinyinUtil;

import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class SearchIndexUtil {
	private static final Logger logger = LoggerFactory.getLogger(SearchIndexUtil.class);
	private static volatile TransportClient client = null;
	private static String host = "localhost";
	private static int port = 9300;
	static {
		/*try {
			String rootPath = SearchIndexUtil.class.getClassLoader().getResource("").toURI().getPath() + File.separator + "common.properties";
			File commonPropFile = new File(rootPath);
			if (commonPropFile.exists()) {
				FileInputStream fileInputStreamStream = new FileInputStream(rootPath);
				Properties propertyConfig = new Properties();
				propertyConfig.load(fileInputStreamStream);
				if (!StringUtils.isBlank(propertyConfig.getProperty("elasticsearch.host"))) {
					host = propertyConfig.getProperty("elasticsearch.host");
				}
				if (!StringUtils.isBlank(propertyConfig.getProperty("elasticsearch.port"))) {
					port = Integer.valueOf(propertyConfig.getProperty("elasticsearch.port"));
				}
				fileInputStreamStream.close();
			} else {
				throw new Exception("cannot be load the file of common.properties");
			}
			client = TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
		} catch (Exception ex) {
			logger.error("Error in {}", ex);
		}*/
	}

	public static Boolean createIndex(MappingType mappingType) {
		String indice, indiceAlias;
		switch (mappingType) {
		case INDEX:
			indice = CommonResources.WEATHERSYSTEM;
			indiceAlias = CommonResources.WEATHERSYSTEMALIAS;
			break;
		case INVALIDINDEX:
			indice = CommonResources.WEATHERSYSTEMINVALID;
			indiceAlias = CommonResources.WEATHERSYSTEMINVALIDALIAS;
			break;
		case INVALIDSTATION:
			indice = CommonResources.STATIONINVALID;
			indiceAlias = CommonResources.STATIONINVALIDALIAS;
			break;
		default:
			throw new IllegalArgumentException("参数错误！");
		}
		IndicesAdminClient indicesAdminClient = client.admin().indices();
		IndicesExistsRequest request = new IndicesExistsRequest(indice);
		IndicesExistsResponse response = indicesAdminClient.exists(request).actionGet();
		if (response.isExists()) {
			return true;
		}
		CreateIndexResponse createIndexResponse = indicesAdminClient.prepareCreate(indice).get();
		boolean createResponseResult = createIndexResponse.isAcknowledged();
		if (createResponseResult) {
			indicesAdminClient.prepareAliases().addAlias(indice, indiceAlias).execute().isDone();
		}
		return createResponseResult;
	}

	public static Boolean postWeatherData(XContentBuilder jsonBuilder, String type, MappingType mappingType) {
		if (jsonBuilder == null) {
			throw new NullPointerException("创建索引的json内容不能为空！");
		}
		if (logger.isDebugEnabled()) {
			logger.debug(jsonBuilder.toString());
		}
		String indiceAlias;
		switch (mappingType) {
		case INDEX:
			indiceAlias = CommonResources.WEATHERSYSTEMALIAS;
			break;
		case INVALIDINDEX:
			indiceAlias = CommonResources.WEATHERSYSTEMINVALIDALIAS;
			break;
		case INVALIDSTATION:
			indiceAlias = CommonResources.STATIONINVALIDALIAS;
			break;
		default:
			throw new IllegalArgumentException("参数错误或不支持映射类型！");
		}
		IndexResponse indexResponse = client.prepareIndex(indiceAlias, type).setSource(jsonBuilder).get();
		return indexResponse.isCreated();
	}

	public static Boolean createMapping(int categoryId, Terminalparameters terminalparameters, MappingType mappingType) throws IOException, BadHanyuPinyinOutputFormatCombination {
		boolean createMappingResult = true;
		String indice;
		switch (mappingType) {
		case INDEX:
			indice = CommonResources.WEATHERSYSTEM;
			break;
		case INVALIDINDEX:
			indice = CommonResources.WEATHERSYSTEMINVALID;
			break;
		default:
			throw new IllegalArgumentException("参数错误或不支持映射类型！");
		}
		IndicesAdminClient indicesAdminClient = client.admin().indices();
		GetMappingsRequest mappingsRequest = new GetMappingsRequest().indices(indice);
		GetMappingsResponse getMappingsResponse = indicesAdminClient.getMappings(mappingsRequest).actionGet();
		ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getMappingsResponse.getMappings();
		if (mappings != null && mappings.keys().size() >= 0) {
			Object[] mapingKeys = mappings.keys().toArray();
			for (Object key : mapingKeys) {
				if (!mappings.get((String) key).containsKey(terminalparameters.getFileName())) {
					XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
					List<String> systemDateTypes = Arrays.asList(CommonResources.SYSTEM_DATETYPE);
					jsonBuilder = jsonBuilder.startObject().startObject(terminalparameters.getFileName()).startObject("properties");
					ParameterAttribute parameterAttributeRsp;
					ParameterAttribute parameterDateFormat;
					for (TerminalParametersAttrs parametersAttrs : terminalparameters.getTerminalParametersAttrs()) {
						jsonBuilder = jsonBuilder.startObject(PinyinUtil.chineseToPinyin(parametersAttrs.getName()) + (parametersAttrs.isCustomeFiled()?"":("_" + categoryId)));
						parameterAttributeRsp = fileAttributes(parametersAttrs.getParameterAttributes(), "datatype");
						parameterDateFormat = fileAttributes(parametersAttrs.getParameterAttributes(), "datatypeformat");
						if (parameterAttributeRsp != null) {
							if (systemDateTypes.contains(parameterAttributeRsp.getValue()) && parameterDateFormat != null) {
								jsonBuilder = jsonBuilder.field("type", "date").field("format", parameterDateFormat.getValue());
							} else {
								jsonBuilder = jsonBuilder.field("type", parameterAttributeRsp.getValue());
								if ("string".equalsIgnoreCase(parameterAttributeRsp.getValue())) {
									jsonBuilder = jsonBuilder.field("index", "not_analyzed");
								}
							}
						}
						jsonBuilder = jsonBuilder.endObject();
					}
					jsonBuilder = jsonBuilder.endObject().endObject().endObject();
					PutMappingResponse putMappingResponse = indicesAdminClient.preparePutMapping(indice).setType(terminalparameters.getFileName()).setSource(jsonBuilder).get();
					createMappingResult = putMappingResponse.isAcknowledged();
				} else {
					logger.info("Mapping已经存在！");
				}
				break;

			}
		} else {
			logger.info("Mapping已经存在！");
		}
		return createMappingResult;
	}

	private static ParameterAttribute fileAttributes(final List<ParameterAttribute> parameterAttributes, final String key) {
		ParameterAttribute parameterAttributeRsp = null;
		for (ParameterAttribute parameterAttribute : parameterAttributes) {
			if (parameterAttribute.getName().equalsIgnoreCase(key)) {
				parameterAttributeRsp = parameterAttribute;
				break;
			}
		}
		return parameterAttributeRsp;
	}

	public static TransportClient getClient() {
		return client;
	}
	
	public static void closeTransportClient() {
		client.close();
	}
}
