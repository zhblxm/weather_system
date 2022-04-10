package com.partners.weather.service.impl;

import com.partners.entity.Emalilandsmssettings;
import com.partners.view.entity.ResponseMsg;
import com.partners.weather.dao.IEmailSettingsDAO;
import com.partners.weather.encrypt.HexUtil;
import com.partners.weather.service.IEmailSettingService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailSettingServiceImp implements IEmailSettingService {


    private IEmailSettingsDAO emailSettingsDAO;

    @Autowired
    public EmailSettingServiceImp(IEmailSettingsDAO emailSettingsDAO) {
        this.emailSettingsDAO = emailSettingsDAO;
    }

    @Override
    public Emalilandsmssettings getEmalilandsmssetting() {
        try {
            Emalilandsmssettings setting = emailSettingsDAO.getEmalilandsmssetting();
            setting.setUniqueId(HexUtil.IntToHex(setting.getEmalilSettingsId()));
            return setting;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public ResponseMsg insertOrUpdateEmailSettings(Emalilandsmssettings emalilandsmssetting) {
        try {
            emalilandsmssetting.setEmalilSettingsId(1);
            if (!StringUtils.isBlank(emalilandsmssetting.getUniqueId())) {
                emalilandsmssetting.setEmalilSettingsId(HexUtil.HexToInt(emalilandsmssetting.getUniqueId()));
            }
            emailSettingsDAO.insertOrUpdateEmailSettings(emalilandsmssetting);
            return ResponseMsg.builder().statusCode(0).messageObject(HexUtil.IntToHex(emalilandsmssetting.getEmalilSettingsId())).build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return ResponseMsg.builder().statusCode(1).message("保存邮箱设置失败！").build();
    }

}
