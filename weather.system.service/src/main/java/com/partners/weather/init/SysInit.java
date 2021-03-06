package com.partners.weather.init;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import javax.annotation.Resource;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.web.context.WebApplicationContext;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.partners.entity.RequestMessage;
import com.partners.entity.SystemOption;
import com.partners.entity.TerminalParamSettings;
import com.partners.entity.Terminalparameters;
import com.partners.entity.Terminalparameterscategory;
import com.partners.entity.WeatherStationTerminal;
import com.partners.entity.Weatherstation;
import com.partners.view.entity.ResponseMsg;
import com.partners.weather.common.CommonResources;
import com.partners.weather.common.MappingType;
import com.partners.weather.common.RedisKey;
import com.partners.weather.encrypt.HexUtil;
import com.partners.weather.job.DataAcquisitionTask;
import com.partners.weather.job.ImageSynTask;
import com.partners.weather.job.MonitorTask;
import com.partners.weather.job.ScheduleFactory;
import com.partners.weather.job.TaskFactory;
import com.partners.weather.protocol.ComponentManager;
import com.partners.weather.redis.RedisPoolManager;
import com.partners.weather.redis.RedisQueue;
import com.partners.weather.search.SearchIndexUtil;
import com.partners.weather.serialize.ObjectSerializeTransfer;
import com.partners.weather.service.IClientInfoService;
import com.partners.weather.service.IEmailSettingService;
import com.partners.weather.service.INotificationService;
import com.partners.weather.service.IScheduleHistoryService;
import com.partners.weather.service.ISystemOptionService;
import com.partners.weather.service.ITerminalHistoryService;
import com.partners.weather.service.ITerminalParamCategoryService;
import com.partners.weather.service.IWeatherStationService;
import com.partners.weather.xml.ParseXML;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class SysInit implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(SysInit.class);
    @Autowired
    private IWeatherStationService weatherStationService;
    @Resource
    private ITerminalParamCategoryService terminalParamCategoryService;
    @Autowired
    private JedisPool jedisPool;
    @Autowired
    private RedisQueue<RequestMessage> jedisQueue;
    @Autowired
    private IScheduleHistoryService historyService;
    @Autowired
    private ISystemOptionService systemOptionService;
    @Autowired
    private IClientInfoService clientInfoService;
    @Autowired
    private ITerminalHistoryService terminalHistoryService;
    @Autowired
    private INotificationService notificationService;
    @Autowired
    private IEmailSettingService emailSettingService;

    @SuppressWarnings("resource")
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Jedis client = null;
        try {
            if (Objects.isNull(event.getApplicationContext().getParent())) {
                TaskFactory.getInstance().setHistoryService(historyService);
                TaskFactory.getInstance().setSystemOptionService(systemOptionService);
                TaskFactory.getInstance().setClientInfoService(clientInfoService);
                TaskFactory.getInstance().setTerminalHistoryService(terminalHistoryService);
                TaskFactory.getInstance().setNotificationService(notificationService);
                TaskFactory.getInstance().setWeatherStationService(weatherStationService);
                TaskFactory.getInstance().setEmailSettingService(emailSettingService);
                ScheduleFactory.addSchedule(CommonResources.MONITORTASK, new CronTrigger("0 0/30 * * * ?"), new MonitorTask());
                //????????????
                SystemOption option = systemOptionService.getSystemOption(CommonResources.TERMINALDATETASK);
                if (option != null) {
                    String trigger = String.format("0 0 %s * * ?", option.getOptionValue());
                    ScheduleFactory.addSchedule(CommonResources.DATAACQUISITIONTASK, new CronTrigger(trigger), new DataAcquisitionTask());
                }
                //??????????????????
                option = systemOptionService.getSystemOption(CommonResources.IMAGEMONITORPATH);
                if (option != null) {
                    String imageFile = option.getOptionValue();
                    if (imageFile.endsWith(File.separator)) {
                        imageFile += File.separator;
                    }
                    TaskFactory.getInstance().setImagePath(imageFile);
                    File file = new File(SearchIndexUtil.class.getClassLoader().getResource("").toURI().getPath());

                    String compressPath = file.getParentFile().getParent() + File.separator + "resources" + File.separator + "images" + File.separator + "imagemonitor";
                    file = new File(compressPath);
                    if (!file.exists())
                        file.mkdir();
                    TaskFactory.getInstance().setCompressPath(compressPath);

                    ScheduleFactory.addSchedule(CommonResources.IMAGEMONITORTASK, new CronTrigger("0 0 2 * * ?"), new ImageSynTask());
                }
                String rootPath = SearchIndexUtil.class.getClassLoader().getResource("").toURI().getPath() + File.separator + "common.properties";
                File commonPropFile = new File(rootPath);
                if (commonPropFile.exists()) {
                    try (FileInputStream fileInputStreamStream = new FileInputStream(rootPath)) {
                        Properties propertyConfig = new Properties();
                        propertyConfig.load(fileInputStreamStream);
                        String imageUrl = propertyConfig.getProperty("map.image.url");
                        if (StringUtils.isNoneBlank(imageUrl)) {
                            CommonResources.MapImageHost = imageUrl;
                        }

                        String heartbeatOnline = propertyConfig.getProperty("heartbeat.online");
                        if (StringUtils.isNoneBlank(heartbeatOnline)) {
                            CommonResources.ONLINECOUNT = Integer.valueOf(heartbeatOnline);
                        }

                        String heartbeatOnOrOffLint = propertyConfig.getProperty("heartbeat.onOrOffLint");
                        if (StringUtils.isNoneBlank(heartbeatOnOrOffLint)) {
                            CommonResources.ONOROFFLINECOUNT = Integer.valueOf(heartbeatOnOrOffLint);
                        }
                    }
                }
                ApplicationContext applicationContext = event.getApplicationContext();
                WebApplicationContext webApplicationContext = (WebApplicationContext) applicationContext;
                String configPath = webApplicationContext.getServletContext().getRealPath("/resources/terminalconfig");
                File configFile = new File(configPath);
                if (configFile.exists() && configFile.isDirectory()) {
                    File[] configFiles = configFile.listFiles();
                    if (ArrayUtils.isNotEmpty(configFiles)) {
                        Terminalparameters terminalParams;
                        RedisPoolManager.Init(jedisPool);
                        client = RedisPoolManager.getJedis();
                        List<WeatherStationTerminal> weatherStationTerminals = weatherStationService.getWeatherStationTerminals();
                        ObjectSerializeTransfer<Terminalparameters> objectSerializeTransfer = new ObjectSerializeTransfer<>();
                        List<Weatherstation> weatherstations = weatherStationService.getAllWeatherStation();
                        if (client.exists(RedisKey.ALLSTATION.getBytes())) {
                            client.del(RedisKey.ALLSTATION.getBytes());
                        }
                        if (client.exists(RedisKey.ALLWEATHERSYSTEMKEY.getBytes())) {
                            client.del(RedisKey.ALLWEATHERSYSTEMKEY.getBytes());
                        }
                        if (weatherStationTerminals.size() > 0) {
                            Map<byte[], byte[]> weatherStationTerminalMap = new HashMap<>();
                            Map<Integer, Weatherstation> weaterStationMapTemp = new HashMap<>();
                            Map<byte[], byte[]> weatherStationMap = new HashMap<>();
                            if (weatherstations.size() > 0) {
                                weaterStationMapTemp = Maps.uniqueIndex(weatherstations, new Function<Weatherstation, Integer>() {
                                    @Override
                                    public Integer apply(Weatherstation input) {
                                        return input.getWeatherStationId();
                                    }
                                });
                            }
                            for (WeatherStationTerminal stationTerminal : weatherStationTerminals) {
                                if (weaterStationMapTemp.containsKey(stationTerminal.getWeatherStationId())) {
                                    weatherStationMap.put(stationTerminal.getTerminalModel().getBytes(), weaterStationMapTemp.get(stationTerminal.getWeatherStationId()).getWeatherStationNumber().getBytes());
                                }
                                weatherStationTerminalMap.put(stationTerminal.getTerminalModel().getBytes(), objectSerializeTransfer.serialize(stationTerminal));
                            }
                            client.hmset(RedisKey.ALLWEATHERSYSTEMKEY.getBytes(), weatherStationTerminalMap);
                            client.hmset(RedisKey.ALLSTATION.getBytes(), weatherStationMap);
                        }
                        ArrayList<Terminalparameterscategory> terminalparameterscategories = terminalParamCategoryService.getAllCategories();
                        if (client.exists(RedisKey.ALLCATETORY)) {
                            client.del(RedisKey.ALLCATETORY);
                        }
                        if (terminalparameterscategories.size() > 0) {
                            Map<String, String> terminalCategoryMap = new HashMap<>();
                            for (Terminalparameterscategory terminalparameterscategory : terminalparameterscategories) {
                                terminalCategoryMap.put(String.valueOf(HexUtil.HexToInt(terminalparameterscategory.getCategoryUniqueId())), terminalparameterscategory.getMappingName());
                            }
                            client.hmset(RedisKey.ALLCATETORY, terminalCategoryMap);
                        }
                        Terminalparameterscategory terminalparameterscategory;
                        List<TerminalParamSettings> settings = terminalParamCategoryService.getAllTerminalParamSettings();
                        int categoryId = 0;
                        for (File file : configFiles) {
                            terminalParams = ParseXML.parseXMLFile(file);
                            if (terminalParams == null) {
                                throw new RuntimeException("???????????????????????????????????????????????????????????????????????????");
                            }
                            terminalparameterscategory = terminalParamCategoryService.getTerminalparamCategoryByName(terminalParams.getName());
                            if(Objects.isNull(terminalparameterscategory)){
                                categoryId = HexUtil.HexToInt(terminalparameterscategory.getCategoryUniqueId());
                                terminalParamCategoryService.setTerminalParameterValue(categoryId, terminalParams, settings);
                            }
                            else{
                                terminalparameterscategory = new Terminalparameterscategory();
                                terminalparameterscategory.setIsSystem(1);
                                terminalparameterscategory.setTerminalParamCategoryName(terminalParams.getName());
                                terminalparameterscategory.setMappingName(file.getName().replaceAll("\\s*", "").replaceAll("[.][^.]+$", ""));
                                terminalparameterscategory.setCreateUser("System User");
                                ResponseMsg responseMsg = terminalParamCategoryService.insertTerminalParamCategory(terminalparameterscategory);
                                if (responseMsg.getStatusCode() >= 0) {
                                    categoryId = (int) responseMsg.getMessageObject();
                                    terminalParamCategoryService.batchInsertTerminalParamSettings(categoryId, terminalParams);
                                }
                            }

                            client.set(String.valueOf(categoryId).getBytes(), objectSerializeTransfer.serialize(terminalParams));
                            if (SearchIndexUtil.createIndex(MappingType.INDEX)) {
                                SearchIndexUtil.createMapping(categoryId, terminalParams, MappingType.INDEX);
                            }
                            if (SearchIndexUtil.createIndex(MappingType.INVALIDINDEX)) {
                                Terminalparameters invalidTerminalParams = ParseXML.gererateInvalidMappingParameters(terminalParams);
                                SearchIndexUtil.createMapping(categoryId, invalidTerminalParams, MappingType.INVALIDINDEX);
                            }
                            SearchIndexUtil.createIndex(MappingType.INVALIDSTATION);
                        }
                    }
                }
                List<Integer> ports = weatherStationService.getAllPorts();
                if (ports.size() > 0) {
                    ComponentManager manager = ComponentManager.getInstance();
                    manager.setJedisQueue(jedisQueue);
                    manager.initialize(ports);
                }
            }
        } catch (Exception exception) {
            logger.error("Error in {}", exception);
        } finally {
            RedisPoolManager.close(client);
        }
    }
}
