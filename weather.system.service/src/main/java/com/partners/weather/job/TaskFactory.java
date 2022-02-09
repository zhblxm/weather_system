package com.partners.weather.job;

import lombok.Data;
import org.springframework.stereotype.Component;

import com.partners.weather.service.IClientInfoService;
import com.partners.weather.service.IEmailSettingService;
import com.partners.weather.service.INotificationService;
import com.partners.weather.service.IScheduleHistoryService;
import com.partners.weather.service.ISystemOptionService;
import com.partners.weather.service.ITerminalHistoryService;
import com.partners.weather.service.IWeatherStationService;

@Component
public class TaskFactory {

	private static volatile TaskFactory INSTANCE = new TaskFactory();
	private IScheduleHistoryService historyService;
	private ISystemOptionService systemOptionService;
	private IClientInfoService clientInfoService;
	private IWeatherStationService weatherStationService;
	private ITerminalHistoryService terminalHistoryService;
	private INotificationService notificationService;
	private IEmailSettingService emailSettingService;
	private String imagePath;
	private String compressPath;
	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public String getCompressPath() {
		return compressPath;
	}

	public void setCompressPath(String compressPath) {
		this.compressPath = compressPath;
	}

	public ITerminalHistoryService getTerminalHistoryService() {
		return terminalHistoryService;
	}

	public void setTerminalHistoryService(ITerminalHistoryService terminalHistoryService) {
		this.terminalHistoryService = terminalHistoryService;
	}

	public static IScheduleHistoryService getScheduleHistoryService() {
		return INSTANCE.historyService;
	}

	public static ISystemOptionService getOptionService() {
		return INSTANCE.systemOptionService;
	}

	public IScheduleHistoryService getHistoryService() {
		return historyService;
	}

	public void setHistoryService(IScheduleHistoryService historyService) {
		this.historyService = historyService;
	}

	public static TaskFactory getInstance() {
		return INSTANCE;
	}

	public ISystemOptionService getSystemOptionService() {
		return systemOptionService;
	}

	public void setSystemOptionService(ISystemOptionService systemOptionService) {
		this.systemOptionService = systemOptionService;
	}

	public IClientInfoService getClientInfoService() {
		return clientInfoService;
	}

	public void setClientInfoService(IClientInfoService clientInfoService) {
		this.clientInfoService = clientInfoService;
	}

	public INotificationService getNotificationService() {
		return notificationService;
	}

	public void setNotificationService(INotificationService notificationService) {
		this.notificationService = notificationService;
	}

	public IWeatherStationService getWeatherStationService() {
		return weatherStationService;
	}

	public void setWeatherStationService(IWeatherStationService weatherStationService) {
		this.weatherStationService = weatherStationService;
	}

	public IEmailSettingService getEmailSettingService() {
		return emailSettingService;
	}

	public void setEmailSettingService(IEmailSettingService emailSettingService) {
		this.emailSettingService = emailSettingService;
	}

}
