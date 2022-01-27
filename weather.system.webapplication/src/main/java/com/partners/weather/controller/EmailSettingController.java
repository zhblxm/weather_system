package com.partners.weather.controller;

import java.text.ParseException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.partners.annotation.UserAction;
import com.partners.annotation.UserPermission;
import com.partners.annotation.UserPermissionEnum;
import com.partners.entity.Emalilandsmssettings;
import com.partners.view.entity.ResponseMsg;
import com.partners.weather.encrypt.HexUtil;
import com.partners.weather.service.IEmailSettingService;

@Controller
@RequestMapping("/emailsetup")
public class EmailSettingController {

	@Resource
	IEmailSettingService emailSettingService;

	@RequestMapping
	@UserPermission(value = UserPermissionEnum.EMAILANDSMS)
	@UserAction(Action = UserPermissionEnum.EMAILANDSMS, Description = "邮件服务器和短信设置")
	public String index(HttpServletRequest request) throws ParseException {
		request.setAttribute("setting", emailSettingService.getEmalilandsmssetting());
		return "emailsetting";
	}

	@RequestMapping("/save")
	@UserPermission(value = UserPermissionEnum.USERGUOUPINSERTANDUPDATE)
	@UserAction(Action = UserPermissionEnum.USERGUOUP, Description = "添加和更新用户组管理")
	@ResponseBody
	public ResponseMsg save(@RequestBody Emalilandsmssettings settings) {
		if(settings!=null)
		{
			settings.setSmtpuserPwd(HexUtil.StringToHex(settings.getSmtpuserPwd()));
			settings.setSmsuserPwd(HexUtil.StringToHex(settings.getSmsuserPwd()));
		}
		ResponseMsg responseMsg=emailSettingService.insertOrUpdateEmailSettings(settings);
		return responseMsg;
	}

}
