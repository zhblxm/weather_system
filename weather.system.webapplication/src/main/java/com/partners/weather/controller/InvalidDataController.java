package com.partners.weather.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.partners.annotation.UserAction;
import com.partners.annotation.UserPermission;
import com.partners.annotation.UserPermissionEnum;
import com.partners.entity.Adminuser;
import com.partners.entity.Invalid;
import com.partners.entity.JsonResult;
import com.partners.entity.Terminalparameters;
import com.partners.entity.Weatherstation;
import com.partners.entity.Weatherstationcategory;
import com.partners.view.entity.VInvalid;
import com.partners.weather.common.CommonResources;
import com.partners.weather.redis.RedisPoolManager;
import com.partners.weather.request.RequestHelper;
import com.partners.weather.serialize.ObjectSerializeTransfer;
import com.partners.weather.service.IStationInvalidService;
import com.partners.weather.service.IWeatherStationService;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Controller
@RequestMapping("/invalid")
@UserPermission(value = UserPermissionEnum.EXCEPTIONDATA)
@Slf4j
public class InvalidDataController {
    @Autowired
    IStationInvalidService stationInvalidService;
    @Autowired
    IWeatherStationService weatherStationService;
    @Autowired
    JedisPool jedisPool;

    @RequestMapping
    @UserPermission(value = UserPermissionEnum.EXCEPTIONDATA)
    @UserAction(Action = UserPermissionEnum.EXCEPTIONDATA, Description = "异常数据查询")
    public String manage(HttpServletRequest request) {
        Adminuser adminuser = (Adminuser) request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
        List<Weatherstationcategory> weatherstationcategories = weatherStationService.getAllWeatherStationCategory();
        weatherstationcategories = weatherStationService.filterWeatherStationCategorys(weatherstationcategories, adminuser.getWeatherStationGroup(), false);
        List<Weatherstation> weatherstations = weatherStationService.getAllWeatherStation();
        weatherstations = weatherStationService.filterWeatherStation(weatherstations, adminuser.getWeatherStation(), false);
        request.setAttribute("Categories", weatherstationcategories);
        request.setAttribute("Stations", weatherstations);
        request.setAttribute("StartDate", new DateTime().toString("yyyy-MM-dd HH:00:00"));
        request.setAttribute("EndDate", new DateTime().plusMonths(1).toString("yyyy-MM-dd HH:00:00"));
        return "invalidpage";
    }

