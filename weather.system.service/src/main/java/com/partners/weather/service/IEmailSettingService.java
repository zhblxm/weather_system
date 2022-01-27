package com.partners.weather.service;

import com.partners.entity.Emalilandsmssettings;
import com.partners.view.entity.ResponseMsg;

public interface IEmailSettingService {

	public Emalilandsmssettings getEmalilandsmssetting();

	public ResponseMsg insertOrUpdateEmailSettings(Emalilandsmssettings emalilandsmssetting);
}
