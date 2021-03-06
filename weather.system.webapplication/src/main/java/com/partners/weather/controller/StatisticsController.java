package com.partners.weather.controller;

import com.partners.annotation.UserAction;
import com.partners.annotation.UserPermission;
import com.partners.annotation.UserPermissionEnum;
import com.partners.entity.Adminuser;
import com.partners.entity.JsonResult;
import com.partners.entity.Statistics;
import com.partners.entity.Weatherstation;
import com.partners.entity.Weatherstationcategory;
import com.partners.view.entity.VStatistics;
import com.partners.weather.common.CommonResources;
import com.partners.weather.request.RequestHelper;
import com.partners.weather.service.IWeatherStationService;
import com.partners.weather.service.IWeatherStatisticsService;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/statistics")
@UserPermission(value = UserPermissionEnum.STATISTICS)
@Slf4j
public class StatisticsController {
    private DateTimeFormatter DATETIMEFORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    @Autowired
    IWeatherStatisticsService statisticsService;
    @Autowired
    IWeatherStationService weatherStationService;

    @RequestMapping
    @UserPermission(value = UserPermissionEnum.STATISTICS)
    @UserAction(Action = UserPermissionEnum.STATISTICS, Description = "数据统计查询")
    public String manage(HttpServletRequest request) {
        Adminuser adminuser = (Adminuser) request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
        List<Weatherstationcategory> weatherstationcategories = weatherStationService.getAllWeatherStationCategory();
        weatherstationcategories = weatherStationService.filterWeatherStationCategorys(weatherstationcategories, adminuser.getWeatherStationGroup(), false);
        List<Weatherstation> weatherstations = weatherStationService.getAllWeatherStation();
        weatherstations = weatherStationService.filterWeatherStation(weatherstations, adminuser.getWeatherStation(), false);
        request.setAttribute("Categories", weatherstationcategories);
        request.setAttribute("Stations", weatherstations);
        request.setAttribute("StartDate", new DateTime().toString("yyyy-MM-dd HH:mm:ss"));
        request.setAttribute("EndDate", new DateTime().plusMonths(1).toString("yyyy-MM-dd HH:mm:ss"));
        return "statistics";
    }

    @RequestMapping("/stations")
    @UserPermission(value = UserPermissionEnum.STATISTICS)
    @UserAction(Action = UserPermissionEnum.STATISTICS, Description = "数据统计查询")
    @ResponseBody
    public JsonResult stations(HttpServletRequest request) throws Exception {
        JsonResult.JsonResultBuilder jsonResult = JsonResult.builder().draw(0).recordsFiltered(0)
                .recordsFiltered(0).data(new Object[0]);
        String station = request.getParameter("station");
        if (StringUtils.isBlank(station)) {
            return jsonResult.build();
        }
        VStatistics statistics = new VStatistics(RequestHelper.prepareRequest(request, true));
        statistics.setStartDate(request.getParameter("startdate"));
        statistics.setEndDate(request.getParameter("enddate"));
        statistics.setWeatherStations(Arrays.stream(station.trim().split(",")).map(p->Integer.parseInt(p.trim())).collect(Collectors.toList()));
        List<Statistics> weatherStations = statisticsService.getStatistics(statistics);
        return jsonResult.draw(StringUtils.isBlank(request.getParameter("draw")) ? 1 : Integer.parseInt(request.getParameter("draw")))
                .recordsTotal(statistics.getWeatherStations().size())
                .recordsFiltered(statistics.getWeatherStations().size())
                .data(weatherStations.toArray()).build();
    }

    @RequestMapping("/download")
    public void download(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String station = request.getParameter("station");
        if (StringUtils.isBlank(station)) {
            return;
        }
        VStatistics statistics = new VStatistics(RequestHelper.prepareRequest(request, true));
        statistics.setWeatherStations(Arrays.stream(station.trim().split(",")).map(p->Integer.parseInt(p.trim())).collect(Collectors.toList()));
        statistics.setSize(10000);
        List<Statistics> weatherStations = statisticsService.getStatistics(statistics);
        File downloadFile = new File(request.getSession().getServletContext().getRealPath("/resources/download/及逾时统计(" + System.currentTimeMillis() + ").csv"));
        try (FileWriter fw = new FileWriter(downloadFile)) {
            fw.write("\"站点名称\",\"站点编号\",\"所属分类\",\"及时数\",\"及时率\",\"逾时数\",\"逾时率\",\"缺报数\",\"缺报率\",\"总数\"\r\n");
            for (Statistics statistic : weatherStations) {
                fw.write(String.format("\"%s\",\"%s\",\"%s\",%d,\"%s\",%d,\"%s\",%d,\"%s\",%d\r\n", statistic.getWeatherStationName(), statistic.getWeatherStationNumber(), statistic.getWeatherStationCategoryName(),
                        statistic.getOnTimeCount(), statistic.getOnTimePercent(), statistic.getDelayedTimeCount(), statistic.getDelayedTimePercent(), statistic.getLoseCount(), statistic.getLosePercent(), statistic.getTotalCount()));
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
