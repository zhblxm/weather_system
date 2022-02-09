package com.partners.weather.job;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import com.partners.entity.Schedulehistory;
import com.partners.weather.common.CommonUtil;

@Slf4j
public class ImageSynTask implements Runnable {

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
            log.error("Error in {}", ex);
        }
        schedulehistory.setTaskEndDate(new Timestamp(new Date().getTime()));
        TaskFactory.getScheduleHistoryService().insertHistory(schedulehistory);
    }

    private void synFile(String filePath, String compressPath) {
        final String imgFilePath = buildFilePath(filePath);
        final String syncFilePath = buildFilePath(compressPath);

        File file = new File(filePath);
        if (!file.exists() || Objects.isNull(file.listFiles())) {
            return;
        }
        Arrays.stream(file.listFiles()).forEach(item -> {
                    String fileName = item.getName();
                    File synFile = new File(syncFilePath, fileName);
                    if (synFile.exists()) {
                        return;
                    }
                    if (item.isDirectory()) {
                        if (!synFile.exists()) {
                            synFile.mkdirs();
                        }
                        try {
                            synFile(item.getCanonicalPath(), synFile.getCanonicalPath());
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                        }
                        return;
                    }
                    CommonUtil.ImageCompress(imgFilePath, fileName, syncFilePath, fileName, (float) 0.2, 0, 0);
                }
        );
    }

    private String buildFilePath(String filePath) {
        filePath = StringUtils.isBlank(filePath) ? StringUtils.EMPTY : filePath;
        if (!filePath.endsWith(File.separator)) {
            filePath += File.separator;
        }
        return filePath;
    }
}
