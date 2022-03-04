package com.partners.weather.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.partners.annotation.UserAction;
import com.partners.annotation.UserPermission;
import com.partners.annotation.UserPermissionEnum;
import com.partners.entity.Adminuser;
import com.partners.entity.AggregationEntity;
import com.partners.entity.JsonResult;
import com.partners.entity.Terminalparameters;
import com.partners.entity.WeatherStationTerminal;
import com.partners.entity.Weatherstation;
import com.partners.entity.Weatherstationcategory;
import com.partners.view.entity.VAgg;
import com.partners.weather.common.CommonResources;
import com.partners.weather.redis.RedisPoolManager;
import com.partners.weather.request.RequestHelper;
import com.partners.weather.serialize.ObjectSerializeTransfer;
import com.partners.weather.service.IAggregationService;
import com.partners.weather.service.IWeatherStationService;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.vavr.Tuple;
import io.vavr.Tuple3;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Controller
@RequestMapping("/agg")
@UserPermission(value = UserPermissionEnum.AGGS)
@Slf4j
public class AggregationController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    @Autowired
    IAggregationService aggregationService;
    @Autowired
    IWeatherStationService weatherStationService;
    @Autowired
    JedisPool jedisPool;

    @RequestMapping
    @UserPermission(value = UserPermissionEnum.AGGS)
    @UserAction(Action = UserPermissionEnum.AGGS, Description = "数据统计")
    public String manage(HttpServletRequest request) {
        Adminuser adminuser = (Adminuser) request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
        List<Weatherstationcategory> weatherstationcategories = weatherStationService.getAllWeatherStationCategory();
        weatherstationcategories = weatherStationService.filterWeatherStationCategorys(weatherstationcategories, adminuser.getWeatherStationGroup(), false);
        List<Weatherstation> weatherstations = weatherStationService.getAllWeatherStation();
        weatherstations = weatherStationService.filterWeatherStation(weatherstations, adminuser.getWeatherStation(), false);
        request.setAttribute("Categories", weatherstationcategories);
        request.setAttribute("Stations", weatherstations);
        DateTime now = DateTime.now();
        request.setAttribute("StartDate", now.plusHours(-1).toString("yyyy-MM-dd HH:mm:00"));
        request.setAttribute("EndDate", now.toString("yyyy-MM-dd HH:mm:00"));
        return "aggregation";
    }

    @RequestMapping("/parameters")
    @UserPermission(value = UserPermissionEnum.AGGS)
    @UserAction(Action = UserPermissionEnum.AGGS, Description = "数据统计")
    @ResponseBody
    public Map<String, String> parameters(HttpServletRequest request) throws Exception {
        ObjectSerializeTransfer<Terminalparameters> objectSerializeTransfer = new ObjectSerializeTransfer<>();
        RedisPoolManager.Init(jedisPool);
        final Jedis client = RedisPoolManager.getJedis();
        final Map<String, String> paramMap = new HashMap<>();
        String station = request.getParameter("station");
        if (StringUtils.isBlank(station)) {
            return paramMap;
        }
        try {

            String[] stationArray = station.trim().split(",");
            if (stationArray.length < 1) {
                return paramMap;
            }
            List<Integer> stations = Arrays.stream(stationArray).map(p -> Integer.parseInt(p.trim())).collect(Collectors.toList());
            weatherStationService.getTerminalsByStationId(stations).forEach(
                    terminal -> {
                        byte[] params = client.get(String.valueOf(terminal.getTeminalParameterCategoryId()).getBytes());
                        if (ArrayUtils.isEmpty(params)) {
                            return;
                        }
                        ((Terminalparameters) objectSerializeTransfer.deserialize(params)).getTerminalParametersAttrs()
                                .stream().filter(tp -> !paramMap.containsKey(tp.getName()) && !"N".equalsIgnoreCase(tp.getShowInPage()))
                                .forEach(tp -> {
                                    paramMap.put(tp.getName(), tp.getDescription());
                                });
                    }
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            RedisPoolManager.close(client);
        }
        return paramMap;
    }

    @RequestMapping("/stations")
    @UserPermission(value = UserPermissionEnum.AGGS)
    @UserAction(Action = UserPermissionEnum.AGGS, Description = "数据统计")
    @ResponseBody
    public JsonResult stations(HttpServletRequest request) throws Exception {
        JsonResult jsonResult = buildJsonResult();
        Tuple3<VAgg, List<WeatherStationTerminal>, Map<String, String>> terminalDetaiTuple = this.getTerminalDetails(request, (t, u) -> {
        }, () -> StringUtils.EMPTY);
        List<AggregationEntity> aggregationEntities = aggregationService.getAggregationRecords(terminalDetaiTuple._1, terminalDetaiTuple._2, terminalDetaiTuple._3);
        int draw = StringUtils.isBlank(request.getParameter("draw")) ? 1 : Integer.valueOf(request.getParameter("draw"));
        jsonResult.setDraw(draw);
        jsonResult.setRecordsTotal(terminalDetaiTuple._1.getTotalRecords());
        jsonResult.setRecordsFiltered(terminalDetaiTuple._1.getTotalRecords());
        jsonResult.setData(aggregationEntities.toArray());
        return jsonResult;
    }

    @RequestMapping("/searchReport")
    @UserPermission(value = UserPermissionEnum.AGGS)
    @UserAction(Action = UserPermissionEnum.AGGS, Description = "数据统计")
    @ResponseBody
    public JsonResult searchReport(HttpServletRequest request) throws Exception {
        JsonResult jsonResult = buildJsonResult();
        Tuple3<VAgg, List<WeatherStationTerminal>, Map<String, String>> terminalDetaiTuple = this.getTerminalDetails(request, (t, u) -> {
            u.setParameters(t);
        }, () -> Lists.newArrayList("rainfall"));

        List<AggregationEntity> aggregationEntities = aggregationService.getAggregationRecords(terminalDetaiTuple._1, terminalDetaiTuple._2, terminalDetaiTuple._3);
        double rainfall = aggregationService.getAggRainfall(terminalDetaiTuple._1, terminalDetaiTuple._2.get(0));

        AggregationEntity aggRainfall = new AggregationEntity();
        aggRainfall.setTerminalName("rainfall");
        aggRainfall.setValue(rainfall);
        aggRainfall.setType("累积雨量");
        aggregationEntities.add(aggRainfall);

        int draw = StringUtils.isBlank(request.getParameter("draw")) ? 1 : Integer.valueOf(request.getParameter("draw"));
        jsonResult.setDraw(draw);
        jsonResult.setRecordsTotal(terminalDetaiTuple._1.getTotalRecords());
        jsonResult.setRecordsFiltered(terminalDetaiTuple._1.getTotalRecords());
        jsonResult.setData(aggregationEntities.toArray());
        return jsonResult;
    }

    @RequestMapping("/download")
    public void download(HttpServletRequest request, HttpServletResponse response) throws Exception {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        Tuple3<VAgg, List<WeatherStationTerminal>, Map<String, String>> terminalDetaiTuple = this.getTerminalDetails(request, (t, u) -> {
            u.setSize(t);
        }, () -> 10000);

        List<AggregationEntity> aggregationEntities = aggregationService.getAggregationRecords(terminalDetaiTuple._1, terminalDetaiTuple._2, terminalDetaiTuple._3);

        File downloadFile = new File(request.getSession().getServletContext().getRealPath("/resources/download/数据统计(" + System.currentTimeMillis() + ").csv"));
        try (FileWriter fw = new FileWriter(downloadFile)) {
            fw.write("\"站点名称\",\"站点编号\",\"要素名称\",\"统计类型\",\"最高/低值\",\"日期\"\r\n");
            for (AggregationEntity aggregationEntity : aggregationEntities) {
                fw.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",%s,\"%s\"\r\n", aggregationEntity.getWeatherStationName(), aggregationEntity.getWeatherStationNumber(), aggregationEntity.getTerminalDesc(),
                        aggregationEntity.getType(), nf.format(aggregationEntity.getValue()), aggregationEntity.getLastDate()));
            }
        }
        String mimeType = "application/octet-stream; charset=utf-8";
        response.setContentType(mimeType);
        String agent = request.getHeader("User-Agent");
        boolean isMSIE = (agent != null && agent.indexOf("MSIE") != -1);
        if (isMSIE) {
            response.setHeader("Content-Disposition", String.format("inline; filename=\"" + java.net.URLEncoder.encode(downloadFile.getName(), "utf-8") + "\""));
        } else {
            response.setHeader("Content-Disposition", String.format("inline; filename=\"" + new String(downloadFile.getName().getBytes("UTF-8"), "ISO-8859-1") + "\""));
        }
        response.setContentLength((int) downloadFile.length());
        try (FileInputStream fileInputStream = new FileInputStream(downloadFile);
             InputStream inputStream = new BufferedInputStream(fileInputStream)) {
            FileCopyUtils.copy(inputStream, response.getOutputStream());
            FileUtils.deleteQuietly(downloadFile);
        }
    }

    private <T> Tuple3<VAgg, List<WeatherStationTerminal>, Map<String, String>> getTerminalDetails(HttpServletRequest request, BiConsumer<T, VAgg> consumer, Supplier<T> supplier) {
        String station = request.getParameter("station");
        String parameter = request.getParameter("parameters");
        VAgg vAgg = new VAgg(RequestHelper.prepareRequest(request, true));
        vAgg.setStartDate(request.getParameter("startdate"));
        vAgg.setEndDate(request.getParameter("enddate"));
        try {
            DateTime.parse(vAgg.getStartDate(), DATE_TIME_FORMATTER);
            DateTime.parse(vAgg.getEndDate(), DATE_TIME_FORMATTER);
        } catch (Exception e) {
            return Tuple.of(null, null, null);
        }

        String[] stations = station.trim().split(",");
        if (ArrayUtils.isNotEmpty(stations)) {
            vAgg.setWeatherStations(Arrays.stream(stations).map(p -> Integer.parseInt(p.trim())).collect(Collectors.toList()));
        }

        String[] parameters = parameter.trim().split(",");
        if (parameters.length < 1) {
            return Tuple.of(null, null, null);
        }
        vAgg.setParameters(Lists.newArrayList(parameters));
        ObjectSerializeTransfer<Terminalparameters> objectSerializeTransfer = new ObjectSerializeTransfer<>();
        RedisPoolManager.Init(jedisPool);
        final Jedis client = RedisPoolManager.getJedis();
        List<WeatherStationTerminal> terminals = Lists.newArrayList();
        Map<String, String> paramMap = Maps.newHashMap();
        try {
            if (vAgg.getWeatherStations().size() > 0) {
                terminals = weatherStationService.getTerminalsByStationId(vAgg.getWeatherStations());
                weatherStationService.getTerminalsByStationId(vAgg.getWeatherStations()).forEach(
                        terminal -> {
                            byte[] params = client.get(String.valueOf(terminal.getTeminalParameterCategoryId()).getBytes());
                            if (ArrayUtils.isEmpty(params)) {
                                return;
                            }
                            ((Terminalparameters) objectSerializeTransfer.deserialize(params)).getTerminalParametersAttrs()
                                    .stream().filter(tp -> !paramMap.containsKey(tp.getName()) && !"N".equalsIgnoreCase(tp.getShowInPage()))
                                    .forEach(tp -> {
                                        paramMap.put(tp.getName(), tp.getDescription());
                                    });
                        }
                );
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            RedisPoolManager.close(client);
        }
        consumer.accept(supplier.get(), vAgg);
        return Tuple.of(vAgg, terminals, paramMap);
    }

    private JsonResult buildJsonResult() {
        JsonResult jsonResult = new JsonResult();
        jsonResult.setDraw(0);
        jsonResult.setRecordsTotal(0);
        jsonResult.setRecordsFiltered(0);
        return jsonResult;
    }

}