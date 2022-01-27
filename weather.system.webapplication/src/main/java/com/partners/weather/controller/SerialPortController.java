package com.partners.weather.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.partners.annotation.UserAction;
import com.partners.annotation.UserPermission;
import com.partners.annotation.UserPermissionEnum;
import com.partners.entity.SystemOption;
import com.partners.view.entity.ResponseMsg;
import com.partners.weather.common.CommonResources;
import com.partners.weather.serial.transfer.SerialListener;
import com.partners.weather.serial.transfer.SerialPortManager;
import com.partners.weather.service.ISystemOptionService;

import gnu.io.SerialPort;

@Controller
@RequestMapping("/serial")
@UserPermission(value = UserPermissionEnum.SERIALPORT)
public class SerialPortController {
	private static final Logger logger = LoggerFactory.getLogger(SerialPortController.class);
	private static SerialPort SERIALPORT;
	@Resource
	ISystemOptionService systemOptionService;
	@RequestMapping
	@UserPermission(value = UserPermissionEnum.SERIALPORT)
	public String manage(HttpServletRequest request) {
		List<SystemOption> systemOptions = systemOptionService.getSystemOptions();
		SystemOption systemOption = systemOptionService.getSystemOption(systemOptions, CommonResources.SERIALPORT);
		request.setAttribute("serialport", systemOption == null ? "" : systemOption.getOptionValue());
		systemOption = systemOptionService.getSystemOption(systemOptions, CommonResources.BAUDRATE);
		request.setAttribute("baudrate", systemOption == null ? "" : systemOption.getOptionValue());
		request.setAttribute("ports", SerialPortManager.findPort());
		return "serialport";
	}

	@RequestMapping("/asynSave")
	@UserPermission(value = UserPermissionEnum.SERIALPORT)
	@UserAction(Action = UserPermissionEnum.MONITOR, Description = "串口设置")
	@ResponseBody
	public ResponseMsg saveSerialPort(HttpServletRequest request) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		String serialPort = request.getParameter("serialport");
		String baudrate = request.getParameter("baudrate");
		if (StringUtils.isBlank(serialPort) || StringUtils.isBlank(baudrate)) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage((StringUtils.isBlank(serialPort) ? "串口" : "波特率") + "名称不能为空。");
			return responseMsg;
		}
		SystemOption originSysOption= systemOptionService.getSystemOption(CommonResources.SERIALPORT);		
		SystemOption systemOption = new SystemOption();
		try {	
			systemOption.setOptionId(CommonResources.SERIALPORT);
			systemOption.setOptionValue(serialPort);
			systemOptionService.addSystemOption(systemOption);			
			systemOption = new SystemOption();
			systemOption.setOptionId(CommonResources.BAUDRATE);
			systemOption.setOptionValue(baudrate.trim());
			systemOptionService.addSystemOption(systemOption);	
		} catch (Exception ex) {
			logger.error("Error in {}", ex);
			responseMsg.setStatusCode(1);
			responseMsg.setMessage(ex.getMessage());
		}
		if(responseMsg.getStatusCode()==0){
			try {
				if(SERIALPORT!=null){
					if(originSysOption!=null && !originSysOption.getOptionValue().equalsIgnoreCase(serialPort)){
						SerialPortManager.closePort(SERIALPORT);
					}
				}else {
					SERIALPORT=SerialPortManager.openPort(serialPort, Integer.valueOf(systemOption.getOptionValue()));
					SerialPortManager.addListener(SERIALPORT, new SerialListener(SERIALPORT));
				}
			} catch (Exception ex) {
				logger.error("Error in {}", ex);
			}
		}		
		return responseMsg;
	}

}
