package com.partners.weather.service.impl;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import com.alibaba.fastjson.JSON;
import com.partners.entity.TerminalWeatherDetail;

public class RequestMessageUtil {
	private static final String PORTABLETERMINAL_TEMPLETE = "{\"header\": \"%s\",\"terminalNumber\": \"%s\",\"shortDate\": \"%s\",\"shortTime\": \"%s\",\"temperature\": %s,\"humidity\": %s,\"windDirection\": %s,\"windSpeed\":%s,\"pressure\": %s,\"rainfall\": %s,\"isValidRadiation\": %s,\"voltage\":%s}";
	private static final String INTELLIGENCETESTTERMINAL_TEMPLETE = "{\"terminalNumber\":\"%s\",\"shortDate\":\"%s\",\"shortTime\":\"%s\",\"temperature\":%s,\"humidity\":%s,\"isValidRadiation\":%s,\"voltage\":%s}";
	private static final String MBBA1TERMINAL_TEMPLETE = "{\"terminalNumber\":\"%s\",\"collectDate\":\"%s\",\"windSpeed\":%s,\"windDirection\":%s,\"rainfall\":%s,\"temperature\":%s,\"humidity\":%s,\"pressure\":%s,\"concentration\":%s,\"isValidRadiation\":%s,\"sunshineNumber\":%s,\"totlRadiation\":%s,\"evaporation\":%s,\"voltage\":%s,\"soilTemperature10\":%s,\"soilTemperature20\":%s,\"soilTemperature30\":%s,\"soilTemperature40\":%s,\"soilTemperature50\":%s,\"soilTemperature60\":%s,\"soilMoisture10\":%s,\"soilMoisture20\":%s,\"soilMoisture30\":%s,\"soilMoisture40\":%s,\"soilMoisture50\":%s,\"soilMoisture60\":%s}";
	private static final String MBBA2TERMINAL_TEMPLETE = "{\"terminalNumber\":\"%s\",\"collectDate\":\"%s\",\"windSpeed\":%s,\"windDirection\":%s,\"rainfall\":%s,\"temperature\":%s,\"humidity\":%s,\"pressure\":%s,\"concentration\":%s,\"isValidRadiation\":%s,\"totlRadiation\":%s,\"radiation\":%s,\"voltage\":%s,\"soilTemperature10\":%s,\"soilTemperature20\":%s,\"soilTemperature30\":%s,\"soilTemperature40\":%s,\"soilTemperature50\":%s,\"soilTemperature60\":%s,\"soilMoisture10\":%s,\"soilMoisture20\":%s,\"soilMoisture30\":%s,\"soilMoisture40\":%s,\"soilMoisture50\":%s,\"soilMoisture60\":%s}";

	public static TerminalWeatherDetail parse(String[] messages) throws Exception {
		String fillTemplete = "";
		int messageLen=messages.length;
		switch (messageLen) {
		case 12:
			fillTemplete = PORTABLETERMINAL_TEMPLETE;
			break;
		case 7:
			fillTemplete = INTELLIGENCETESTTERMINAL_TEMPLETE;
			break;
		case 25:
			fillTemplete =MBBA2TERMINAL_TEMPLETE;
			break;
		case 26:
			fillTemplete =MBBA1TERMINAL_TEMPLETE;
			break;
		default:
			throw new Exception("未知格式类型");
		}
		fillTemplete=String.format(fillTemplete, messages);
		TerminalWeatherDetail terminalWeatherDetail=JSON.parseObject(fillTemplete,TerminalWeatherDetail.class);
		if(terminalWeatherDetail!=null)
		{	   
			switch (messageLen) {
			case 12:
			case 7:
				terminalWeatherDetail.setFullDate(DateTime.parse(terminalWeatherDetail.getShortDate()+" "+terminalWeatherDetail.getShortTime(),  DateTimeFormat .forPattern("yyyy-MM-dd HH:mm:ss")));
				break;
			case 25:
			case 26:
				terminalWeatherDetail.setFullDate(DateTime.parse(terminalWeatherDetail.getCollectDate(),  DateTimeFormat .forPattern("yyyyMMddHHmmss")));
				break;
			}
		}
		return terminalWeatherDetail;
	}

	public static void main(String[] args) throws Exception {
		String aString = "";
//		aString = "20016 20150402104800 34 10 2 198 39 990 0 999 649 999 0 120 173 999 999 999 999 999 132 49 49 48 49 49";//MA-BA1农业自动气象监测站-26
//		aString="1006 2015-01-12 15:00:00  228 178 0000 368";//智能温湿仪-7
//		aString="HY6MAIN-ID 1002 2015-04-07 09:29:00 -10 0 0 0 0 0 0 70";//便携自动站-12													
		aString="20003 20150206151743 9 155 0	177	45 0 57	912	60 0 128 152 195 156 188 0 0 0 0 0 0 0 0";//MA-BA2农业自动气象监测站-25
		String[] weatherItems = aString.split("\\s+");	
		System.out.println(weatherItems.length); 
		TerminalWeatherDetail terminalWeatherDetail=RequestMessageUtil.parse(weatherItems);
		System.out.println(terminalWeatherDetail.getFullDate().toString("yyyy-MM-dd HH:mm:ss")); 
		System.out.println(JSON.toJSON(terminalWeatherDetail)); 

	}
}
