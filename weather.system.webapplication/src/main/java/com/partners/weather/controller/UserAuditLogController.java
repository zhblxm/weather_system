package com.partners.weather.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.partners.annotation.UserPermission;
import com.partners.annotation.UserPermissionEnum;
import com.partners.entity.Adminuser;
import com.partners.entity.AuditTrailLog;
import com.partners.entity.JsonResult;
import com.partners.view.entity.VAuditTrailLog;
import com.partners.weather.common.CommonResources;
import com.partners.weather.request.RequestHelper;
import com.partners.weather.service.IUserAuditTrailLogService;

@Controller
@RequestMapping("/audittrail")	
@UserPermission(value=UserPermissionEnum.USERAUDITTRATILLOG)
public class UserAuditLogController {
	
	@Resource
	IUserAuditTrailLogService userAuditTrailLogService;

	@RequestMapping
	@UserPermission(value=UserPermissionEnum.USERAUDITTRATILLOG)
	public String manage(HttpServletRequest request) {
		Adminuser adminuser = (Adminuser) request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
		request.setAttribute("Update", 0);
		request.setAttribute("Delete", 0);
		request.setAttribute("View", 0);
		if (adminuser.getPermissions().contains(UserPermissionEnum.PARAMETERGROUPINSERTANDUPDATE.getId())) {
			request.setAttribute("Update", 1);
		}
		if (adminuser.getPermissions().contains(UserPermissionEnum.PARAMETERGROUPDELETE.getId())) {
			request.setAttribute("Delete", 1);
		}
		if (adminuser.getPermissions().contains(UserPermissionEnum.TERMINALPARAMETERSELECT.getId())) {
			request.setAttribute("View", 1);
		}
		return "useraudittratiloglist";
	}
	@RequestMapping("/logs")
	@UserPermission(value=UserPermissionEnum.USERAUDITTRATILLOG)
	@ResponseBody
	public JsonResult logs(HttpServletRequest request) {
		VAuditTrailLog vAuditTrailLog=new VAuditTrailLog(RequestHelper.prepareRequest(request,true));
		List<AuditTrailLog> auditTrailLogs=userAuditTrailLogService.getAuditTratilLogs(vAuditTrailLog);
		auditTrailLogs= auditTrailLogs==null?new ArrayList<AuditTrailLog>(0):auditTrailLogs;
		int draw = StringUtils.isBlank(request.getParameter("draw")) ? 1 : Integer.valueOf(request.getParameter("draw"));
		int count = userAuditTrailLogService.getAuditTratilLogCount(vAuditTrailLog);
		JsonResult jsonResult = new JsonResult();
		jsonResult.setDraw(draw);
		jsonResult.setRecordsTotal(count);
		jsonResult.setRecordsFiltered(count);
		jsonResult.setData(auditTrailLogs.toArray());	
		return jsonResult;
	}
	@RequestMapping("/detail/{id}")
	@UserPermission(value=UserPermissionEnum.USERAUDITTRATILLOG)
	public String detail(HttpServletRequest request, @PathVariable("id") int id) {
		AuditTrailLog log=userAuditTrailLogService.getAuditTratilLog(id);
		request.setAttribute("detail",log==null?"没有详细信息":log.getRequestDetail().replace(CommonResources.NEWLINE, "<br/>"));
		return "auditlogdetail";
	}
}
