package com.partners.weather.dao;

import com.partners.entity.Emalilandsmssettings;

public interface IEmailSettingsDAO {

	Emalilandsmssettings getEmalilandsmssetting();

	int insertOrUpdateEmailSettings(Emalilandsmssettings emalilandsmssettingss);
}
