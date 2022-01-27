package com.partners.weather.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.partners.entity.Emalilandsmssettings;
import com.partners.view.entity.ResponseMsg;
import com.partners.weather.dao.IEmailSettingsDAO;
import com.partners.weather.encrypt.HexUtil;
import com.partners.weather.service.IEmailSettingService;

@Service
public class EmailSettingServiceImp implements IEmailSettingService {
	private static final Logger logger = LoggerFactory.getLogger(EmailSettingServiceImp.class);

	@Autowired
	IEmailSettingsDAO emailSettingsDAO;

	@Override
	public Emalilandsmssettings getEmalilandsmssetting() {
		Emalilandsmssettings setting = null;
		try {
			setting = emailSettingsDAO.getEmalilandsmssetting();
			if(setting!=null){
				setting.setUniqueId(HexUtil.IntToHex(setting.getEmalilSettingsId()));
			}
		} catch (Exception e) {
			logger.error("Error in {}", e);
		}
		return setting;
	}

	@Override
	public ResponseMsg insertOrUpdateEmailSettings(Emalilandsmssettings emalilandsmssetting) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		try {
			emalilandsmssetting.setEmalilSettingsId(1);
			if (!StringUtils.isBlank(emalilandsmssetting.getUniqueId())) {
				emalilandsmssetting.setEmalilSettingsId(HexUtil.HexToInt(emalilandsmssetting.getUniqueId()));
			}
			emailSettingsDAO.insertOrUpdateEmailSettings(emalilandsmssetting);
			responseMsg.setMessageObject(HexUtil.IntToHex(emalilandsmssetting.getEmalilSettingsId()));
		} catch (Exception e) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("保持邮箱设置失败！");
			logger.error("Error in {}", e);
		}
		return responseMsg;
	}

}
