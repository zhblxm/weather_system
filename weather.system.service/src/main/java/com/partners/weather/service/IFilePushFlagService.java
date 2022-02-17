package com.partners.weather.service;

import java.util.List;

import com.partners.entity.FilePushFlag;

public interface IFilePushFlagService {
	List<FilePushFlag> getAllFilePushFlags();
	
	FilePushFlag getFilePushFlag(List<FilePushFlag> filePushFlags, String terminalParam);
}
