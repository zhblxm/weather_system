package com.partners.weather.service;

import java.util.List;

import com.partners.entity.SystemOption;
import com.partners.view.entity.ResponseMsg;

public interface ISystemOptionService {
	public List<SystemOption> getSystemOptions();

	public SystemOption getSystemOption(String optionId);

	public ResponseMsg addSystemOption(SystemOption systemOption);
	
	public SystemOption getSystemOption(List<SystemOption> systemOptions,String optionId);
	
	public ResponseMsg delSystemOption(String optionId);
}
