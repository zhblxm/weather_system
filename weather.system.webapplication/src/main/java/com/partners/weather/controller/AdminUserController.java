package com.partners.weather.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
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
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VUser;
import com.partners.view.entity.VUserPassword;
import com.partners.weather.common.CommonResources;
import com.partners.weather.encrypt.HexUtil;
import com.partners.weather.encrypt.Md5Util;
import com.partners.weather.request.RequestHelper;
import com.partners.weather.service.IPermissionService;
import com.partners.weather.service.IUserService;

@Controller
@RequestMapping("/User")
@UserPermission(value = UserPermissionEnum.ADMINUSER)
public class AdminUserController {

	@Resource
	IUserService userService;
	@Resource
	IPermissionService permissionService;

	@RequestMapping("/Manage")
	@UserPermission(value = UserPermissionEnum.ADMINUSERSELECT)
	@UserAction(Action = UserPermissionEnum.ADMINUSER, Description = "查询用户")
	public String Manage(HttpServletRequest request) {
		Adminuser adminuser = (Adminuser) request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
		request.setAttribute("Update", 0);
		request.setAttribute("Delete", 0);
		request.setAttribute("Reset", 0);
		if (adminuser.getPermissions().contains(UserPermissionEnum.USERGUOUPINSERTANDUPDATE.getId())) {
			request.setAttribute("Update", 1);
		}
		if (adminuser.getPermissions().contains(UserPermissionEnum.USERGUOUPDELETE.getId())) {
			request.setAttribute("Delete", 1);
		}
		if (adminuser.getPermissions().contains(UserPermissionEnum.RESETPASSWORD.getId())) {
			request.setAttribute("Reset", 1);
		}
		return "userlist";
	}

	@RequestMapping("/managepwd/{userUniqueId}")
	@UserPermission(value = UserPermissionEnum.ALLOWALL)
	@UserAction(Action = UserPermissionEnum.ALLOWALL, Description = "强制修改密码")
	public String managepwd(HttpServletRequest request, @PathVariable("userUniqueId") String userUniqueId) {
		request.setAttribute("uid", userUniqueId);
		return "changepwd";
	}

