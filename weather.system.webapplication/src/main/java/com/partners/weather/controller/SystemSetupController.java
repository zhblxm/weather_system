package com.partners.weather.controller;

import com.partners.annotation.UserAction;
import com.partners.annotation.UserPermission;
import com.partners.annotation.UserPermissionEnum;
import com.partners.entity.SystemOption;
import com.partners.view.entity.ResponseMsg;
import com.partners.weather.common.CommonResources;
import com.partners.weather.common.RedisKey;
import com.partners.weather.job.DataAcquisitionTask;
import com.partners.weather.job.ImageSynTask;
import com.partners.weather.job.ScheduleFactory;
import com.partners.weather.job.TaskFactory;
import com.partners.weather.job.TerminalDateTimeTask;
import com.partners.weather.service.ISystemOptionService;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/system")
@UserPermission(value = UserPermissionEnum.SYSTEMSETUP)
@Slf4j
public class SystemSetupController {

    @Resource
    private ISystemOptionService systemOptionService;

    @RequestMapping
    @UserPermission(value = UserPermissionEnum.SYSTEMSETUP)
    @UserAction(Action = UserPermissionEnum.SYSTEMSETUP, Description = "系统设置")
    public String Manage(HttpServletRequest request) {
        String saveFilePathDir = request.getSession().getServletContext().getRealPath("/resources/images");
        File logoFile = new File(saveFilePathDir + "/logo_new.png");
        request.setAttribute("logo", "logo.png");
        request.setAttribute("backgroundimg", "category7jp1.jpg");
        if (logoFile.exists()) {
            request.setAttribute("logo", "logo_new.png");
        }
        File bgImageFile = new File(saveFilePathDir + "/background.png");
        if (bgImageFile.exists()) {
            request.setAttribute("backgroundimg", "background.png");
        }
        List<SystemOption> systemOptions = systemOptionService.getSystemOptions();
        SystemOption systemOption = systemOptionService.getSystemOption(systemOptions, CommonResources.AUTOREFRESHNUMBER);
        request.setAttribute("autorefreshnumber", systemOption == null ? "" : systemOption.getOptionValue());
        systemOption = systemOptionService.getSystemOption(systemOptions, CommonResources.AUTOREFRESHDATE);
        request.setAttribute("autorefreshdate", systemOption == null ? "" : systemOption.getOptionValue());
        systemOption = systemOptionService.getSystemOption(systemOptions, CommonResources.AUTOREFRESHDATETYPE);
        request.setAttribute("autorefreshdatetype", systemOption == null ? "h" : systemOption.getOptionValue());
        systemOption = systemOptionService.getSystemOption(systemOptions, RedisKey.LOGINMAINTITLE);
        request.setAttribute("loginMainTitle", systemOption == null ? "" : systemOption.getOptionValue());
        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd");
        request.setAttribute("now", (new DateTime()).toString(format));
        systemOption = systemOptionService.getSystemOption(systemOptions, CommonResources.TERMINALDATETASK);
        request.setAttribute("executedate", systemOption == null ? "" : systemOption.getOptionValue());
        systemOption = systemOptionService.getSystemOption(systemOptions, CommonResources.IMAGEMONITORPATH);
        request.setAttribute("imageMonitorPath", systemOption == null ? "" : systemOption.getOptionValue());
        return "systemsetup";
    }

