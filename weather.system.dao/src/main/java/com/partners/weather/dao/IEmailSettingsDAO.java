package com.partners.weather.dao;

import com.partners.entity.Emalilandsmssettings;

interface IEmailSettingsDAO {

	Emalilandsmssettings getEmalilandsmssetting();

	int insertOrUpdateEmailSettings(Emalilandsmssettings emalilandsmssettingss);
}
