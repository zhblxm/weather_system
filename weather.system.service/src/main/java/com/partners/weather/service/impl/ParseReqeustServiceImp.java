package com.partners.weather.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Service;

import com.partners.entity.ClientInfo;
import com.partners.entity.Emalilandsmssettings;
import com.partners.entity.FilePushFlag;
import com.partners.entity.Heartbeat;
import com.partners.entity.Invalid;
import com.partners.entity.MonitorBatter;
import com.partners.entity.Notification;
import com.partners.entity.ParameterAttribute;
import com.partners.entity.RequestMessage;
import com.partners.entity.SystemOption;
import com.partners.entity.TerminalParametersAttrs;
import com.partners.entity.Terminalhistory;
import com.partners.entity.Terminalparameters;
import com.partners.entity.Terminalparameterscategory;
import com.partners.entity.WeatherStationTerminal;
import com.partners.weather.battery.BatteryUtil;
import com.partners.weather.common.Battery;
import com.partners.weather.common.CommonResources;
import com.partners.weather.common.DataSourceEnum;
import com.partners.weather.common.MappingType;
import com.partners.weather.common.NotificationType;
import com.partners.weather.common.RedisKey;
import com.partners.weather.ftp.FtpUtil;
import com.partners.weather.ftp.SftpUtil;
import com.partners.weather.mail.EmailHelper;
import com.partners.weather.redis.RedisPoolManager;
import com.partners.weather.search.SearchIndexUtil;
import com.partners.weather.serialize.ListSerializeTransfer;
import com.partners.weather.serialize.ObjectSerializeTransfer;
import com.partners.weather.service.IClientInfoService;
import com.partners.weather.service.IEmailSettingService;
import com.partners.weather.service.IFilePushFlagService;
import com.partners.weather.service.INotificationService;
import com.partners.weather.service.IParseReqeustService;
import com.partners.weather.service.ISystemOptionService;
import com.partners.weather.service.ITerminalHistoryService;
import com.partners.weather.service.ITerminalParamCategoryService;
import com.partners.weather.service.IWeatherStationService;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class ParseReqeustServiceImp implements IParseReqeustService {
	private static final Logger logger = LoggerFactory.getLogger(ParseReqeustServiceImp.class);
	private static final String separator = "\\s+";
	private static final String fieldPrefix = "item_";
	private static final Pattern IDPATTERN = Pattern.compile("[0-9]*");
	private static final ReentrantLock cachelock = new ReentrantLock();
	@Autowired
	JedisPool jedisPool;
	@Autowired
	IWeatherStationService weatherStationService;
	@Autowired
	ITerminalParamCategoryService terminalParamCategoryService;
	@Autowired
	ITerminalHistoryService terminalHistoryService;
	@Autowired
	private RedisAtomicLong redisAtomicLong;
	@Autowired
	private IClientInfoService clientInfoService;
	@Autowired
	private INotificationService notificationService;
	@Autowired
	private IEmailSettingService emailSettingService;
	@Autowired
	ISystemOptionService systemOptionService;
	@Autowired
	IFilePushFlagService filePushFlagService;
	
	private static ListSerializeTransfer<Heartbeat> lsSerializeTransfer = new ListSerializeTransfer<Heartbeat>();
	private static ObjectSerializeTransfer<Terminalparameters> objectSerializeTransfer = new ObjectSerializeTransfer<Terminalparameters>();
	@Override
	public void parse(RequestMessage message) throws Exception {
		Jedis client = null;
		if (StringUtils.isBlank(message.getRequestMessage())) {
			throw new NullPointerException("接收到的信息为空。");
		}
		if (logger.isInfoEnabled()) {
			logger.info(String.format("Receive result is %s:%s.", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"), message.getRequestMessage()));
		}
		try {
			ClientInfo info = new ClientInfo(message.getClientIP(), message.getPort());
			RedisPoolManager.Init(jedisPool);
			client = RedisPoolManager.getJedis();
			byte[] key;
			
			boolean exists = false;	
			if (message.getRequestMessage().startsWith("$$$")) {
				// 心跳数据						
				String weatherStaionNumber=message.getRequestMessage().substring(3).trim();
				if(StringUtils.isBlank(weatherStaionNumber)){
					logger.error("心跳信息缺少站点信息！");
					return;
				}
				this.saveHeartbeatInfo(client,info,weatherStaionNumber);
				return;
			}
			String[] weatherItems = message.getRequestMessage().split(separator);
			if (weatherItems.length < 2) {
				throw new Exception("信息格式长度不符合要求！");
			}
			String terminalModel = "";
			for (int i = 0; i < 2; i++) {
				if (IDPATTERN.matcher(String.valueOf(weatherItems[i])).matches()) {
					terminalModel = weatherItems[i];
					break;
				}
			}
			if (StringUtils.isBlank(terminalModel)) {
				throw new Exception("信息未包含ID，ID为必须信息，请联系管理员！");
			}
			Emalilandsmssettings settings=emailSettingService.getEmalilandsmssetting();
//			byte[] notificationKeys = CommonResources.NOTIFICATIONS.getBytes();
//			List<Notification> notifications = new ArrayList<>();
//			if (client.exists(notificationKeys)) {
//				notifications = (List<Notification>) lsSerializeTransfer.deserialize(client.get(notificationKeys));
//			}
			terminalModel = terminalModel.trim();		 
			if (client.exists(RedisKey.ALLSTATION.getBytes())) {
				byte[] stations=client.hget(RedisKey.ALLSTATION.getBytes(), terminalModel.getBytes());
				if(stations!=null && stations.length>0){
					String weatherStationNumber=new String(stations);
					this.saveHeartbeatInfo(client,info,weatherStationNumber);	
				}				
			}
			List<WeatherStationTerminal> weatherStationTerminals;
			List<byte[]> weatherTerminals = client.hmget(RedisKey.ALLWEATHERSYSTEMKEY.getBytes(), terminalModel.getBytes());
		
			WeatherStationTerminal weatherStationTerminal = null;
			byte[] weatherStationTerminalBs = null;
			boolean createIndexResult = false;
			if (weatherTerminals == null || weatherTerminals.size() == 0 || weatherTerminals.get(0) == null) {
				try {
					cachelock.lock();
					weatherStationTerminals = weatherStationService.getWeatherStationTerminals();
					if (weatherStationTerminals.size() > 0) {
						Map<byte[], byte[]> weatherStationTerminalMap = new HashMap<byte[], byte[]>();
						byte[] objectBs;
						for (WeatherStationTerminal stationTerminal : weatherStationTerminals) {
							objectBs = objectSerializeTransfer.serialize(stationTerminal);
							if (terminalModel.trim().equals(stationTerminal.getTerminalModel().trim())) {
								weatherStationTerminalBs = objectBs;
							}
							weatherStationTerminalMap.put(stationTerminal.getTerminalModel().getBytes(), objectBs);
						}
						if (logger.isDebugEnabled()) {
							long weatherTerminalLen = client.hlen(RedisKey.ALLWEATHERSYSTEMKEY.getBytes());
						}
						client.hmset(RedisKey.ALLWEATHERSYSTEMKEY.getBytes(), weatherStationTerminalMap);
					}
				} finally {
					cachelock.unlock();
				}
			} else {
				weatherStationTerminalBs = weatherTerminals.get(0);
			}
			if (weatherStationTerminalBs != null && weatherStationTerminalBs.length > 0) {
				weatherStationTerminal = (WeatherStationTerminal) objectSerializeTransfer.deserialize(weatherStationTerminalBs);
			}
			// 保持下载的原始数据
			DateTime now = DateTime.now();
			Terminalhistory terminalhistory = new Terminalhistory();
			terminalhistory.setDetail(message.getRequestMessage());
			terminalhistory.setSource(DataSourceEnum.GRPS.name());
			terminalhistory.setExtendInfo("");
			terminalhistory.setTerminalCatetoryId(0);
			terminalhistory.setWeatherStationId(0);
			terminalhistory.setInvalid(1);
			if (weatherStationTerminal != null) {
				terminalhistory.setTerminalCatetoryId(weatherStationTerminal.getTeminalParameterCategoryId());
				terminalhistory.setWeatherStationId(weatherStationTerminal.getWeatherStationId());
				byte[] terminalParamBs = client.get(String.valueOf(weatherStationTerminal.getTeminalParameterCategoryId()).getBytes());
				if (terminalParamBs == null || terminalParamBs.length == 0) {
					throw new Exception("要素信息解析文件无法找到！");
				}
				Terminalparameters terminalParams = (Terminalparameters) objectSerializeTransfer.deserialize(terminalParamBs);
				List<TerminalParametersAttrs> terminalParametersAttrs = terminalParams.getTerminalParametersAttrs();
				List<ParameterAttribute> parameterAttributes;
				String minValue = "", maxValue = "", dateType = "", type = "", dataConvert = "", paramUnit = "";
				double convertAfterData = 1;
				XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
				jsonBuilder = jsonBuilder.startObject();
				int size = terminalParametersAttrs.size() - CommonResources.extParametersSize;
				Map<String, String> dateParams = new HashMap<String, String>();
				List<String> baseDateList = Arrays.asList(CommonResources.SYSTEM_DATETYPE);
				List<Invalid> invalids = new ArrayList<>();
				Invalid invalidData;
				double lastVoltage = 0;
				
				//获取z文件配置及模板
				String ftpurl = null, ftpport = null, ftpuser = null, ftppwd = null, ftptype = null, ftponoff, templetContent = null, ledContent = "";
				List<FilePushFlag> filePushFlags = null;
				FilePushFlag filePushFlag;
				List<SystemOption> systemOptions= systemOptionService.getSystemOptions();
				SystemOption systemOption = systemOptionService.getSystemOption(systemOptions,CommonResources.FILEPUSHONOFF);
				ftponoff = systemOption == null ? "off" : systemOption.getOptionValue();
				File file = new File(SearchIndexUtil.class.getClassLoader().getResource("").toURI().getPath());

				String templetPath = file.getParentFile().getParent() + File.separator + "resources" + File.separator + "download";
				File templetFile = new File(templetPath + File.separator  + "filetempletup.txt");
				if(ftponoff.equals("on") && templetFile.exists()){
					filePushFlags = filePushFlagService.getAllFilePushFlags();
					systemOption = systemOptionService.getSystemOption(systemOptions,CommonResources.FILEPUSHFTPURL);
					ftpurl = systemOption==null?"":systemOption.getOptionValue();
					systemOption = systemOptionService.getSystemOption(systemOptions,CommonResources.FILEPUSHFTPPORT);
					ftpport = systemOption==null?"":systemOption.getOptionValue();
					systemOption = systemOptionService.getSystemOption(systemOptions,CommonResources.FILEPUSHFTPUSER);
					ftpuser = systemOption==null?"":systemOption.getOptionValue();
					systemOption = systemOptionService.getSystemOption(systemOptions,CommonResources.FILEPUSHFTPTYPE);
					ftptype = systemOption==null?"ftp":systemOption.getOptionValue();	
					systemOption = systemOptionService.getSystemOption(systemOptions,CommonResources.FILEPUSHFTPPWD);
					ftppwd = systemOption==null?"ftp":systemOption.getOptionValue();
					
					templetContent = txt2String(templetFile);
				}
				
				for (int i = 0; i < size; i++) {
					parameterAttributes = terminalParametersAttrs.get(i).getParameterAttributes();
					minValue = fileAttributes(parameterAttributes, "minvalue");
					dateType = fileAttributes(parameterAttributes, "datatype");
					maxValue = fileAttributes(parameterAttributes, "maxvalue");
					dataConvert = fileAttributes(parameterAttributes, "dataconvert");
					paramUnit = fileAttributes(parameterAttributes, "unit");
					if(paramUnit == null)
						paramUnit = "";
					
					if (baseDateList.contains(dateType) && !dateParams.containsKey(dateType)) {
						dateParams.put(dateType, weatherItems[i].trim());
					}
					if ("double".equalsIgnoreCase(dateType) || "integer".equalsIgnoreCase(dateType)) {
						convertAfterData = Double.valueOf(weatherItems[i].trim());
						if (!StringUtils.isBlank(dataConvert)) {
							if (dataConvert.contains("乘")) {
								convertAfterData = convertAfterData * Integer.valueOf(dataConvert.replace("乘", ""));
							} else if (dataConvert.contains("除")) {
								convertAfterData = convertAfterData / Integer.valueOf(dataConvert.replace("除", ""));
							}
						}
						if(terminalParametersAttrs.get(i).getDescription().indexOf("电压")!=-1){
							lastVoltage=convertAfterData;
						}
						if ((!StringUtils.isBlank(minValue) && Double.valueOf(minValue) > convertAfterData) || (!StringUtils.isBlank(maxValue) && Double.valueOf(maxValue) < convertAfterData)) {
							invalidData = new Invalid();
							invalidData.setInvalidField(terminalParametersAttrs.get(i).getName());
							invalidData.setInvalidFieldDesc(terminalParametersAttrs.get(i).getDescription());
							invalidData.setMaxValue(Double.valueOf(maxValue));
							invalidData.setMinValue(Double.valueOf(minValue));
							invalidData.setValue(convertAfterData);
							invalidData.setCovert(dataConvert);
							invalids.add(invalidData);
							continue;
						}
						jsonBuilder.field(terminalParametersAttrs.get(i).getName() + "_" + weatherStationTerminal.getTeminalParameterCategoryId(), convertAfterData);
					} else {
						jsonBuilder.field(terminalParametersAttrs.get(i).getName() + "_" + weatherStationTerminal.getTeminalParameterCategoryId(), weatherItems[i].trim());
					}
					
					if(templetContent != null && filePushFlags != null){
						filePushFlag = filePushFlagService.getFilePushFlag(filePushFlags, terminalParametersAttrs.get(i).getName());
						if(filePushFlag != null){
							templetContent = templetContent.replace(filePushFlag.getFlag(), weatherItems[i].trim());
						}
					}
					
					ledContent += terminalParametersAttrs.get(i).getDescription() + ":" + weatherItems[i].trim() + paramUnit + "\r\n";
				}
				DateTime fullCollectDate = now;
				long systemUniqueId = redisAtomicLong.incrementAndGet();
				if (size + CommonResources.extParametersSize == terminalParametersAttrs.size()) {
					if (dateParams.size() > 0) {
						if (dateParams.containsKey("datetime")) {
							fullCollectDate = DateTime.parse(dateParams.get("datetime"), DateTimeFormat.forPattern("yyyyMMddHHmmss"));
						} else if (dateParams.containsKey("date") && dateParams.containsKey("time")) {
							fullCollectDate = DateTime.parse(dateParams.get("date") + " " + dateParams.get("time"), DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
						} else if (dateParams.containsKey("date")) {
							fullCollectDate = DateTime.parse(dateParams.get("date"), DateTimeFormat.forPattern("yyyy-MM-dd"));
						} else if (dateParams.containsKey("time")) {
							fullCollectDate = DateTime.parse(now.toString("yyyy-MM-dd") + " " + dateParams.get("time"), DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
						}
					}
					jsonBuilder.field(terminalParametersAttrs.get(size).getName(), fullCollectDate.toString("yyyy-MM-dd HH:mm:ss"));
					jsonBuilder.field(terminalParametersAttrs.get(size + 1).getName(), now.toString("yyyy-MM-dd HH:mm:ss"));
					jsonBuilder.field(terminalParametersAttrs.get(size + 2).getName(), weatherStationTerminal.getWeatherStationId());
					jsonBuilder.field(terminalParametersAttrs.get(size + 3).getName(), systemUniqueId);
					String unit = weatherStationTerminal.getAcquisitionFrequencyUnit();
					boolean isDelayed = false;
					PeriodType periodType = "d".equalsIgnoreCase(unit) ? PeriodType.days() : ("h".equalsIgnoreCase(unit) ? PeriodType.hours() : PeriodType.minutes());
					if (periodType != null) {
						Period period = new Period(CommonResources.SYSTEMLAUNCHDATE, now, periodType);
						int periodCount = "d".equalsIgnoreCase(unit) ? period.getDays() : ("h".equalsIgnoreCase(unit) ? period.getHours() : period.getMinutes());
						periodCount = periodCount % (weatherStationTerminal.getAcquisitionFrequency() == 0 ? 1 : weatherStationTerminal.getAcquisitionFrequency());
						DateTime lastDateTime = now;
						switch (unit) {
						case "d":
							lastDateTime.plusDays(periodCount);
							break;
						case "h":
							lastDateTime.plusHours(periodCount);
							break;
						case "m":
							lastDateTime.plusMinutes(periodCount);
							break;
						}
						period = new Period(fullCollectDate, lastDateTime, periodType);
						periodCount = Math.abs("d".equalsIgnoreCase(unit) ? period.getDays() : ("h".equalsIgnoreCase(unit) ? period.getHours() : period.getMinutes()));
						isDelayed = periodCount > CommonResources.DELAYEDCOUNT;
					}
					jsonBuilder.field(terminalParametersAttrs.get(size + 4).getName(), isDelayed ? 1 : 0);
					jsonBuilder.field(terminalParametersAttrs.get(size + 5).getName(), now.getMinuteOfHour() == 0 ? 1 : 0);
					jsonBuilder.field(terminalParametersAttrs.get(size + 6).getName(), weatherStationTerminal.getTeminalParameterCategoryId());
					jsonBuilder.field(terminalParametersAttrs.get(size + 7).getName(), terminalModel);
				}
				
				if(templetContent != null && filePushFlags != null){
					filePushFlag = filePushFlagService.getFilePushFlag(filePushFlags, "fullCollectDate");
					if(filePushFlag != null){
						templetContent = templetContent.replace(filePushFlag.getFlag(), fullCollectDate.toString("yyyyMMddHHmmss"));
					}
					
					filePushFlag = filePushFlagService.getFilePushFlag(filePushFlags, "terminalModel");
					if(filePushFlag != null){
						templetContent = templetContent.replace(filePushFlag.getFlag(), terminalModel);
					}
				}
				
				jsonBuilder = jsonBuilder.endObject();

				List<String> categories = client.hmget(RedisKey.ALLCATETORY, String.valueOf(weatherStationTerminal.getTeminalParameterCategoryId()));
				if (categories == null || categories.size() == 0) {
					ArrayList<Terminalparameterscategory> terminalparameterscategories = terminalParamCategoryService.getAllCategories();
					if (terminalparameterscategories.size() > 0) {
						Map<String, String> terminalCatetoryMap = new HashMap<>();
						for (Terminalparameterscategory terminalparameterscategory : terminalparameterscategories) {
							terminalCatetoryMap.put(String.valueOf(terminalparameterscategory.getTerminalParamCategoryId()), terminalparameterscategory.getMappingName());
						}
						// client.hdel(RedisKey.ALLCATETORY);
						client.hmset(RedisKey.ALLCATETORY, terminalCatetoryMap);
						type = terminalCatetoryMap.get(String.valueOf(weatherStationTerminal.getTeminalParameterCategoryId()));
					}
				} else {
					type = categories.get(0);
				}
				if (StringUtils.isBlank(type)) {
					throw new NullPointerException("系统中未获取到要素分类信息！");
				}
				MonitorBatter monitorBatter=new MonitorBatter();
				monitorBatter.setStationId( weatherStationTerminal.getWeatherStationId());
				monitorBatter.setTerminalCagegoryId(weatherStationTerminal.getTeminalParameterCategoryId());
				monitorBatter.setLastUpdateTime(now);
				monitorBatter.setBatterValue(lastVoltage);
				monitorBatter.setInvalid(invalids.size() != 0);
				monitorBatter.setBatterPercent(BatteryUtil.getBatteryPercent(Battery.valueOf(weatherStationTerminal.getBatteryType()), monitorBatter.getBatterValue()));
				List<MonitorBatter> monitorBatters = new ArrayList<>();
				key = CommonResources.MONITORBATTERY.getBytes();
				if (client.exists(key)) {
					monitorBatters = (List<MonitorBatter>) lsSerializeTransfer.deserialize(client.get(key));
				}
				for (MonitorBatter m : monitorBatters) {
					if (m.getStationId()==monitorBatter.getStationId() && m.getTerminalCagegoryId()==m.getTerminalCagegoryId()) {
						exists = true;
						m.setBatterValue(monitorBatter.getBatterValue());
						m.setBatterPercent(monitorBatter.getBatterPercent());
						m.setLastUpdateTime(monitorBatter.getLastUpdateTime());
						m.setInvalid(monitorBatter.isInvalid());
						break;
					}
				}
				if (!exists) {
					monitorBatters.add(monitorBatter);
				}
				client.set(key, lsSerializeTransfer.serialize(monitorBatters));						
				if(monitorBatter.getBatterValue()<30 || monitorBatter.isInvalid()){
					Notification notification = null;					
					if(monitorBatter.getBatterValue()<30){
						notification=new Notification();
						notification.setNotificationType(NotificationType.BatterAlert.getValue());
						notification.setNotificationDesc(NotificationType.BatterAlert.getDescription());
						notification.setMessage(String.format("编号为%s的设备电量低", terminalModel));
						notification.setIsChecked((byte)1);
						notificationService.inserNotification(notification);
						EmailHelper.SendEmail(settings, notification, NotificationType.BatterAlert);
//						notifications.add(notification);
					}
					if(monitorBatter.isInvalid()){
						notification=new Notification();
						notification.setNotificationType(NotificationType.InvalidAlert.getValue());
						notification.setNotificationDesc(NotificationType.InvalidAlert.getDescription());
						notification.setMessage(String.format("编号为%s的设备发来无效数据", terminalModel));
						notification.setIsChecked((byte)1);
						notificationService.inserNotification(notification);
						EmailHelper.SendEmail(settings, notification, NotificationType.InvalidAlert);
//						notifications.add(notification);
					}
//					client.set(notificationKeys, lsSerializeTransfer.serialize(notifications));				
				}
				if (invalids.size() == 0) {
					// System.out.println(type + "===" + jsonBuilder.string());
					terminalhistory.setInvalid(0);
					createIndexResult = SearchIndexUtil.postWeatherData(jsonBuilder, type, MappingType.INDEX);
				} else {
					XContentBuilder invalidJsonBuilder;
					for (Invalid invalidItem : invalids) {
						invalidJsonBuilder = XContentFactory.jsonBuilder();
						invalidJsonBuilder = invalidJsonBuilder.startObject();
						invalidJsonBuilder.field("invalidfield", invalidItem.getInvalidField());
						invalidJsonBuilder.field("invalidfielddesc", invalidItem.getInvalidFieldDesc());
						invalidJsonBuilder.field("maxvalue", invalidItem.getMaxValue());
						invalidJsonBuilder.field("minvalue", invalidItem.getMinValue());
						invalidJsonBuilder.field("value", invalidItem.getValue());
						invalidJsonBuilder.field("convert", invalidItem.getCovert());
						invalidJsonBuilder.field("fullCollectDate", fullCollectDate.toString("yyyy-MM-dd HH:mm:ss"));
						invalidJsonBuilder.field("systemDate", now.toString("yyyy-MM-dd HH:mm:ss"));
						invalidJsonBuilder.field("stationId", weatherStationTerminal.getWeatherStationId());
						invalidJsonBuilder.field("weatherhistoryId", systemUniqueId);
						invalidJsonBuilder.field("systemShortDate", now.getMinuteOfHour() == 0 ? 1 : 0);
						invalidJsonBuilder.field("terminalParamterCategoryId", weatherStationTerminal.getTeminalParameterCategoryId());
						invalidJsonBuilder.field("terminalModel", terminalModel);
						invalidJsonBuilder = invalidJsonBuilder.endObject();
						// 不符合规范数据
						createIndexResult = SearchIndexUtil.postWeatherData(invalidJsonBuilder, type, MappingType.INVALIDINDEX);
					}
				}
				
				if(ledContent.length() > 0)
				{
					FileWriter writer;
					
		        	try {
		        		String fileTempletName = "Date" + terminalModel + ".txt";
			        	String fileTempletPath = templetPath + File.separator  + "ledFile" + File.separator + fileTempletName;
			        	File ledFile = new File(fileTempletPath);
			        	
			        	if(ledFile.exists()){
			        		ledFile.delete();
						}
			        	
			        	writer = new FileWriter(ledFile);
			            writer.write(ledContent);
			            writer.flush();
			            writer.close();
		        	}catch (IOException e) {
			        	logger.error("Exception in {}", e);
			        }
				}
				
				if(templetContent != null && filePushFlags != null){
					FileWriter writer;
			        try {
			        	for (FilePushFlag optionItem : filePushFlags) {
			        		templetContent = templetContent.replace(optionItem.getFlag(), optionItem.getDefaultValue());
						}
			        	
			        	String fileTempletName = terminalModel + fullCollectDate.toString("yyyyMMddHHmmss") + ".txt";
			        	String fileTempletPath = templetPath + File.separator  + fileTempletName;
			        	File fileTempletPush = new File(fileTempletPath);
			            writer = new FileWriter(fileTempletPush);
			            writer.write(templetContent);
			            writer.flush();
			            writer.close();
			        
						if(ftptype.equalsIgnoreCase("ftp")){
							FtpUtil ftpUtil = new FtpUtil();
							ftpUtil.connect(ftpurl, Integer.parseInt(ftpport), ftpuser, ftppwd, true);
							if(ftpUtil.isConnected())
							{
								ftpUtil.upload(fileTempletName, fileTempletPush);
								ftpUtil.disconnect();
							}
						}else{
							SftpUtil sftpUtil = new SftpUtil(ftpurl, ftpuser, Integer.parseInt(ftpport));
							sftpUtil.connect(ftppwd);
							sftpUtil.upload(fileTempletName, "/" + fileTempletName);
							sftpUtil.disconnect();
						}
						
						if(fileTempletPush.exists()){
							fileTempletPush.delete();
						}
			        } catch (IOException e) {
			        	logger.error("Exception in {}", e);
			        }
				}
			} else {				 
				Notification notification = new Notification();
				notification.setNotificationType(NotificationType.ErrorStationAlert.getValue());
				notification.setNotificationDesc(NotificationType.ErrorStationAlert.getDescription());
				notification.setMessage(String.format("未发现编号为%s的设备所属站点", terminalModel));
				notification.setIsChecked((byte)1);
				notificationService.inserNotification(notification);
				EmailHelper.SendEmail(settings, notification, NotificationType.ErrorStationAlert);
//				notifications.add(notification);				 
//				client.set(notificationKeys, lsSerializeTransfer.serialize(notifications));	
				// 未知站点数据
				XContentBuilder NoStationJsonBuilder = XContentFactory.jsonBuilder();
				NoStationJsonBuilder = NoStationJsonBuilder.startObject();
				for (int i = 0; i < weatherItems.length; i++) {
					NoStationJsonBuilder.field(fieldPrefix + i, weatherItems[i]);
				}
				NoStationJsonBuilder = NoStationJsonBuilder.endObject();
				createIndexResult = SearchIndexUtil.postWeatherData(NoStationJsonBuilder, CommonResources.INVALIDSTATIONTYPE, MappingType.INVALIDSTATION);
			}
			terminalhistory.setTableName(String.format("terminalhistory_%d_%d_%d_%d", terminalhistory.getTerminalCatetoryId(), terminalhistory.getWeatherStationId(), now.getYear(), now.getMonthOfYear()));
			terminalHistoryService.inserTerminalhistory(terminalhistory);
			if (logger.isInfoEnabled()) {
				logger.info(String.format("Create result is {}.", createIndexResult));
			}
		} catch (Exception exception) {
			logger.error("Exception in {}", exception);
		} finally {
			RedisPoolManager.close(client);
		}
	}

	private static String fileAttributes(final List<ParameterAttribute> parameterAttributes, final String key) {
		String attributeValue = null;
		for (ParameterAttribute parameterAttribute : parameterAttributes) {
			if (parameterAttribute.getName().equalsIgnoreCase(key)) {
				attributeValue = parameterAttribute.getValue();
				break;
			}
		}
		return attributeValue;
	}
	private void saveHeartbeatInfo(Jedis client,ClientInfo info,String weatherStationNumber) {
		// 心跳数据	
		boolean exists = false;	
		final Heartbeat heartbeat = new Heartbeat();
		heartbeat.setWeatherStationNumber(weatherStationNumber);
		if(StringUtils.isBlank(heartbeat.getWeatherStationNumber())){
			return;
		}
		heartbeat.setCreateDate(DateTime.now());
		// 保存IP地址
		
		ClientInfo clientInfo = clientInfoService.getClientInfoByWSNumber(weatherStationNumber);
		
		if(clientInfo != null && clientInfo.getClientId() > 0){
			clientInfo.setClientIP(info.getClientIP());
			clientInfo.setPort(info.getPort());
			clientInfoService.updateClientInfo(clientInfo);
		} else {
			info.setWeatherStationNumber(weatherStationNumber);
			clientInfoService.insertClientInfo(info);
		}

		// End Save
		List<Heartbeat> heartbeats = new ArrayList<>();
		byte[] key = CommonResources.HEARTBEAT.getBytes();
		if (client.exists(key)) {
			heartbeats = (List<Heartbeat>) lsSerializeTransfer.deserialize(client.get(key));
		}
		for (Heartbeat h : heartbeats) {
			if (h.getWeatherStationNumber().equalsIgnoreCase(heartbeat.getWeatherStationNumber())) {
				exists = true;
				h.setCreateDate(heartbeat.getCreateDate());
				break;
			}
		}
		if (!exists) {
			heartbeats.add(heartbeat);
		}
		client.set(key, lsSerializeTransfer.serialize(heartbeats));
	}
	
	/**
     * 读取txt文件的内容
     * @param file 想要读取的文件对象
     * @return 返回文件内容
     */
    private String txt2String(File file){
        StringBuilder result = new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
            String s = null;
            s = br.readLine();
            if(s != null){
            	result.append(s);
	            while((s = br.readLine())!=null){//使用readLine方法，一次读一行
	                result.append(System.lineSeparator()+s);
	            }
            }
            br.close();    
        }catch(Exception e){
            e.printStackTrace();
        }
        return result.toString();
    }
}
