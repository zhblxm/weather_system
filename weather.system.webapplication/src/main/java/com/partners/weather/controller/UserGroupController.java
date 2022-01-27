package com.partners.weather.controller;

import java.util.ArrayList;
import java.util.Arrays;
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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.partners.annotation.UserAction;
import com.partners.annotation.UserPermission;
import com.partners.annotation.UserPermissionEnum;
import com.partners.entity.Adminuser;
import com.partners.entity.JsonResult;
import com.partners.entity.Permission;
import com.partners.entity.Usergroup;
import com.partners.entity.Weatherstation;
import com.partners.entity.Weatherstationcategory;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VUserGroup;
import com.partners.weather.common.CommonResources;
import com.partners.weather.encrypt.HexUtil;
import com.partners.weather.request.RequestHelper;
import com.partners.weather.service.IPermissionService;
import com.partners.weather.service.IWeatherStationService;

@Controller
@RequestMapping("/UserGroup")
@UserPermission(value = UserPermissionEnum.USERGUOUP)
public class UserGroupController {
	@Resource
	IPermissionService permissionService;
	@Autowired
	IWeatherStationService weatherStationService;
	
	@RequestMapping
	@UserPermission(value = UserPermissionEnum.USERGUOUPSELECT)
	@UserAction(Action = UserPermissionEnum.USERGUOUP, Description = "查询用户组")
	public String manage(HttpServletRequest request) {
		Adminuser adminuser = (Adminuser) request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
		request.setAttribute("Update", 0);
		request.setAttribute("Delete", 0);
		if (adminuser.getPermissions().contains(UserPermissionEnum.USERGUOUPINSERTANDUPDATE.getId())) {
			request.setAttribute("Update", 1);
		}
		if (adminuser.getPermissions().contains(UserPermissionEnum.USERGUOUPDELETE.getId())) {
			request.setAttribute("Delete", 1);
		}
		return "usergrouplist";
	}

	@RequestMapping("/groups")
	@UserPermission(value = UserPermissionEnum.USERGUOUPSELECT)
	@UserAction(Action = UserPermissionEnum.USERGUOUP, Description = "查询用户组")
	@ResponseBody
	public JsonResult grouplist(HttpServletRequest request) {
		VUserGroup vUserGroup = new VUserGroup(RequestHelper.prepareRequest(request,true));
		ArrayList<Usergroup> usergroups = permissionService.getUserGroups(vUserGroup);
		for (Usergroup usergroup : usergroups) {
			usergroup.setGroupUniqueId(HexUtil.IntToHex(usergroup.getGroupId()));
			usergroup.setGroupId(0);
		}
		usergroups = usergroups == null ? new ArrayList<Usergroup>(0) : usergroups;
		int draw = StringUtils.isBlank(request.getParameter("draw")) ? 1 : Integer.valueOf(request.getParameter("draw"));
		int count = permissionService.getUserGroupCount(vUserGroup);
		JsonResult jsonResult = new JsonResult();
		jsonResult.setDraw(draw);
		jsonResult.setRecordsTotal(count);
		jsonResult.setRecordsFiltered(count);
		jsonResult.setData(usergroups.toArray());
		return jsonResult;
	}

	@RequestMapping("/Add")
	@UserPermission(value = UserPermissionEnum.USERGUOUPINSERTANDUPDATE)
	@UserAction(Action = UserPermissionEnum.USERGUOUP, Description = "添加和更新用户组管理")
	public String Add(HttpServletRequest request) {
		ArrayList<Permission> permissions = permissionService.getPermissions();
		List<Permission> parentPermissions = new ArrayList<>(permissions.size());
		List<Permission> childPermissions = new ArrayList<>(permissions.size());
		for (Permission permission : permissions) {
			if (permission.getParentId() == 0 && !permission.isHasChild()) {
				parentPermissions.add(permission);
			} else {
				childPermissions.add(permission);
			}
		}
		List<Weatherstationcategory> categories= weatherStationService.getAllWeatherStationCategory();
		request.setAttribute("Categories", weatherStationService.filterWeatherStationCategorys(categories, "all",true));
		request.setAttribute("ParentPermissions", parentPermissions);
		request.setAttribute("ChildPermissions", childPermissions);
		return "usergroup";
	}

