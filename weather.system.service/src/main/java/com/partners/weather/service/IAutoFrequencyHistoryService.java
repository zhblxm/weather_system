package com.partners.weather.service;

import java.util.List;

import com.partners.entity.AutoFrequencyTerminal;
import com.partners.entity.Autofrequencyhistory;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VStatistics;

public interface IAutoFrequencyHistoryService {

	ResponseMsg insertAutoFrequencyHistory(Autofrequencyhistory autofrequencyhistory);

	List<Autofrequencyhistory> getAutoFrequencyHistories();

	List<Autofrequencyhistory> getAutoFrequencyHistory(VStatistics statistics);
	
	ResponseMsg batchInsertAutoFrequencyHistory(List<Autofrequencyhistory> autofrequencyhistories);
	
	void batchUpdateAutoFrequencyHistory(List<AutoFrequencyTerminal> autoFrequencyTerminals);	
}
