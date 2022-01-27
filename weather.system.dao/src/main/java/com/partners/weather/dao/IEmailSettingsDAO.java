package com.partners.weather.dao;

import com.partners.entity.Emalilandsmssettings;

public interface IEmailSettingsDAO {

	public Emalilandsmssettings getEmalilandsmssetting();

	public int insertOrUpdateEmailSettings(Emalilandsmssettings emalilandsmssettingss);
}
