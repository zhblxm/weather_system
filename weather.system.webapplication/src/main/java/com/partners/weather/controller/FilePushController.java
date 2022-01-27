package com.partners.weather.controller;

import java.io.File;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.partners.annotation.UserAction;
import com.partners.annotation.UserPermission;
import com.partners.annotation.UserPermissionEnum;
import com.partners.entity.SystemOption;
import com.partners.entity.RequestMessage;
import com.partners.view.entity.ResponseMsg;
import com.partners.weather.common.CommonResources;
import com.partners.weather.ftp.FtpUtil;
import com.partners.weather.ftp.SftpUtil;
import com.partners.weather.service.IParseReqeustService;
import com.partners.weather.service.ISystemOptionService;

@Controller
@RequestMapping("/filepush")
@UserPermission(value = UserPermissionEnum.FILEPUSH)
public class FilePushController {
	private static final Logger logger = LoggerFactory.getLogger(FilePushController.class);
	@Resource
	ISystemOptionService systemOptionService;
	@Resource
	IParseReqeustService parseReqeustService;
	
	/**
	 * @param request
	 * @return
	 */
	@RequestMapping
	@UserPermission(value = UserPermissionEnum.FILEPUSH)
	@UserAction(Action = UserPermissionEnum.FILEPUSH, Description = "系统设置")
	public String Manage(HttpServletRequest request) {
		List<SystemOption> systemOptions= systemOptionService.getSystemOptions();
		SystemOption systemOption = systemOptionService.getSystemOption(systemOptions,CommonResources.FILEPUSHFTPURL);
		request.setAttribute("ftpurl", systemOption==null?"":systemOption.getOptionValue());
		
		systemOption = systemOptionService.getSystemOption(systemOptions,CommonResources.FILEPUSHFTPPORT);
		request.setAttribute("ftpport", systemOption==null?"":systemOption.getOptionValue());
		
		systemOption = systemOptionService.getSystemOption(systemOptions,CommonResources.FILEPUSHFTPUSER);
		request.setAttribute("ftpuser", systemOption==null?"":systemOption.getOptionValue());
		
		systemOption = systemOptionService.getSystemOption(systemOptions,CommonResources.FILEPUSHFTPTYPE);
		request.setAttribute("ftptype", systemOption==null?"ftp":systemOption.getOptionValue());	
		
		systemOption = systemOptionService.getSystemOption(systemOptions,CommonResources.FILEPUSHONOFF);
		request.setAttribute("ftponoff", systemOption==null?"off":systemOption.getOptionValue());
		
		systemOption = systemOptionService.getSystemOption(systemOptions,CommonResources.FILEPUSHFTPPWD);
		request.setAttribute("ftppwd", systemOption==null?"":systemOption.getOptionValue());
/*
		RequestMessage message = new RequestMessage();
		message.setClientIP("127.0.0.1");
		message.setPort(8080);
		message.setRequestMessage("10071 20171110113000    0   0    0  999   0 1038    0  528  659  514    0 123    0    0    0    0    0    0    0    0    0    0    0    0");
		
		try {
			parseReqeustService.parse(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
		return "filepush";
	}
	
	@RequestMapping("/save")
	@UserPermission(value = UserPermissionEnum.FILEPUSH)
	@UserAction(Action = UserPermissionEnum.FILEPUSH, Description = "更新文件设置")
	@ResponseBody
	public ResponseMsg saveFilePush(HttpServletRequest request) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		boolean ftpIsConnected = false;

		try {
			String ftpurl = request.getParameter("ftpurl");
			String ftpport = request.getParameter("ftpport");
			String ftpuser = request.getParameter("ftpuser");
			String ftppwd = request.getParameter("ftppwd");
			String ftptype = request.getParameter("ftptype");
			String ftponoff = request.getParameter("ftponoff");

			try {
				if(ftptype.equals("ftp")){
					FtpUtil ftpUtil = new FtpUtil(2, 2, 2);
					ftpUtil.connect(ftpurl, Integer.parseInt(ftpport), ftpuser, ftppwd, true);
					if (ftpUtil.isConnected()) {
						ftpIsConnected = true;
						ftpUtil.disconnect();
					} else {
						ftpIsConnected = false;
						responseMsg.setStatusCode(1);
						responseMsg.setMessage("ftp连接失败！");
					}
				} else {
					SftpUtil sftpUtil = new SftpUtil(ftpurl, ftpuser, Integer.parseInt(ftpport), 2);
					sftpUtil.connect(ftppwd);
					ftpIsConnected = true;
					sftpUtil.disconnect();
				}
			} catch (Exception ex) {
				ftpIsConnected = false;
				responseMsg.setStatusCode(1);
				responseMsg.setMessage("ftp连接失败！");
			}

			if (ftpIsConnected) {
				SystemOption systemOption = new SystemOption();
				systemOption.setOptionId(CommonResources.FILEPUSHFTPURL);
				systemOption.setOptionValue(ftpurl);
				systemOptionService.addSystemOption(systemOption);

				systemOption = new SystemOption();
				systemOption.setOptionId(CommonResources.FILEPUSHFTPPORT);
				systemOption.setOptionValue(ftpport);
				systemOptionService.addSystemOption(systemOption);

				systemOption = new SystemOption();
				systemOption.setOptionId(CommonResources.FILEPUSHFTPUSER);
				systemOption.setOptionValue(ftpuser);
				systemOptionService.addSystemOption(systemOption);

				systemOption = new SystemOption();
				systemOption.setOptionId(CommonResources.FILEPUSHFTPPWD);
				systemOption.setOptionValue(ftppwd);
				systemOptionService.addSystemOption(systemOption);

				systemOption = new SystemOption();
				systemOption.setOptionId(CommonResources.FILEPUSHFTPTYPE);
				systemOption.setOptionValue(ftptype);
				systemOptionService.addSystemOption(systemOption);

				systemOption = new SystemOption();
				systemOption.setOptionId(CommonResources.FILEPUSHONOFF);
				systemOption.setOptionValue(ftponoff);
				systemOptionService.addSystemOption(systemOption);
			}
		} catch (Exception exception) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("文件推送设置保存失败，请联系管理员！");
			logger.error("Error in {}", exception);
		}
		
		return responseMsg;
	}
	@RequestMapping("/upload")
	@UserPermission(value = UserPermissionEnum.FILEPUSH)
	@UserAction(Action = UserPermissionEnum.FILEPUSH, Description = "更新文件模板")
	@ResponseBody
	public ResponseMsg uploadFile(HttpServletRequest request) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		try {
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			MultipartFile file = null;

			String filePath = "", basePath = request.getSession().getServletContext().getRealPath("/resources/download");
			File existsFile;
			filePath = basePath + "/filetempletup.txt";
			file = multiRequest.getFile("fileTemplet");

			if (file != null && !StringUtils.isBlank(filePath)) {
				existsFile = new File(filePath);
				if (existsFile.exists())
					existsFile.delete();
				file.transferTo(new File(filePath));
			}else{
				responseMsg.setStatusCode(1);
				responseMsg.setMessage("文件模板保存失败，请联系管理员！");
			}

		} catch (Exception exception) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("文件模板保存失败，请联系管理员！");
			logger.error("Error in {}", exception);
		}
		return responseMsg;
	}
}