	@RequestMapping("/forceChangePwd/{userUniqueId}")
	@UserPermission(value = UserPermissionEnum.ALLOWALL)
	@UserAction(Action = UserPermissionEnum.ALLOWALL, Description = "修改密码")
	@ResponseBody
	public ResponseMsg forceChangePwd(@PathVariable("userUniqueId") String userUniqueId, HttpServletRequest request) throws Exception {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		String userPassword = request.getParameter("UserPassword");
		String confirmPwd = request.getParameter("ConfirmPassword");
		if (StringUtils.isBlank(userPassword) || StringUtils.isBlank(userPassword) || !userPassword.equals(confirmPwd)) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("密码不能为空或两次输入的密码不同！");
			return responseMsg;
		}
		int userId = HexUtil.HexToInt(userUniqueId);
		Adminuser adminuser = userService.getUser(userId);
		if (adminuser == null) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("用户不存在");
			return responseMsg;
		}
		adminuser.setUserPassword(Md5Util.md5(userPassword));
		adminuser.setIsForceChangePwd((byte) 0);
		boolean blnUpdatePwdSuccessed = this.userService.updateUserPwd(adminuser);
		if (!blnUpdatePwdSuccessed) {
			responseMsg.setStatusCode(2);
			responseMsg.setMessage("修改密码失败。");
		}
		return responseMsg;
	}

	@RequestMapping("/users")
	@UserPermission(value = UserPermissionEnum.ADMINUSERSELECT)
	@UserAction(Action = UserPermissionEnum.ADMINUSER, Description = "查询用户")
	@ResponseBody
	public JsonResult userlist(HttpServletRequest request) {
		VUser vUser = new VUser(RequestHelper.prepareRequest(request,true));
		List<Adminuser> adminusers = userService.getUsers(vUser);
		for (Adminuser adminuser : adminusers) {
			adminuser.setUniqueUserId(HexUtil.IntToHex(adminuser.getUserId()));
			adminuser.setUserId(0);
			adminuser.setUniqueGroupId(HexUtil.IntToHex(adminuser.getGroupId()));
			adminuser.setGroupId(0);
		}
		adminusers = adminusers == null ? new ArrayList<Adminuser>(0) : adminusers;
		int draw = StringUtils.isBlank(request.getParameter("draw")) ? 1 : Integer.valueOf(request.getParameter("draw"));
		int count = userService.getUserCount(vUser);
		JsonResult jsonResult = new JsonResult();
		jsonResult.setDraw(draw);
		jsonResult.setRecordsTotal(count);
		jsonResult.setRecordsFiltered(count);
		jsonResult.setData(adminusers.toArray());
		return jsonResult;
	}

	@RequestMapping("/Add")
	@UserPermission(value = UserPermissionEnum.ADMINUSERINSERTANDUPDATE)
	@UserAction(Action = UserPermissionEnum.ADMINUSER, Description = "添加和更新用户")
	public String Add(HttpServletRequest request) {
		request.setAttribute("UserGroups", permissionService.getALLGroups());
		return "user";
	}

	@RequestMapping("/Update/{userUniqueId}")
	@UserPermission(value = UserPermissionEnum.ADMINUSERINSERTANDUPDATE)
	@UserAction(Action = UserPermissionEnum.ADMINUSER, Description = "添加和更新用户")
	public String Update(HttpServletRequest request, @PathVariable("userUniqueId") String userUniqueId) throws Exception {
		int userId = HexUtil.HexToInt(userUniqueId);
		Adminuser adminuser = userService.getUser(userId);
		if (adminuser == null) {
			throw new Exception("用户不存在");
		}
		adminuser.setUniqueUserId(HexUtil.IntToHex(adminuser.getUserId()));
		adminuser.setUserId(0);
		adminuser.setUniqueGroupId(HexUtil.IntToHex(adminuser.getGroupId()));
		adminuser.setGroupId(0);
		request.setAttribute("User", adminuser);
		request.setAttribute("UserGroups", permissionService.getALLGroups());
		return "user";
	}
	@RequestMapping("/manageresetpwd/{userUniqueId}")
	@UserPermission(value = UserPermissionEnum.RESETPASSWORD)
	@UserAction(Action = UserPermissionEnum.RESETPASSWORD, Description = "修改密码")
	public String manageresetpwd(HttpServletRequest request, @PathVariable("userUniqueId") String userUniqueId) {
		request.setAttribute("uid", userUniqueId);
		return "resetpwd";
	}
	@RequestMapping("/resetpwd/{userUniqueId}")
	@UserPermission(value = UserPermissionEnum.RESETPASSWORD)
	@UserAction(Action = UserPermissionEnum.RESETPASSWORD, Description = "修改密码")
	@ResponseBody
	public ResponseMsg resetpwd(@PathVariable("userUniqueId") String userUniqueId, @RequestBody VUserPassword vUserPassword) throws Exception {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		int userId = HexUtil.HexToInt(userUniqueId);
		Adminuser adminuser = userService.getUser(userId);
		if (adminuser == null) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("用户不存在");
			return responseMsg;
		}
		if (!adminuser.getUserPassword().equals(Md5Util.md5(vUserPassword.getOriginPassowrd()))) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("原始密码输入错误！");
			return responseMsg;
		}
		adminuser.setUserPassword(Md5Util.md5(vUserPassword.getNewPassword()));
		adminuser.setIsForceChangePwd(vUserPassword.getIsForcePwd());
		boolean blnUpdatePwdSuccessed = this.userService.updateUserPwd(adminuser);
		if (!blnUpdatePwdSuccessed) {
			responseMsg.setStatusCode(2);
			responseMsg.setMessage("修改密码失败。");
		}
		return responseMsg;
	}

	@RequestMapping("/AsynSave")
	@UserPermission(value = UserPermissionEnum.ADMINUSERINSERTANDUPDATE)
	@UserAction(Action = UserPermissionEnum.ADMINUSER, Description = "添加和更新用户")
	@ResponseBody
	public ResponseMsg SaveUser(@RequestBody Adminuser adminuser) {
		ResponseMsg responseMsg = new ResponseMsg();
		if (StringUtils.isBlank(adminuser.getUserName())) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("用户名不能为空。");
			return responseMsg;
		}
		int groupId = 0, userId = 0;
		if (!StringUtils.isBlank(adminuser.getUniqueUserId())) {
			userId = HexUtil.HexToInt(adminuser.getUniqueUserId());
		} else {
			if (StringUtils.isBlank(adminuser.getUserPassword())) {
				responseMsg.setStatusCode(1);
				responseMsg.setMessage("密码不能为空。");
				return responseMsg;
			}
		}
		if (!StringUtils.isBlank(adminuser.getUniqueGroupId())) {
			groupId = HexUtil.HexToInt(adminuser.getUniqueGroupId());
		}
		adminuser.setGroupId(groupId);
		adminuser.setUserId(userId);
		adminuser.setUserPassword(Md5Util.md5(adminuser.getUserPassword()));
		responseMsg = this.userService.insertUser(adminuser);
		return responseMsg;
	}

	@RequestMapping("/Delete/{userUniqueId}")
	@UserPermission(value = UserPermissionEnum.ADMINUSERDELETE)
	@UserAction(Action = UserPermissionEnum.ADMINUSER, Description = "删除用户")
	@ResponseBody
	public ResponseMsg Delete(HttpServletRequest request, @PathVariable("userUniqueId") String userUniqueId) {
		ResponseMsg responseMsg = new ResponseMsg();
		int userId = HexUtil.HexToInt(userUniqueId);
		this.userService.delUser(userId);
		responseMsg.setStatusCode(0);
		responseMsg.setMessage("删除成功");
		return responseMsg;
	}

}
