package com.partners.weather.service.impl;

import com.google.common.collect.Lists;

import com.partners.entity.AutoFrequencyTerminal;
import com.partners.entity.Autofrequencyhistory;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VStatistics;
import com.partners.weather.dao.IWeatherStationDAO;
import com.partners.weather.encrypt.HexUtil;
import com.partners.weather.service.IAutoFrequencyHistoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class AutoFrequencyHistoryServiceImp implements IAutoFrequencyHistoryService {

    @Autowired
    private IWeatherStationDAO weatherStationDAO;

    @Override
    public ResponseMsg insertAutoFrequencyHistory(Autofrequencyhistory autofrequencyhistory) {

        try {
            weatherStationDAO.insertAutoFrequencyHistory(autofrequencyhistory);
            return ResponseMsg.builder().statusCode(0).messageObject(HexUtil.IntToHex(autofrequencyhistory.getAutoFrequencyId())).build();
        } catch (Exception ex) {
            log.error(String.format("Error in %s", ex.getMessage()), ex);
        }
        return ResponseMsg.builder().statusCode(1).message("新增同步频率失败！").build();
    }

    @Override
    public List<Autofrequencyhistory> getAutoFrequencyHistories() {
        try {
            return weatherStationDAO.getAutoFrequencyHistories();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return Lists.newArrayListWithCapacity(0);
    }

    @Override
    public List<Autofrequencyhistory> getAutoFrequencyHistory(VStatistics statistics) {
        try {
            return weatherStationDAO.getAutoFrequencyHistory(statistics);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return Lists.newArrayListWithCapacity(0);
    }

    @Override
    public ResponseMsg batchInsertAutoFrequencyHistory(List<Autofrequencyhistory> autofrequencyhistories) {
        try {
            weatherStationDAO.batchInsertAutoFrequencyHistory(autofrequencyhistories);
            return ResponseMsg.builder().statusCode(0).build();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return ResponseMsg.builder().statusCode(1).message("批量同步频率失败！").build();
    }

    @Override
    public void batchUpdateAutoFrequencyHistory(List<AutoFrequencyTerminal> autoFrequencyTerminals) {
        try {
            if (autoFrequencyTerminals.size() < 1) {
                return;
            }
            weatherStationDAO.batchUpdateAutoFrequencyHistory(autoFrequencyTerminals);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

}
