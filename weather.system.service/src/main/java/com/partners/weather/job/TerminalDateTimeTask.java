package com.partners.weather.job;

import com.partners.entity.ClientInfo;
import com.partners.entity.Schedulehistory;
import com.partners.entity.SystemOption;
import com.partners.weather.common.CommonResources;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class TerminalDateTimeTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(TerminalDateTimeTask.class);

    @Override
    public void run() {
        Schedulehistory schedulehistory = new Schedulehistory();
        schedulehistory.setTaskId(0);
        schedulehistory.setTaskType((byte) 1);
        schedulehistory.setTaskDesc("同步设备时间");
        schedulehistory.setTaskResult((byte) 1);
        schedulehistory.setTaskMessage("时间同步成功.");
        schedulehistory.setTaskStartDate(new Timestamp(new Date().getTime()));
        //获取所有客户端的连接端口
        List<ClientInfo> clientInfos = TaskFactory.getInstance().getClientInfoService().getClientInfos();
        Socket socket = null;
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH-mm-ss");
        byte[] sendToTerminalBs = String.format("CM_STD:%s!", DateTime.now().toString(format)).getBytes();
        for (ClientInfo clientInfo : clientInfos) {
            try {
                socket = new Socket(clientInfo.getClientIP(), clientInfo.getPort());
                // in = new BufferedInputStream(socket.getInputStream());
                out = new BufferedOutputStream(socket.getOutputStream());
                out.write(sendToTerminalBs);
            } catch (Exception ex) {
                schedulehistory.setTaskResult((byte) 2);
                schedulehistory.setTaskMessage("时间同步失败.Exception:" + ex.getMessage());
                logger.error("Error in {}", ex);
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
                    logger.error("Error in {}", e);
                }
            }
        }
        TaskFactory.getOptionService().addSystemOption(SystemOption.builder().optionId(CommonResources.AUTOREFRESHDATE)
                .optionValue("N").build());
        schedulehistory.setTaskEndDate(new Timestamp(new Date().getTime()));
        TaskFactory.getScheduleHistoryService().insertHistory(schedulehistory);
        // 取消任务
        ScheduleFactory.cancelSchedule(CommonResources.TERMINALDATETASK);
    }

}
