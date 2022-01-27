package com.partners.weather.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.partners.annotation.UserAction;
import com.partners.annotation.UserPermission;
import com.partners.annotation.UserPermissionEnum;
import com.partners.entity.Adminuser;
import com.partners.entity.JsonResult;
import com.partners.entity.MessageNotice;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VMessageNotice;
import com.partners.weather.common.CommonResources;
import com.partners.weather.encrypt.HexUtil;
import com.partners.weather.request.RequestHelper;
import com.partners.weather.service.IMessageNoticeService;

@Controller
@RequestMapping("/messagenotice")
@UserPermission(value = UserPermissionEnum.NOTIFICATION)
public class MessageNoticeController {
	
	@Autowired
	IMessageNoticeService messageNoticeService;

	@RequestMapping("/manage")
	@UserPermission(value = UserPermissionEnum.NOTIFICATIONSELECT)
	@UserAction(Action = UserPermissionEnum.NOTIFICATION, Description = "查询通知")
	public String Manage(HttpServletRequest request) {
		Adminuser adminuser = (Adminuser) request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
		request.setAttribute("Update", 0);
		request.setAttribute("Delete", 0);
		if (adminuser.getPermissions().contains(UserPermissionEnum.NOTIFICATIONINSERTANDUPDATE.getId())) {
			request.setAttribute("Update", 1);
		}
		if (adminuser.getPermissions().contains(UserPermissionEnum.NOTIFICATIONDELETE.getId())) {
			request.setAttribute("Delete", 1);
		}
		return "messagenoticelist";
	}

	@RequestMapping("/messagenotices")
	@UserPermission(value = UserPermissionEnum.NOTIFICATIONSELECT)
	@UserAction(Action = UserPermissionEnum.NOTIFICATION, Description = "查询通知")
	@ResponseBody
	public JsonResult MessageNoticeList(HttpServletRequest request) {
		VMessageNotice vMessageNotice = new VMessageNotice(RequestHelper.prepareRequest(request,true));
		List<MessageNotice> messageNotices = messageNoticeService.getMessageNotices(vMessageNotice);
		for (MessageNotice messageNotice : messageNotices) {
			messageNotice.setUniqueMessageNoticeId(HexUtil.IntToHex(messageNotice.getMessageNoticeId()));
			messageNotice.setMessageNoticeId(0);
		}
		messageNotices = messageNotices == null ? new ArrayList<MessageNotice>(0) : messageNotices;
		int draw = StringUtils.isBlank(request.getParameter("draw")) ? 1 : Integer.valueOf(request.getParameter("draw"));
		int count = messageNoticeService.getMessageNoticeCount(vMessageNotice);
		JsonResult jsonResult = new JsonResult();
		jsonResult.setDraw(draw);
		jsonResult.setRecordsTotal(count);
		jsonResult.setRecordsFiltered(count);
		jsonResult.setData(messageNotices.toArray());
		return jsonResult;
	}

	@RequestMapping("/add")
	@UserPermission(value = UserPermissionEnum.NOTIFICATIONINSERTANDUPDATE)
	@UserAction(Action = UserPermissionEnum.NOTIFICATION, Description = "添加和更新通知")
	public String Add(HttpServletRequest request) {
		
		return "messagenotice";
	}

	@RequestMapping("/update/{messageNoticeUniqueId}")
	@UserPermission(value = UserPermissionEnum.NOTIFICATIONINSERTANDUPDATE)
	@UserAction(Action = UserPermissionEnum.NOTIFICATION, Description = "添加和更新用户")
	public String Update(HttpServletRequest request, @PathVariable("messageNoticeUniqueId") String messageNoticeUniqueId) throws Exception {
		int messageNoticeId = HexUtil.HexToInt(messageNoticeUniqueId);
		MessageNotice messageNotice = messageNoticeService.getMessageNotice(messageNoticeId);
		if (messageNotice == null) {
			throw new Exception("通知不存在");
		}
		messageNotice.setUniqueMessageNoticeId(HexUtil.IntToHex(messageNotice.getMessageNoticeId()));
		messageNotice.setMessageNoticeId(0);

		request.setAttribute("MessageNotice", messageNotice);
		return "messagenotice";
	}

	@RequestMapping("/asynsave")
	@UserPermission(value = UserPermissionEnum.NOTIFICATIONINSERTANDUPDATE)
	@UserAction(Action = UserPermissionEnum.NOTIFICATION, Description = "添加和更新通知")
	@ResponseBody
	public ResponseMsg SaveMessageNotice(@RequestBody MessageNotice messageNotice) {
		ResponseMsg responseMsg = new ResponseMsg();
		if (StringUtils.isBlank(messageNotice.getMessageNoticeDesc())) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("通知名称不能为空。");
			return responseMsg;
		}
		
		if (StringUtils.isBlank(messageNotice.getMessage())) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("通知信息不能为空。");
			return responseMsg;
		}
		
		int messageNoticeId = 0;
		if (!StringUtils.isBlank(messageNotice.getUniqueMessageNoticeId())) {
			messageNoticeId = HexUtil.HexToInt(messageNotice.getUniqueMessageNoticeId());
		} 
		
		messageNotice.setMessageNoticeId(messageNoticeId);
		responseMsg = messageNoticeService.insertMessageNotice(messageNotice);
		return responseMsg;
	}

	@RequestMapping("/delete/{messageNoticeUniqueId}")
	@UserPermission(value = UserPermissionEnum.NOTIFICATIONDELETE)
	@UserAction(Action = UserPermissionEnum.NOTIFICATION, Description = "删除用户")
	@ResponseBody
	public ResponseMsg Delete(HttpServletRequest request, @PathVariable("messageNoticeUniqueId") String messageNoticeUniqueId) {
		ResponseMsg responseMsg = new ResponseMsg();
		int messageNoticeId = HexUtil.HexToInt(messageNoticeUniqueId);
		this.messageNoticeService.delMessageNotice(messageNoticeId);
		responseMsg.setStatusCode(0);
		responseMsg.setMessage("删除成功");
		return responseMsg;
	}
}
