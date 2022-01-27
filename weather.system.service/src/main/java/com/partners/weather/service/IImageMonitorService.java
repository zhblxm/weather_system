package com.partners.weather.service;

import java.util.List;

import com.partners.view.entity.VImageMonitor;

public interface IImageMonitorService {
	public List<VImageMonitor> getImageMonitors(List<String> stationNumbers, String filePath);
}
