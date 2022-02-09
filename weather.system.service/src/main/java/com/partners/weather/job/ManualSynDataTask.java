package com.partners.weather.job;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.partners.entity.RequestMessage;
import com.partners.entity.Schedulehistory;
import com.partners.entity.WeatherstationClient;
import com.partners.weather.protocol.ComponentManager;

@Slf4j
public class ManualSynDataTask implements Runnable {
	private List<Integer> weatherStationIds;
	private String systemDateFrom;
	private String systemDateTo;

	public ManualSynDataTask(String systemDateFrom, String systemDateTo, List<Integer> weatherStationIds) {
		this.weatherStationIds = weatherStationIds;
		this.systemDateFrom = systemDateFrom;
		this.systemDateTo = systemDateTo;
	}

	@Override
	public void run() {
		Schedulehistory schedulehistory = new Schedulehistory();
		schedulehistory.setTaskId(0);
		schedulehistory.setTaskType((byte) 2);
		schedulehistory.setTaskDesc("手动数据采集");
		schedulehistory.setTaskResult((byte) 1);
		schedulehistory.setTaskMessage("手动数据采集成功.");
		schedulehistory.setTaskStartDate(new Timestamp(new Date().getTime()));
		try {
			DateTime dateFrom, dateTo;
			DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
			
			dateFrom = DateTime.parse(systemDateFrom, format);
			dateTo = DateTime.parse(systemDateTo, format);

			// 获取站点、terminalCatetoryId、采集频率、ip、port
			List<WeatherstationClient> WeatherstationClients = TaskFactory.getInstance().getWeatherStationService()
					.getWSClientsByStationId(weatherStationIds);

			for (WeatherstationClient weatherstationClient : WeatherstationClients) {
				receiveData(dateFrom, dateTo, weatherstationClient, schedulehistory);
			}
		} catch (Exception ex) {
			schedulehistory.setTaskResult((byte) 2);
			schedulehistory.setTaskMessage("手动数据采集失败.Exception:" + ex.getMessage());
			log.error("Error in {}", ex);
		}
		schedulehistory.setTaskEndDate(new Timestamp(new Date().getTime()));
		TaskFactory.getScheduleHistoryService().insertHistory(schedulehistory);
	}

	/**
	 * 从Terminal获取气象数据
	 * 
	 * @param clientIP
	 * @param port
	 * @param schedulehistory
	 * @return 气象数据
	 */
	private String getTerminalData(DateTime dateFrom, DateTime dateTo, String clientIP, int port,
			Schedulehistory schedulehistory) {
		String receiveWeatherInfo = "";
		Socket socket = null;
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		// 获取Terminal数据命令
		String sendToTerminalStr = String
				.format("CM_GDC:%s,%s!", dateFrom.toString("dd-HH:mm"), dateTo.toString("dd-HH:mm"));
		byte[] sendToTerminalBs = sendToTerminalStr.getBytes();

		try {
			log.info(sendToTerminalStr);
			log.info(clientIP + ":" + port);
			socket = new Socket(clientIP, port);
			out = new BufferedOutputStream(socket.getOutputStream());
			out.write(sendToTerminalBs);
			out.flush();
			log.info("命令发送成功");
			socket.shutdownOutput();
			
			in = new BufferedInputStream(socket.getInputStream());
			int availableLen = in.available();
			byte[] buffer = new byte[availableLen];
			in.read(buffer, 0, availableLen);
			receiveWeatherInfo = new String(buffer, "UTF-8");
			log.info(receiveWeatherInfo);
		} catch (Exception ex) {
			schedulehistory.setTaskResult((byte) 2);
			schedulehistory.setTaskMessage("补数据采集失败.Exception:" + ex.getMessage());
			log.error("Error in {}", ex);
		} finally {
			try {
				if (null != in) {
					in.close();
				}
				if (null != out) {
					out.close();
				}
				if (null != socket && !socket.isClosed()) {
					socket.shutdownOutput();
					socket.shutdownInput();
					socket.close();
				}
			} catch (IOException e) {
				log.error("Error in {}", e);
			}
		}
		return receiveWeatherInfo;
	}

	/**
	 * 根据时间段进行补数
	 * 
	 * @param dateFrom
	 * @param dateTo
	 * @param weatherstationClient
	 * @param schedulehistory
	 */
	private void receiveData(DateTime dateFrom, DateTime dateTo, WeatherstationClient weatherstationClient, 
			Schedulehistory schedulehistory) {

		String clientIP = weatherstationClient.getClientIP();
		int port = weatherstationClient.getPort();
		// 获取Terminal数据
		String receiveWeatherInfo = getTerminalData(dateFrom, dateTo, clientIP, port, schedulehistory);
		// 数据写入操作
		if (!StringUtils.isBlank(receiveWeatherInfo)) {
			String[] receiveMessages = receiveWeatherInfo.split("\r\n");
			RequestMessage message = null;
			for (String receiveMsg : receiveMessages) {
				if (StringUtils.isBlank(receiveMsg)) {
					continue;
				}
				message = new RequestMessage();
				message.setClientIP(clientIP);
				message.setPort(port);
				message.setRequestMessage(receiveMsg);
				ComponentManager.getInstance().getJedisQueue().pushFromHead(message);
			}
		}
	}

}
