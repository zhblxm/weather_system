package com.partners.weather.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.partners.entity.AutoFrequencyTerminal;
import com.partners.entity.Autofrequencyhistory;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VStatistics;
import com.partners.weather.dao.IWeatherStationDAO;
import com.partners.weather.encrypt.HexUtil;
import com.partners.weather.service.IAutoFrequencyHistoryService;

@Service
@Transactional
public class AutoFrequencyHistoryServiceImp implements IAutoFrequencyHistoryService {

	private static final Logger logger = LoggerFactory.getLogger(AutoFrequencyHistoryServiceImp.class);
	@Autowired
	private IWeatherStationDAO weatherStationDAO;
	@Override
	public ResponseMsg insertAutoFrequencyHistory(Autofrequencyhistory autofrequencyhistory) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		try {
			weatherStationDAO.insertAutoFrequencyHistory(autofrequencyhistory);
			responseMsg.setMessageObject(HexUtil.IntToHex(autofrequencyhistory.getAutoFrequencyId()));
		} catch (Exception ex) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("新增同步频率失败！");
			logger.error("Erron in {}", ex);
		} 
		return responseMsg;
	}

	@Override
	public List<Autofrequencyhistory> getAutoFrequencyHistories() {
		List<Autofrequencyhistory> autofrequencyhistories = null;
		try {
			autofrequencyhistories = weatherStationDAO.getAutoFrequencyHistories();

		} catch (Exception ex) {
			logger.error("Erron in {}", ex);
		}
		return autofrequencyhistories == null ? new ArrayList<Autofrequencyhistory>(0) : autofrequencyhistories;
	}

	@Override
	public List<Autofrequencyhistory> getAutoFrequencyHistory(VStatistics statistics) {
		List<Autofrequencyhistory> autofrequencyhistories = null;
		try {
			autofrequencyhistories = weatherStationDAO.getAutoFrequencyHistory(statistics);
		} catch (Exception ex) {
			logger.error("Erron in {}", ex);
		}
		return autofrequencyhistories == null ? new ArrayList<Autofrequencyhistory>(0) : autofrequencyhistories;
	}

	@Override
	public ResponseMsg batchInsertAutoFrequencyHistory(List<Autofrequencyhistory> autofrequencyhistories) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		try {
			weatherStationDAO.batchInsertAutoFrequencyHistory(autofrequencyhistories);
			responseMsg.setMessageObject("");
		} catch (Exception ex) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("批量同步频率失败！");
			logger.error("Erron in {}", ex);
		} 
		return responseMsg;
	}

	@Override
	public void batchUpdateAutoFrequencyHistory(List<AutoFrequencyTerminal> autoFrequencyTerminals) {
		try {
			if(autoFrequencyTerminals.size()>0)
			{
				weatherStationDAO.batchUpdateAutoFrequencyHistory(autoFrequencyTerminals);
			}
		} catch (Exception ex) {
			logger.error("Erron in {}", ex);
		} 
	}

}