	@RequestMapping("/Update/{groupUniqueId}")
	@UserPermission(value = UserPermissionEnum.USERGUOUPINSERTANDUPDATE)
	@UserAction(Action = UserPermissionEnum.USERGUOUP, Description = "添加和更新用户组管理")
	public String Update(HttpServletRequest request, @PathVariable("groupUniqueId") String groupUniqueId) throws Exception {
		int groupId = HexUtil.HexToInt(groupUniqueId);
		Usergroup usergroup = permissionService.getUserGroup(groupId);
		if (usergroup == null) {
			throw new Exception("用户组不存在");
		}
		request.setAttribute("UserGroupPermissions", permissionService.getGroupPermissions(usergroup.getGroupId()));
		usergroup.setGroupUniqueId(HexUtil.IntToHex(usergroup.getGroupId()));
		usergroup.setGroupId(0);
		request.setAttribute("UserGroup", usergroup);
		ArrayList<Permission> permissions = permissionService.getPermissions();
		List<Permission> parentPermissions = new ArrayList<>(permissions.size());
		List<Permission> childPermissions = new ArrayList<>(permissions.size());
		for (Permission permission : permissions) {
			if (permission.getParentId() == 0 && !permission.isHasChild()) {
				parentPermissions.add(permission);
			} else {
				childPermissions.add(permission);
			}
		}
		request.setAttribute("UserCategories", usergroup.getStationGroup().split(","));
		request.setAttribute("UserStations", usergroup.getStation().split(","));
		List<Weatherstationcategory> categories= weatherStationService.getAllWeatherStationCategory();
		request.setAttribute("Categories", weatherStationService.filterWeatherStationCategorys(categories, "all",true));
		request.setAttribute("ParentPermissions", parentPermissions);
		request.setAttribute("ChildPermissions", childPermissions);
		return "usergroup";
	}

	@RequestMapping("/AsynSave")
	@UserPermission(value = UserPermissionEnum.USERGUOUPINSERTANDUPDATE)
	@UserAction(Action = UserPermissionEnum.USERGUOUP, Description = "添加和更新用户组管理")
	@ResponseBody
	public ResponseMsg SaveUserGroup(@RequestBody VUserGroup userGroup) {
		ResponseMsg responseMsg = new ResponseMsg();
		if (StringUtils.isBlank(userGroup.getGroupName())) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("用户组名称不能为空。");
			return responseMsg;
		}
		Usergroup group = new Usergroup();
		int groupId = 0;
		if (!StringUtils.isBlank(userGroup.getGroupUniqueId())) {
			groupId = HexUtil.HexToInt(userGroup.getGroupUniqueId());
		} else {
			groupId = permissionService.getMaxId();
		}
		group.setGroupId(groupId);
		group.setGroupName(userGroup.getGroupName());
		group.setStationGroup(userGroup.getWeatherCategory());
		group.setStation(userGroup.getWeatherStation());
		responseMsg = permissionService.insertUserGroup(group, userGroup.getPermissions());
		return responseMsg;
	}

	@RequestMapping("/Delete/{groupUniqueId}")
	@UserPermission(value = UserPermissionEnum.USERGUOUPDELETE)
	@UserAction(Action = UserPermissionEnum.USERGUOUP, Description = "删除用户组")
	@ResponseBody
	public ResponseMsg Delete(HttpServletRequest request, @PathVariable("groupUniqueId") String groupUniqueId) {
		ResponseMsg responseMsg = new ResponseMsg();
		int groupId = HexUtil.HexToInt(groupUniqueId);
		int count = this.permissionService.getUserGroupJoinAdminUserCount(groupId);
		if (count > 0) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("用户组被使用，不能删除。");
		}
		this.permissionService.delUserGroup(groupId);
		responseMsg.setStatusCode(0);
		responseMsg.setMessage("删除成功");
		return responseMsg;
	}
	@RequestMapping("/stations")
	@ResponseBody
	public JsonResult stations(HttpServletRequest request) {
		String categories=request.getParameter("categories");
		List<Weatherstation> weatherstations = null;
		if(StringUtils.isBlank(categories) || "all".equalsIgnoreCase(categories))
		{
			weatherstations=weatherStationService.getAllWeatherStation();
		}else {
			List<String> categoryArray=Arrays.asList(categories.split(","));	
			if(categoryArray.size()>0)
			{
				List<Integer> selectedCategories = Lists.transform(categoryArray, new Function<String, Integer>() {
					public Integer apply(String e) {
						return HexUtil.HexToInt(e.trim());
					}
				});
				weatherstations=weatherStationService.getWeatherStationsByCategory(selectedCategories);
			}
		}	
		JsonResult jsonResult = new JsonResult();
		jsonResult.setData(weatherStationService.filterWeatherStation(weatherstations, "all",true).toArray());
		return jsonResult;
	}
}
