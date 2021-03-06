package com.partners.weather.controller;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;

import com.partners.annotation.UserAction;
import com.partners.annotation.UserPermission;
import com.partners.annotation.UserPermissionEnum;
import com.partners.entity.Adminuser;
import com.partners.entity.Heartbeat;
import com.partners.entity.MonitorBatter;
import com.partners.entity.WeatherStationTerminal;
import com.partners.entity.Weatherstation;
import com.partners.entity.Weatherstationcategory;
import com.partners.view.entity.ResponseMsg;
import com.partners.weather.common.CommonResources;
import com.partners.weather.encrypt.HexUtil;
import com.partners.weather.redis.RedisPoolManager;
import com.partners.weather.serialize.ListSerializeTransfer;
import com.partners.weather.service.IMonitorService;
import com.partners.weather.service.IWeatherStationService;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@UserPermission(value = UserPermissionEnum.MONITOR)
@Controller
@RequestMapping("/monitor")
@Scope("prototype")
public class MonitorController {
    @Autowired
    IWeatherStationService weatherStationService;
    @Autowired
    IMonitorService monitorService;
    @Autowired
    JedisPool jedisPool;

    @RequestMapping
    @UserPermission(value = UserPermissionEnum.MONITOR)
    @UserAction(Action = UserPermissionEnum.MONITOR, Description = "????????????")
    public String Index(HttpServletRequest request) {
        ListSerializeTransfer<Heartbeat> lsSerializeTransfer = new ListSerializeTransfer<Heartbeat>();
        RedisPoolManager.Init(jedisPool);
        Jedis client = RedisPoolManager.getJedis();

        request.setAttribute("MapImgHost", CommonResources.MapImageHost);
        Adminuser adminuser = (Adminuser) request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
        List<Weatherstationcategory> weatherstationcategories = weatherStationService.getAllWeatherStationCategory();
        weatherstationcategories = weatherStationService.filterWeatherStationCategorys(weatherstationcategories, adminuser.getWeatherStationGroup(), false);
        List<Weatherstation> weatherstations = weatherStationService.getAllWeatherStation();
        weatherstations = weatherStationService.filterWeatherStation(weatherstations, adminuser.getWeatherStation(), false);
        String coverFileBasePath = request.getSession().getServletContext().getRealPath("/resources/cover");
        Map<Integer, Double> monitorBatterMap = new HashMap<>();
        final DateTime now = DateTime.now();
        try {
            byte[] monitorKey = CommonResources.MONITORBATTERY.getBytes();
            if (client.exists(monitorKey)) {
                ((List<MonitorBatter>) lsSerializeTransfer.deserialize(client.get(monitorKey))).forEach(m -> {
                    monitorBatterMap.put(m.getStationId(), m.getBatterPercent());
                });
            }
        } finally {
            RedisPoolManager.close(client);
        }
        NumberFormat nf = NumberFormat.getPercentInstance();
        nf.setMinimumFractionDigits(0);
        for (Weatherstation weatherstation : weatherstations) {
            weatherstation.setCover("");
            weatherstation.setUniqueWeatherStationId(HexUtil.IntToHex(weatherstation.getWeatherStationId()));
            if ((new File(coverFileBasePath + "/" + weatherstation.getWeatherStationId() + ".png")).exists()) {
                weatherstation.setCover(weatherstation.getWeatherStationId() + ".png");
            }
            if (monitorBatterMap.containsKey(weatherstation.getWeatherStationId())) {
                weatherstation.setVoltage(monitorBatterMap.get(weatherstation.getWeatherStationId()));
                weatherstation.setVoltagePercent(nf.format(weatherstation.getVoltage() / 100));
            }
        }
        Map<Integer, Weatherstationcategory> categoryMap = Maps.uniqueIndex(weatherstationcategories, Weatherstationcategory::getWeatherStationCategoryId);
        List<Integer> stationList = weatherstations.stream().map(Weatherstation::getWeatherStationCategoryId).distinct().collect(Collectors.toList());
        weatherstationcategories.addAll(
                categoryMap.entrySet().stream().filter(iter -> stationList.contains(iter.getKey())).map(Map.Entry::getValue).collect(Collectors.toList())
        );
        request.setAttribute("Categories", weatherstationcategories);
        request.setAttribute("Stations", weatherstations);
        List<Heartbeat> heartbeats = weatherStationService.getHeartbeats();
        Collection<Heartbeat> heartbeatFilterList = Collections2.filter(heartbeats, heartbeat -> !StringUtils.isBlank(heartbeat.getWeatherStationNumber())
				&& now.compareTo(heartbeat.getCreateDate().plusMinutes(CommonResources.ONLINECOUNT)) <= 0);
        int onlineCount = heartbeatFilterList.size();
        heartbeatFilterList = Collections2.filter(heartbeats, heartbeat -> StringUtils.isBlank(heartbeat.getWeatherStationNumber())
				&& now.compareTo(heartbeat.getCreateDate().plusMinutes(CommonResources.ONLINECOUNT)) > 0
				&& now.compareTo(heartbeat.getCreateDate().plusMinutes(CommonResources.ONOROFFLINECOUNT)) <= 0);
        int onlineOrOffLineCount = heartbeatFilterList.size();
        long lastHourCount = monitorService.getLastHourWeathers();
        List<WeatherStationTerminal> terminals = weatherStationService.getWeatherStationTerminals();
        DateTime start = now.plusDays(-1), end = now;
        Period period;
        long frequencyCount = 0;
        for (WeatherStationTerminal weatherStationTerminal : terminals) {
            switch (weatherStationTerminal.getAcquisitionFrequencyUnit()) {
                case "h":
                    period = new Period(start, end, PeriodType.hours());
                    frequencyCount += period.getHours() / weatherStationTerminal.getAcquisitionFrequency();
                    break;
                default:
                    period = new Period(start, end, PeriodType.minutes());
                    frequencyCount += period.getMinutes() / weatherStationTerminal.getAcquisitionFrequency();
                    break;
            }
        }
        frequencyCount = lastHourCount > frequencyCount ? lastHourCount : frequencyCount;
        if (frequencyCount == 0) frequencyCount = 1;
        request.setAttribute("Online", onlineCount);
        request.setAttribute("OnOrOffline", onlineOrOffLineCount);
        int terminalCount = terminals.size();
        request.setAttribute("Terminals", terminalCount);
        request.setAttribute("NetworkTransferPercent", nf.format((double) onlineCount / (double) (terminalCount == 0 ? 1 : terminalCount)));
        request.setAttribute("NetworkDataTransferPercent", nf.format(lastHourCount / frequencyCount));
        return "monitor";
    }

