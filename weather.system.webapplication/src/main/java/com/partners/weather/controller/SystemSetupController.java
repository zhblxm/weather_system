package com.partners.weather.controller;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

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

@Controller
@RequestMapping("/system")
@UserPermission(value = UserPermissionEnum.SYSTEMSETUP)
public class SystemSetupController {
	private static final Logger logger = LoggerFactory.getLogger(SystemSetupController.class);
	@Resource
	ISystemOptionService systemOptionService;

	/**
	 * @param request
	 * @return
	 */
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
	public ResponseMsg upload(HttpServletRequest request) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		try {
			String autonumber = request.getParameter("autonumber");
			SystemOption systemOption = new SystemOption();
			systemOption.setOptionId(CommonResources.AUTOREFRESHNUMBER);
			systemOption.setOptionValue("on".equalsIgnoreCase(autonumber) ? "Y" : "N");
			systemOptionService.addSystemOption(systemOption);
			String autodate = request.getParameter("autodate");
			systemOption = new SystemOption();
			systemOption.setOptionId(CommonResources.AUTOREFRESHDATE);
			systemOption.setOptionValue("on".equalsIgnoreCase(autodate) ? "Y" : "N");
			systemOptionService.addSystemOption(systemOption);
			DateTime now = new DateTime();
			String trigger = "";
			if ("Y".equalsIgnoreCase(systemOption.getOptionValue())) {
				if (!ScheduleFactory.checkExistsScheduler(CommonResources.TERMINALDATETASK)) {
					trigger = String.format("0 %d %d %d %d *", now.getMinuteOfHour() + 2, now.getHourOfDay(), now.getDayOfMonth(), now.getMonthOfYear());
					ScheduleFactory.addSchedule(CommonResources.TERMINALDATETASK, new CronTrigger(trigger), new TerminalDateTimeTask());
				}
			}
			String autorefreshdatetype = request.getParameter("autorefreshdatetype");
			systemOption = new SystemOption();
			systemOption.setOptionId(CommonResources.AUTOREFRESHDATETYPE);
			systemOption.setOptionValue(autorefreshdatetype);
			systemOptionService.addSystemOption(systemOption);
			String systemname = request.getParameter("systemname");
			if (!StringUtils.isBlank(systemname)) {
				systemOption = new SystemOption();
				systemOption.setOptionId(RedisKey.LOGINMAINTITLE);
				systemOption.setOptionValue(systemname);
				systemOptionService.addSystemOption(systemOption);
			} else {
				systemOptionService.delSystemOption(RedisKey.LOGINMAINTITLE);
			}
			String executedate = request.getParameter("executedate");
			if (StringUtils.isBlank(executedate)) {
				executedate = "0";
			}
			systemOption = new SystemOption();
			systemOption.setOptionId(CommonResources.TERMINALDATETASK);
			systemOption.setOptionValue(executedate);
			systemOptionService.addSystemOption(systemOption);

			String imagepath = request.getParameter("imagepath");
			systemOption = new SystemOption();
			systemOption.setOptionId(CommonResources.IMAGEMONITORPATH);
			systemOption.setOptionValue(imagepath);
			systemOptionService.addSystemOption(systemOption);
			// 自动补数
			if (ScheduleFactory.checkExistsScheduler(CommonResources.DATAACQUISITIONTASK)) {
				ScheduleFactory.cancelSchedule(CommonResources.DATAACQUISITIONTASK);
			}
			trigger = String.format("0 0 %s * * ?", executedate);
			ScheduleFactory.addSchedule(CommonResources.DATAACQUISITIONTASK, new CronTrigger(trigger), new DataAcquisitionTask());
			CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
			if (multipartResolver.isMultipart(request)) {
				MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
				Iterator<String> iter = multiRequest.getFileNames();
				String name, filePath = "", basePath = request.getSession().getServletContext().getRealPath("/resources/images");
				File existsFile;
				while (iter.hasNext()) {
					name = iter.next().toString();
					MultipartFile file = multiRequest.getFile(name);
					if ("backgroundfile".equalsIgnoreCase(name)) {
						filePath = basePath + "/background.png";
					} else if ("logofile".equalsIgnoreCase(name)) {
						filePath = basePath + "/logo_new.png";

					}
					existsFile = new File(filePath);
					if (existsFile.exists())
						existsFile.delete();
					file.transferTo(new File(filePath));
				}

			}
		} catch (Exception exception) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("系统设置保存失败，请联系管理员！");
			logger.error("Error in {}", exception);
		}
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
			File existsFile;
			if (type == 0) {
				filePath = basePath + "/background.png";
				file = multiRequest.getFile("backgroundfile");
			} else if (type == 1) {
				filePath = basePath + "/logo_new.png";
				file = multiRequest.getFile("logofile");
			}
			if (file != null && !StringUtils.isBlank(filePath)) {
				existsFile = new File(filePath);
				if (existsFile.exists())
					existsFile.delete();
				file.transferTo(new File(filePath));
			}else{
				responseMsg.setStatusCode(1);
				responseMsg.setMessage("系统设置保存失败，请联系管理员！");
			}

		} catch (Exception exception) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("系统设置保存失败，请联系管理员！");
			logger.error("Error in {}", exception);
		}
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

			if(imageFile.endsWith(File.separator)){
				imageFile += File.separator;
			}
			TaskFactory.getInstance().setImagePath(imageFile);
			
			new ImageSynTask().run();
			
			List<SystemOption> systemOptions = systemOptionService.getSystemOptions();
			SystemOption systemOption = systemOptionService.getSystemOption(systemOptions, CommonResources.IMAGEMONITORPATH);
			
			if(systemOption == null){
				TaskFactory.getInstance().setImagePath("");
			} else {
				imageFile = systemOption.getOptionValue();
				if(imageFile.endsWith(File.separator)){
					imageFile += File.separator;
				}
				TaskFactory.getInstance().setImagePath(imageFile);
			}

		} catch (Exception exception) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("图片同步失败，请联系管理员！");
			logger.error("Error in {}", exception);
		}
		return responseMsg;
	}
}