    @RequestMapping("/upload")
    @UserPermission(value = UserPermissionEnum.SYSTEMSETUP)
    @UserAction(Action = UserPermissionEnum.SYSTEMSETUP, Description = "添加和更新系统设置")
    @ResponseBody
    @SneakyThrows
    public ResponseMsg upload(HttpServletRequest request) {
        ResponseMsg responseMsg = new ResponseMsg();
        responseMsg.setStatusCode(0);
        try {
            saveSystemOption(CommonResources.AUTOREFRESHNUMBER, "on".equalsIgnoreCase(request.getParameter("autonumber")) ? "Y" : "N");
            saveSystemOption(CommonResources.AUTOREFRESHDATE, "on".equalsIgnoreCase(request.getParameter("autodate")) ? "Y" : "N");
            DateTime now = DateTime.now();
            if ("on".equalsIgnoreCase(request.getParameter("autodate")) &&
                    !ScheduleFactory.checkExistsScheduler(CommonResources.TERMINALDATETASK)) {
                String trigger = String.format("0 %d %d %d %d *", now.getMinuteOfHour() + 2, now.getHourOfDay(), now.getDayOfMonth(), now.getMonthOfYear());
                ScheduleFactory.addSchedule(CommonResources.TERMINALDATETASK, new CronTrigger(trigger), new TerminalDateTimeTask());
            }
            saveSystemOption(CommonResources.AUTOREFRESHDATETYPE, request.getParameter("autorefreshdatetype"));
            if (!StringUtils.isBlank(request.getParameter("systemname"))) {
                saveSystemOption(CommonResources.AUTOREFRESHDATETYPE, request.getParameter("systemname"));
            } else {
                systemOptionService.delSystemOption(RedisKey.LOGINMAINTITLE);
            }
            String executedate = StringUtils.isBlank(request.getParameter("executedate")) ? "0" : request.getParameter("executedate");
            saveSystemOption(CommonResources.TERMINALDATETASK, executedate);
            saveSystemOption(CommonResources.IMAGEMONITORPATH, request.getParameter("imagepath"));
            // 自动补数
            if (ScheduleFactory.checkExistsScheduler(CommonResources.DATAACQUISITIONTASK)) {
                ScheduleFactory.cancelSchedule(CommonResources.DATAACQUISITIONTASK);
            }
            ScheduleFactory.addSchedule(CommonResources.DATAACQUISITIONTASK, new CronTrigger(String.format("0 0 %s * * ?", executedate)), new DataAcquisitionTask());
            CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
            if (multipartResolver.isMultipart(request)) {
                MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
                String basePath = request.getSession().getServletContext().getRealPath("/resources/images");
                multiRequest.getFileNames().forEachRemaining(name -> {
                    MultipartFile multipartFile = multiRequest.getFile(name);
                    File destinationFile = "backgroundfile".equalsIgnoreCase(name) ? new File(basePath, "/background.png") : new File(basePath, "/logo_new.png");
                    try {
                        FileUtils.writeByteArrayToFile(destinationFile, multipartFile.getBytes());
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                });
            }
            return responseMsg;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        responseMsg.setStatusCode(1);
        responseMsg.setMessage("系统设置保存失败，请联系管理员！");
        return responseMsg;
    }

    @RequestMapping("/singleupload/{type}")
    @UserPermission(value = UserPermissionEnum.SYSTEMSETUP)
    @UserAction(Action = UserPermissionEnum.SYSTEMSETUP, Description = "更新背景或Logo图片")
    @ResponseBody
    public ResponseMsg upload(@PathVariable("type") int type, HttpServletRequest request) {
        ResponseMsg responseMsg = new ResponseMsg();
        responseMsg.setStatusCode(0);
        try {
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            MultipartFile file = null;

            String filePath = "", basePath = request.getSession().getServletContext().getRealPath("/resources/images");
            switch (type) {
                case 0:
                    filePath = basePath + "/background.png";
                    file = multiRequest.getFile("backgroundfile");
                    break;
                case 1:
                    filePath = basePath + "/logo_new.png";
                    file = multiRequest.getFile("logofile");
                    break;
            }
            if (Objects.nonNull(file)) {
                FileUtils.writeByteArrayToFile(new File(filePath), file.getBytes());
            } else {
                responseMsg.setStatusCode(1);
                responseMsg.setMessage("系统设置保存失败，请联系管理员！");
            }
            return responseMsg;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        responseMsg.setStatusCode(1);
        responseMsg.setMessage("系统设置保存失败，请联系管理员！");
        return responseMsg;
    }

    @RequestMapping("/syncimages")
    @UserPermission(value = UserPermissionEnum.SYSTEMSETUP)
    @UserAction(Action = UserPermissionEnum.SYSTEMSETUP, Description = "同步监控图片")
    @ResponseBody
    public ResponseMsg syncImages(HttpServletRequest request) {
        ResponseMsg responseMsg = new ResponseMsg();
        responseMsg.setStatusCode(0);
        try {
            String imageFile = request.getParameter("imagepath");
            imageFile += (imageFile.endsWith(File.separator) ? File.separator : StringUtils.EMPTY);
            TaskFactory.getInstance().setImagePath(imageFile);

            new ImageSynTask().run();

            List<SystemOption> systemOptions = systemOptionService.getSystemOptions();
            SystemOption systemOption = systemOptionService.getSystemOption(systemOptions, CommonResources.IMAGEMONITORPATH);

            if (Objects.isNull(systemOption)) {
                TaskFactory.getInstance().setImagePath(StringUtils.EMPTY);
            } else {
                TaskFactory.getInstance().setImagePath(systemOption.getOptionValue() + (!systemOption.getOptionValue().endsWith(File.separator) ? File.separator : StringUtils.EMPTY));
            }
            return responseMsg;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        responseMsg.setStatusCode(1);
        responseMsg.setMessage("图片同步失败，请联系管理员！");
        return responseMsg;
    }

    private void saveSystemOption(String key, String value) {
        systemOptionService.addSystemOption(SystemOption.builder()
                .optionId(key)
                .optionValue(value)
                .build());
    }
}