    @RequestMapping("/station/{uniqueWeatherStationId}")
    @UserPermission(value = UserPermissionEnum.MONITOR)
    @UserAction(Action = UserPermissionEnum.MONITOR, Description = "????????????")
    @ResponseBody
    public ResponseMsg stations(HttpServletRequest request, @PathVariable("uniqueWeatherStationId") String uniqueWeatherStationId) {
        ResponseMsg responseMsg = new ResponseMsg();
        responseMsg.setStatusCode(0);
        int stationId = HexUtil.HexToInt(uniqueWeatherStationId);
        Weatherstation weatherstation = weatherStationService.getWeatherStation(stationId);
        if (weatherstation == null) {
            responseMsg.setStatusCode(1);
            responseMsg.setMessage("??????????????????");
            return responseMsg;
        }
        ArrayList<WeatherStationTerminal> weatherStationTerminals = weatherStationService.getWeatherStationTerminalById(stationId);
        if (weatherStationTerminals.size() == 0) {
            responseMsg.setStatusCode(1);
            responseMsg.setMessage("??????????????????????????????????????????????????????");
            return responseMsg;
        }
        Map<String, String> termialParameterMap = monitorService.getLastWeatherDetail(stationId, weatherStationTerminals);
        Map<String, String> termialParameterFilterMap = Maps.filterKeys(termialParameterMap, new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return !input.contains("fulldate");
            }
        });
        if (termialParameterMap.containsKey("fulldate")) {
            responseMsg.setMessage(termialParameterMap.get("fulldate"));
        }
        responseMsg.setMessageObject(termialParameterFilterMap);
        return responseMsg;
    }
}
