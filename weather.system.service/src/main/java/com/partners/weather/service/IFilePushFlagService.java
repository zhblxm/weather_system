package com.partners.weather.service;

import java.util.List;

import com.partners.entity.FilePushFlag;

public interface IFilePushFlagService {
	public List<FilePushFlag> getAllFilePushFlags();
	
	public FilePushFlag getFilePushFlag(List<FilePushFlag> filePushFlags, String terminalParam);
}
