package com.partners.weather.service.impl;

import java.io.File;
import java.io.FileFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.partners.view.entity.VImageInfo;
import com.partners.view.entity.VImageMonitor;
import com.partners.weather.service.IImageMonitorService;

import io.netty.util.internal.StringUtil;

@Service
public class ImageMonitorServiceImp  implements IImageMonitorService {
	@Override
	public List<VImageMonitor> getImageMonitors(final List<String> stationNumbers, String filePath){
		List<VImageMonitor> imageMonitors = new ArrayList<VImageMonitor>();
		
		if(StringUtil.isNullOrEmpty(filePath))
			return null;
		
		if(filePath.endsWith(File.separator)){
			filePath += "imagemonitor" + File.separator;
		} else {
			filePath += File.separator + "imagemonitor" + File.separator;
		}
		
		File file=new File(filePath);
		if(!file.exists() || !file.isDirectory()) {
			if(!file.mkdir()) {
				return null;
			}
		}
		
		File[] allFiles = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
            	if(pathname.isFile()){
            		return false;
            	}
            	if(stationNumbers == null || stationNumbers.size() < 1){
            		return true;
            	}
            	if(stationNumbers.contains(pathname.getName())){
            		return true;
            	}
               return false;
            }
         });
		
		for(File f : allFiles) {
			VImageMonitor imageMonitor = new VImageMonitor();
			imageMonitor.setWeatherStationNumber(f.getName());
			imageMonitor.setImages(getAllFile(f, f.getName()));
			imageMonitors.add(imageMonitor);
		}
		
		return imageMonitors;
	}
	
	private List<VImageInfo> getAllFile(File file, String path){
		/*
		List<VImageInfo> imageInfos  = new ArrayList<VImageInfo>();
		File[] fileDates = file.listFiles();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String strFileDate, strFileTime, strFileImage;
		for(File fileDate: fileDates){
			if(fileDate.isFile()){
				continue;
			}
			strFileDate = fileDate.getName();
			if(strFileDate.length() != 8){
				continue;
			}
			strFileDate = strFileDate.substring(0, 4) + "-" + strFileDate.substring(4,6) + "-" + strFileDate.substring(6);
			try {
				format.parse(strFileDate + " 00:00");
			} catch (ParseException e) {
				continue;
			}
			File[] fileTimes = fileDate.listFiles();
			for(File fileTime: fileTimes){
				if(fileTime.isFile()){
					continue;
				}
				strFileTime = fileTime.getName();
				if(strFileTime.length() != 4){
					continue;
				}
				strFileTime = strFileTime.substring(0, 2) + ":" + strFileTime.substring(2,4);
				try {
					format.parse(strFileDate + " " + strFileTime);
					
				} catch (ParseException e) {
					continue;
				}
				
				File[] fileImages = fileTime.listFiles();
				for(File fileImage : fileImages) {
					strFileImage = fileImage.getName();
					String extension = getExtensionName(strFileImage);
					if(!extension.equalsIgnoreCase("jpg")) {
						continue;
					}
					
					VImageInfo imageInfo = new VImageInfo();
					imageInfo.setImageDate(strFileDate);
					imageInfo.setImageTime(strFileTime);
					imageInfo.setImageUrl(String.format("/resources/images/imagemonitor/%s/%s/%s/%s", file.getName(), fileDate.getName(), fileTime.getName(), fileImage.getName()));
					imageInfos.add(imageInfo);
				}
			}
		}
		return imageInfos;
		*/
		
		List<VImageInfo> imageInfos  = new ArrayList<VImageInfo>();
		String strFileImage;
		for(File item : file.listFiles()){
			if(item.isDirectory()){
				imageInfos.addAll(getAllFile(item, path + File.separator + item.getName()));
			} else {
				strFileImage = item.getName();
				String extension = getExtensionName(strFileImage);
				if(!extension.equalsIgnoreCase("jpg")) {
					continue;
				}
				VImageInfo imageInfo = new VImageInfo();
				imageInfo.setImageUrl(String.format("/resources/images/imagemonitor/%s/%s", path, item.getName()));
				imageInfos.add(imageInfo);
			}
		}
		
		return imageInfos;
	}
	
	private String getExtensionName(String filename) {   
        if ((filename != null) && (filename.length() > 0)) {   
            int dot = filename.lastIndexOf('.');   
            if ((dot >-1) && (dot < (filename.length() - 1))) {   
                return filename.substring(dot + 1);   
            }   
        }   
        return filename;   
    } 
}
