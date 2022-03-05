package com.partners.weather.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.partners.annotation.UserAction;
import com.partners.annotation.UserPermission;
import com.partners.annotation.UserPermissionEnum;
import com.partners.entity.Adminuser;
import com.partners.entity.Weatherstation;
import com.partners.entity.Weatherstationcategory;
import com.partners.view.entity.VImageMonitor;
import com.partners.weather.common.CommonResources;
import com.partners.weather.service.IImageMonitorService;
import com.partners.weather.service.IWeatherStationService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/imagemonitor")
@UserPermission(value = UserPermissionEnum.ANALYSIS)
public class ImageMonitorController {
    @Autowired
    IWeatherStationService weatherStationService;
    @Autowired
    IImageMonitorService imageMonitorService;

    @RequestMapping
    @UserPermission(value = UserPermissionEnum.IMAGEMONITOR)
    @UserAction(Action = UserPermissionEnum.IMAGEMONITOR, Description = "图像监控")
    public String Index(HttpServletRequest request) {
        Adminuser adminuser = (Adminuser) request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
        List<Weatherstationcategory> weatherstationcategories = weatherStationService.getAllWeatherStationCategory();
        weatherstationcategories = weatherStationService.filterWeatherStationCategorys(weatherstationcategories, adminuser.getWeatherStationGroup(), false);

        List<Weatherstation> weatherstations = weatherStationService.getAllWeatherStation();
        weatherstations = weatherStationService.filterWeatherStation(weatherstations, adminuser.getWeatherStation(), false);

        Map<Integer, Weatherstationcategory> categoryMap = Maps.uniqueIndex(weatherstationcategories, Weatherstationcategory::getWeatherStationCategoryId);

        List<Integer> stationList = weatherstations.stream().map(Weatherstation::getWeatherStationCategoryId).distinct().collect(Collectors.toList());

        weatherstationcategories.clear();
        weatherstationcategories.addAll(
                categoryMap.entrySet().stream().filter(item -> stationList.contains(item.getKey()))
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toList())
        );
        request.setAttribute("Categories", weatherstationcategories);
        request.setAttribute("Stations", weatherstations);
        return "imagemonitor";
    }

    @RequestMapping("/getimage")
    @UserPermission(value = UserPermissionEnum.SELECT)
    @UserAction(Action = UserPermissionEnum.SELECT, Description = "数据查询")
    @ResponseBody
    public List<VImageMonitor> getImageInfo(HttpServletRequest request) throws Exception {
        List<VImageMonitor> imageMonitors;
        String wsNumber = request.getParameter("wsNumber");
        String wsCategoryId = request.getParameter("wsCategoryId");

        List<String> stationNumbers = null;
        List<Integer> categories = new ArrayList<>(Integer.parseInt(wsCategoryId));
        List<Weatherstation> weatherstations = weatherStationService.getWeatherStationsByCategory(categories);

        if (!StringUtils.isBlank(wsNumber)) {
            stationNumbers = Lists.newArrayList(wsNumber);
        } else if (!StringUtils.isBlank(wsCategoryId)) {
            stationNumbers = weatherstations.stream().map(Weatherstation::getWeatherStationNumber).collect(Collectors.toList());
        }
        Map<String, Weatherstation> stationByNumberMap = Maps.uniqueIndex(weatherstations, Weatherstation::getWeatherStationNumber);

        String imagePath = request.getSession().getServletContext().getRealPath("/resources/images/");
        imageMonitors = imageMonitorService.getImageMonitors(stationNumbers, imagePath);


        for (VImageMonitor item : imageMonitors) {
            Weatherstation station = stationByNumberMap.get(item.getWeatherStationNumber());
            if (Objects.isNull(station)) {
                continue;
            }
            item.setWeatherStationCategoryId(station.getWeatherStationCategoryId());
            item.setWeatherStationId(station.getWeatherStationId());
            item.setWeatherStationName(station.getWeatherStationName());
        }

        return imageMonitors;
    }
}
