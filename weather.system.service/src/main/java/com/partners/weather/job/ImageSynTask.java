package com.partners.weather.job;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.partners.entity.Schedulehistory;
import com.partners.weather.common.CommonUtil;

import io.netty.util.internal.StringUtil;

public class ImageSynTask implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(ImageSynTask.class);
	
	public void run() {
		Schedulehistory schedulehistory = new Schedulehistory();
		schedulehistory.setTaskId(0);
		schedulehistory.setTaskType((byte) 2);
		schedulehistory.setTaskDesc("同步压缩图片");
		schedulehistory.setTaskResult((byte) 1);
		schedulehistory.setTaskMessage("同步压缩图片成功.");
		schedulehistory.setTaskStartDate(new Timestamp(new Date().getTime()));
		try {
			String imagePath = TaskFactory.getInstance().getImagePath();
			String compressPath = TaskFactory.getInstance().getCompressPath();
			
			synFile(imagePath, compressPath);
			
		} catch (Exception ex) {
			schedulehistory.setTaskResult((byte) 2);
			schedulehistory.setTaskMessage("同步压缩图片失败.Exception:" + ex.getMessage());
			logger.error("Error in {}", ex);
		}
		schedulehistory.setTaskEndDate(new Timestamp(new Date().getTime()));
		TaskFactory.getScheduleHistoryService().insertHistory(schedulehistory);
	}
	
	private void synFile(String filePath, String synFilePath) {
		filePath = AppendSeparator(filePath);
		synFilePath = AppendSeparator(synFilePath);
		
		File file = new File(filePath);
		if(!file.exists()){
			return;
		}
		File[] files = file.listFiles();
		if(files == null){
			return;
		}
		String fileName;
		for(File item : files){
			fileName = item.getName();
			if(item.isDirectory()){
				File synFile = new File(synFilePath + File.separator + fileName);
				if(!synFile.exists()){
					synFile.mkdirs();
				}
					
				try {
					synFile(item.getCanonicalPath(), synFile.getCanonicalPath());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				File synFile = new File(synFilePath + File.separator + fileName);
				if(synFile.exists()){
					continue;
				}
				
				CommonUtil.ImageCompress(filePath, fileName, synFilePath, fileName, (float) 0.2, 0, 0);
			}
		}
	}
	
	private String AppendSeparator(String filePath){
		if(StringUtils.isBlank(filePath)){
			filePath = "";
		}
		if(!filePath.endsWith(File.separator)){
			filePath += File.separator;
		}
		return filePath;
	}
}
