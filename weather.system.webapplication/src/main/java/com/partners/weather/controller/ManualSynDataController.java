package com.partners.weather.controller;

import com.partners.annotation.UserAction;
import com.partners.annotation.UserPermission;
import com.partners.annotation.UserPermissionEnum;
import com.partners.entity.Adminuser;
import com.partners.entity.Weatherstation;
import com.partners.entity.Weatherstationcategory;
import com.partners.weather.common.CommonResources;
import com.partners.weather.job.ManualSynDataTask;
import com.partners.weather.job.ScheduleFactory;
import com.partners.weather.service.IWeatherStationService;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/manualsyndata")
@UserPermission(value = UserPermissionEnum.MANUALSYNDATA)
@Slf4j
public class ManualSynDataController {
    @Autowired
    IWeatherStationService weatherStationService;

    @RequestMapping
    @UserPermission(value = UserPermissionEnum.SELECT)
    @UserAction(Action = UserPermissionEnum.SELECT, Description = "数据查询")
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
        return "manualsyndata";
    }

    @RequestMapping("/syndata")
    @UserPermission(value = UserPermissionEnum.SELECT)
    @UserAction(Action = UserPermissionEnum.SELECT, Description = "手动同步数据")
    @ResponseBody
    public String syndata(HttpServletRequest request) throws Exception {
        try {
            String station = request.getParameter("station");
            List<Integer> stations = Stream.of(station.trim().split(",")).map(p -> Integer.parseInt(p.trim())).collect(Collectors.toList());
            DateTime now = DateTime.now().plusSeconds(2);
            String trigger = String.format("%d %d %d %d %d *", now.getSecondOfMinute(), now.getMinuteOfHour(), now.getHourOfDay(), now.getDayOfMonth(), now.getMonthOfYear());
            ScheduleFactory.addSchedule(CommonResources.TERMINALDATETASK, new CronTrigger(trigger), new ManualSynDataTask(request.getParameter("startdate"), request.getParameter("enddate"), stations));
            return "1";
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return "0";
    }
}
