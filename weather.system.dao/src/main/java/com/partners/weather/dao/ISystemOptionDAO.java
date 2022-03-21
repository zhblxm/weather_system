package com.partners.weather.dao;

import java.util.List;

import com.partners.entity.SystemOption;

interface ISystemOptionDAO {

	List<SystemOption> getSystemOptions();

	SystemOption getSystemOption(String optionId);

	void insertSystemOption(SystemOption systemOption);
	
	void delSystemOption(String optionId);
}
