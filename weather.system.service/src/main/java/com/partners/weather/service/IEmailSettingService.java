package com.partners.weather.service;

import com.partners.entity.Emalilandsmssettings;
import com.partners.view.entity.ResponseMsg;

public interface IEmailSettingService {

	Emalilandsmssettings getEmalilandsmssetting();

	ResponseMsg insertOrUpdateEmailSettings(Emalilandsmssettings emalilandsmssetting);
}
