package com.partners.weather.job;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.partners.entity.RequestMessage;
import com.partners.entity.Schedulehistory;
import com.partners.entity.Terminalhistory;
import com.partners.entity.WeatherstationClient;
import com.partners.view.entity.VTerminalhistory;
import com.partners.weather.common.CommonResources;
import com.partners.weather.protocol.ComponentManager;
import com.partners.weather.search.SearchIndexUtil;

@Slf4j
public class DataAcquisitionTask implements Runnable {

    @Override
    public void run() {
        Schedulehistory schedulehistory = new Schedulehistory();
        schedulehistory.setTaskId(0);
        schedulehistory.setTaskType((byte) 2);
        schedulehistory.setTaskDesc("同步数据采集");
        schedulehistory.setTaskResult((byte) 1);
        schedulehistory.setTaskMessage("补数据采集成功.");
        schedulehistory.setTaskStartDate(new Timestamp(new Date().getTime()));
        try {
            // 补数逻辑
            DateTime now = DateTime.now();
            DateTime dateFrom, dateTo;
            DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

            // 获取站点、terminalCatetoryId、采集频率、ip、port
            List<WeatherstationClient> WeatherstationClients = TaskFactory.getInstance().getWeatherStationService()
                    .getWeatherStationClients();

            for (WeatherstationClient weatherstationClient : WeatherstationClients) {
                String systemDateFrom = now.minusDays(1).toString("yyyy-MM-dd 00:00:00");
                String systemDateTo = now.toString("yyyy-MM-dd 00:00:00");

                SearchHit[] TerminalDatas = searchHitData(weatherstationClient.getTerminalModel(),
                        weatherstationClient.getWeatherStationId(),
                        systemDateFrom,
                        systemDateTo);

                // 计算采集频率，单位：分钟
                int acquisitionFrequency = getAcquisitionFrequency(weatherstationClient.getAcquisitionFrequency(),
                        weatherstationClient.getAcquisitionFrequencyUnit());
                //进行补数
                dateFrom = DateTime.parse(systemDateFrom, format);
                for (SearchHit terminalData : TerminalDatas) {
                    String fullCollectDate = (String) terminalData.getSource().get("fullCollectDate");
                    dateTo = DateTime.parse(fullCollectDate, format);
                    receiveData(dateFrom, dateTo, acquisitionFrequency, weatherstationClient, schedulehistory);
                    dateFrom = dateTo;
                }
                //补充TerminalhistoryList后缺失数据，
                dateTo = DateTime.parse(systemDateTo, format);
                receiveData(dateFrom, dateTo, acquisitionFrequency, weatherstationClient, schedulehistory);
            }
        } catch (Exception ex) {
            schedulehistory.setTaskResult((byte) 2);
            schedulehistory.setTaskMessage("补数据采集失败.Exception:" + ex.getMessage());
            log.error("Error in {}", ex);
        }
        schedulehistory.setTaskEndDate(new Timestamp(new Date().getTime()));
        TaskFactory.getScheduleHistoryService().insertHistory(schedulehistory);
    }

    @SuppressWarnings("deprecation")
    private SearchHit[] searchHitData(String terminalModel, int WeatherStationId, String systemDateFrom, String systemDateTo) {
        SearchHit[] hits = null;
        try {
            SearchResponse response = SearchIndexUtil.getClient().prepareSearch(CommonResources.WEATHERSYSTEM)
                    .setFrom(0).setSize(10000).//最大是1万，也是ES的最大值
                            setQuery(QueryBuilders.boolQuery()
                            .filter(QueryBuilders.andQuery(
                                    //日期区间，该后面的值就行
                                    QueryBuilders.rangeQuery("systemDate").format("yyyy-MM-dd HH:mm:ss").gt(systemDateFrom).lt(systemDateTo),
                                    //站点ID，23和30是站点的ID，可传递1个或多个，不需要可将这段代码删除
                                    QueryBuilders.termsQuery("stationId", new int[]{WeatherStationId}),
                                    //设备编号，可以传递一个或多个，用不到这个条件，可以删除
                                    QueryBuilders.termsQuery("terminalModel", new String[]{terminalModel})
                            ))
                    ).setExplain(false).get();
            hits = response.getHits().getHits();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return hits;
    }

    /**
     * 计算采集频率
     *
     * @param acquisitionFrequency
     * @param acquisitionFrequencyUnit
     * @return 采集频率，单位：分钟
     */
    private int getAcquisitionFrequency(int acquisitionFrequency, String acquisitionFrequencyUnit) {
        if (acquisitionFrequencyUnit == "d") {
            acquisitionFrequency = acquisitionFrequency * 24 * 60;
        } else if (acquisitionFrequencyUnit == "h") {
            acquisitionFrequency = acquisitionFrequency * 60;
        }
        return acquisitionFrequency;
    }

    /**
     * 从Terminal获取气象数据
     *
     * @param receiveDate
     * @param clientIP
     * @param port
     * @param schedulehistory
     * @return 气象数据
     */
    private String getTerminalData(DateTime receiveDate, String clientIP, int port, Schedulehistory schedulehistory) {
        String receiveWeatherInfo = "";
        Socket socket = null;
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        //获取Terminal数据命令
        byte[] sendToTerminalBs = String.format("CM_GTC:%s!", receiveDate.toString("yyyy-MM-dd-HH:mm:00")).getBytes();

        try {
            socket = new Socket(clientIP, port);
            out = new BufferedOutputStream(socket.getOutputStream());
            out.write(sendToTerminalBs);
            out.flush();
            socket.shutdownOutput();

            in = new BufferedInputStream(socket.getInputStream());
            int availableLen = in.available();
            byte[] buffer = new byte[availableLen];
            in.read(buffer, 0, availableLen);
            receiveWeatherInfo = new String(buffer, "UTF-8");

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
     * @param acquisitionFrequency
     * @param weatherstationClient
     * @param schedulehistory
     */
    private void receiveData(DateTime dateFrom, DateTime dateTo, int acquisitionFrequency,
                             WeatherstationClient weatherstationClient, Schedulehistory schedulehistory) {

        Period period = new Period(dateFrom, dateTo, PeriodType.minutes());
        int periodMinutes = period.getMinutes();
        //时间差<=采集频率不进行补数
        if (periodMinutes <= acquisitionFrequency) {
            return;
        } else {
            //计算需要补数的时间点
            DateTime receiveDate = dateTo.minusMinutes(periodMinutes);
            String clientIP = weatherstationClient.getClientIP();
            int port = weatherstationClient.getPort();
            //获取Terminal数据
            String receiveWeatherInfo = getTerminalData(receiveDate, clientIP, port, schedulehistory);
            //数据写入操作
            if (StringUtils.isBlank(receiveWeatherInfo)) {
                return;
            }
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
            //递归，继续进行补数，直到时间差<=采集频率
            receiveData(dateFrom, receiveDate, acquisitionFrequency, weatherstationClient, schedulehistory);
        }
    }
}