    @RequestMapping("/parameters")
    @UserPermission(value = UserPermissionEnum.EXCEPTIONDATA)
    @UserAction(Action = UserPermissionEnum.EXCEPTIONDATA, Description = "异常数据查询")
    @ResponseBody
    public Map<String, String> parameters(HttpServletRequest request) throws Exception {
        Map<String, String> paramMap = Maps.newHashMap();
        String station = request.getParameter("station");
        if (StringUtils.isBlank(station)) {
            return paramMap;
        }
        String[] stationArray = station.trim().split(",");
        if (ArrayUtils.isEmpty(stationArray)) {
            return paramMap;
        }
        ObjectSerializeTransfer<Terminalparameters> objectSerializeTransfer = new ObjectSerializeTransfer<>();
        RedisPoolManager.Init(jedisPool);
        Jedis client = RedisPoolManager.getJedis();
        try {
            weatherStationService.getTerminalsByStationId(
                    Stream.of(stationArray).map(e -> Integer.parseInt(e.trim())).collect(Collectors.toList())
            ).forEach(terminal -> {
                byte[] params = client.get(String.valueOf(terminal.getTeminalParameterCategoryId()).getBytes());
                if (ArrayUtils.isEmpty(params)) {
                    return;
                }
                ((Terminalparameters) objectSerializeTransfer.deserialize(params)).getTerminalParametersAttrs()
                        .stream().filter(tp -> !paramMap.containsKey(tp.getName()) && !"N".equalsIgnoreCase(tp.getShowInPage()))
                        .forEach(tp -> {
                            paramMap.put(tp.getName(), tp.getDescription());
                        });
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            RedisPoolManager.close(client);
        }
        return paramMap;
    }

    @RequestMapping("/stations")
    @UserPermission(value = UserPermissionEnum.EXCEPTIONDATA)
    @UserAction(Action = UserPermissionEnum.EXCEPTIONDATA, Description = "异常数据查询")
    @ResponseBody
    public JsonResult stations(HttpServletRequest request) throws Exception {
        JsonResult jsonResult = new JsonResult();
        jsonResult.setDraw(0);
        jsonResult.setRecordsTotal(0);
        jsonResult.setRecordsFiltered(0);
        List<Invalid> invalids = new ArrayList<>(0);
        jsonResult.setData(invalids.toArray());
        String station = request.getParameter("station");
        String parameter = request.getParameter("parameters");
        if (StringUtils.isBlank(station) || StringUtils.isBlank(parameter)) {
            return jsonResult;
        }
        VInvalid vInvalid = new VInvalid(RequestHelper.prepareRequest(request, true));
        try {
            vInvalid.setStartDate(request.getParameter("startdate"));
            vInvalid.setEndDate(request.getParameter("enddate"));
            DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            DateTime.parse(vInvalid.getStartDate(), format);
            DateTime.parse(vInvalid.getEndDate(), format);
        } catch (Exception e) {
            return jsonResult;
        }
        String[] stations = station.trim().split(",");
        if (stations.length > 0) {
            List<Integer> stationList = new ArrayList<>(stations.length);
            for (String p : stations) {
                stationList.add(Integer.parseInt(p.trim()));
            }
            vInvalid.setWeatherStations(stationList);
        }
        String[] parameters = parameter.trim().split(",");
        if (parameters.length > 0) {
            vInvalid.setParameters(Lists.newArrayList(parameters));
        }
        invalids = stationInvalidService.getInvalidRecords(vInvalid);
        int draw = StringUtils.isBlank(request.getParameter("draw")) ? 1 : Integer.valueOf(request.getParameter("draw"));
        jsonResult.setDraw(draw);
        jsonResult.setRecordsTotal(vInvalid.getTotalRecords());
        jsonResult.setRecordsFiltered(vInvalid.getTotalRecords());
        jsonResult.setData(invalids.toArray());
        return jsonResult;
    }

    @RequestMapping("/download")
    public void download(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String station = request.getParameter("station");
        String parameter = request.getParameter("parameters");
        if (StringUtils.isBlank(station) || StringUtils.isBlank(parameter)) {
            return;
        }
        VInvalid vInvalid = new VInvalid(RequestHelper.prepareRequest(request, true));
        vInvalid.setSize(10000);
        try {
            DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            DateTime.parse(request.getParameter("startdate"), format);
            DateTime.parse(request.getParameter("enddate"), format);
        } catch (Exception e) {
            return;
        }
        String[] stations = station.trim().split(",");
        if (stations.length > 0) {
            List<Integer> stationList = new ArrayList<>(stations.length);
            for (String p : stations) {
                stationList.add(Integer.parseInt(p.trim()));
            }
            vInvalid.setWeatherStations(stationList);
        }
        String[] parameters = parameter.trim().split(",");
        if (parameters.length > 0) {
            vInvalid.setParameters(Lists.newArrayList(parameters));
        }
        List<Invalid> invalids = stationInvalidService.getInvalidRecords(vInvalid);
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        File downloadFile = new File(request.getSession().getServletContext().getRealPath("/resources/download/异常数据(" + System.currentTimeMillis() + ").csv"));
        try (FileWriter fw = new FileWriter(downloadFile)) {
            fw.write("\"站点名称\",\"站点编号\",\"要素名称\",\"最小值\",\"收到数据	\",\"最大值\",\"日期\"\r\n");
            for (Invalid invalid : invalids) {
                fw.write(String.format("\"%s\",\"%s\",\"%s\",%s,%s,%s,\"%s\"\r\n", invalid.getWeatherStationName(), invalid.getWeatherStationNumber(), invalid.getInvalidFieldDesc(),
                        nf.format(invalid.getMinValue()), nf.format(invalid.getValue()), nf.format(invalid.getMaxValue()), invalid.getCollectionDate()));
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
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(downloadFile))) {
            FileCopyUtils.copy(inputStream, response.getOutputStream());
            downloadFile.delete();
        }
    }
}
