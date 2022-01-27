package com.partners.weather.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.partners.entity.FilePushFlag;
import com.partners.weather.dao.IFilePushFlagDAO;
import com.partners.weather.service.IFilePushFlagService;

@Service
public class FilePushFlagServiceImp implements IFilePushFlagService {
	@Autowired
	private IFilePushFlagDAO filePushFlagDAO;
	
	private static final Logger logger = LoggerFactory.getLogger(FilePushFlagServiceImp.class);
	
	@Override
	public List<FilePushFlag> getAllFilePushFlags() {
		List<FilePushFlag> filePushFlags = null;
		try {
			filePushFlags = filePushFlagDAO.getAllFilePushFlags();
		} catch (Exception e) {
			//logger.error("Error in {}", e);
		}
		return filePushFlags == null ? new ArrayList<FilePushFlag>(0) : filePushFlags;
	}
	
	@Override
	public FilePushFlag getFilePushFlag(List<FilePushFlag> filePushFlags, String terminalParam) {
		FilePushFlag filePushFlag=null;
		if(filePushFlags==null)
		{
			return filePushFlag;
		}
		try {
			for (FilePushFlag optionItem : filePushFlags) {
				if(terminalParam.equals(optionItem.getTerminalparam()))
				{
					filePushFlag=optionItem;
					break;
				}
			}
		} catch (Exception e) {
			logger.error("Error in {}", e);
		}
		return filePushFlag;
	}
}
