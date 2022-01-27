package com.partners.weather.serial.transfer;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.partners.entity.RequestMessage;
import com.partners.weather.protocol.ComponentManager;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class SerialListener implements SerialPortEventListener {
	private static final Logger logger = LoggerFactory.getLogger(SerialListener.class);
	private SerialPort serialPort;
	private ConcurrentHashMap<String,String> keyMap=new ConcurrentHashMap<>();
	private final String key="WEATHERINFO";
	public SerialListener(SerialPort serialPort) {
		this.serialPort = serialPort;
	}

	@Override
	public void serialEvent(SerialPortEvent serialPortEvent) {
		String errorMsg = null;
		switch (serialPortEvent.getEventType()) {

		case SerialPortEvent.BI:
			errorMsg = "与串口设备通讯中断";
			break;
		case SerialPortEvent.OE:
		case SerialPortEvent.FE:
		case SerialPortEvent.PE:
		case SerialPortEvent.CD:
		case SerialPortEvent.CTS:
		case SerialPortEvent.DSR:
		case SerialPortEvent.RI:
			errorMsg = "与串口通信异常。事件：" + serialPortEvent.getEventType();
			break;
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			break;
		case SerialPortEvent.DATA_AVAILABLE:
			try {
				if (this.serialPort == null) {
					errorMsg = "串口对象为空！监听失败";

				} else {
					byte[] serialDataBs = SerialPortManager.readFromPort(serialPort);
					if (serialDataBs == null || serialDataBs.length < 1) {
						errorMsg = "未获取到有效数据！";
					} else {
						String dataOriginal = new String(serialDataBs, "UTF-8");
						//dataOriginal=dataOriginal.replace("\\r\\n", "\r\n");
						if(dataOriginal.indexOf("\r\n")==-1){
							keyMap.put(key, dataOriginal);
						}else{
							if(keyMap.containsKey(key)){
								dataOriginal=keyMap.get(key)+dataOriginal;
								keyMap.remove(key);
							}							
							RequestMessage message = null;
							int indexOf=dataOriginal.indexOf("\r\n");
							while(indexOf!=-1){
								indexOf+=2;
								String originalMsg=dataOriginal.substring(0,indexOf);
								dataOriginal=dataOriginal.substring(indexOf);							 
								if(StringUtils.isBlank(originalMsg)){
									continue;
								}
								message = new RequestMessage();
								message.setClientIP("127.0.0.1");
								message.setPort(0);
								message.setRequestMessage(originalMsg);
								ComponentManager.getInstance().getJedisQueue().pushFromHead(message);
								indexOf=dataOriginal.indexOf("\r\n");
								if(indexOf==-1 && !StringUtils.isBlank(dataOriginal)){
									keyMap.put(key, dataOriginal);
								}
							}
						}
					
					}
				}

			} catch (IOException e) {
				errorMsg = e.getMessage();
			}
			break;
		}
		if (!StringUtils.isBlank(errorMsg)) {
			logger.error(errorMsg);
		}
	}

}
