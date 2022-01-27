package com.partners.weather.dao;

import java.util.List;

import com.partners.entity.SystemOption;

public interface ISystemOptionDAO {

	public List<SystemOption> getSystemOptions();

	public SystemOption getSystemOption(String optionId);

	public void insertSystemOption(SystemOption systemOption);
	
	public void delSystemOption(String optionId);
}
