package com.partners.weather.controller;

import com.google.common.base.Preconditions;
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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
        request.setAttribute("Update", adminuser.getPermissions().contains(UserPermissionEnum.USERGUOUPINSERTANDUPDATE.getId()) ? 1 : 0);
        request.setAttribute("Delete", adminuser.getPermissions().contains(UserPermissionEnum.USERGUOUPDELETE.getId()) ? 1 : 0);
        return "usergrouplist";
    }

    @RequestMapping("/groups")
    @UserPermission(value = UserPermissionEnum.USERGUOUPSELECT)
    @UserAction(Action = UserPermissionEnum.USERGUOUP, Description = "查询用户组")
    @ResponseBody
    public JsonResult grouplist(HttpServletRequest request) {
        VUserGroup vUserGroup = new VUserGroup(RequestHelper.prepareRequest(request, true));
        List<Usergroup> userGroups = permissionService.getUserGroups(vUserGroup);
        userGroups.forEach(e -> {
                    e.setGroupUniqueId(HexUtil.IntToHex(e.getGroupId()));
                    e.setGroupId(0);
                }
        );
        int draw = StringUtils.isBlank(request.getParameter("draw")) ? 1 : Integer.parseInt(request.getParameter("draw"));
        int count = permissionService.getUserGroupCount(vUserGroup);
        return JsonResult.builder().draw(draw).recordsTotal(count).recordsFiltered(count).data(userGroups.toArray()).build();
    }

    @RequestMapping("/Add")
    @UserPermission(value = UserPermissionEnum.USERGUOUPINSERTANDUPDATE)
    @UserAction(Action = UserPermissionEnum.USERGUOUP, Description = "添加和更新用户组管理")
    public String Add(HttpServletRequest request) {
        ArrayList<Permission> permissions = permissionService.getPermissions();
        List<Weatherstationcategory> categories = weatherStationService.getAllWeatherStationCategory();
        request.setAttribute("Categories", weatherStationService.filterWeatherStationCategorys(categories, "all", true));
        request.setAttribute("ParentPermissions", permissions.stream().filter(permission -> permission.getParentId() == 0 && !permission.isHasChild()).collect(Collectors.toList()));
        request.setAttribute("ChildPermissions", permissions.stream().filter(permission -> permission.getParentId() != 0 && permission.isHasChild()).collect(Collectors.toList()));
        return "usergroup";
    }

    @RequestMapping("/Update/{groupUniqueId}")
    @UserPermission(value = UserPermissionEnum.USERGUOUPINSERTANDUPDATE)
    @UserAction(Action = UserPermissionEnum.USERGUOUP, Description = "添加和更新用户组管理")
    public String Update(HttpServletRequest request, @PathVariable("groupUniqueId") String groupUniqueId) throws Exception {
        Usergroup usergroup = permissionService.getUserGroup(HexUtil.HexToInt(groupUniqueId));
        Preconditions.checkNotNull(usergroup, "用户组不存在");
        request.setAttribute("UserGroupPermissions", permissionService.getGroupPermissions(usergroup.getGroupId()));

        usergroup.setGroupUniqueId(HexUtil.IntToHex(usergroup.getGroupId()));
        usergroup.setGroupId(0);
        request.setAttribute("UserGroup", usergroup);

        ArrayList<Permission> permissions = permissionService.getPermissions();
        request.setAttribute("UserCategories", usergroup.getStationGroup().split(","));
        request.setAttribute("UserStations", usergroup.getStation().split(","));
        List<Weatherstationcategory> categories = weatherStationService.getAllWeatherStationCategory();
        request.setAttribute("Categories", weatherStationService.filterWeatherStationCategorys(categories, "all", true));
        request.setAttribute("ParentPermissions", permissions.stream().filter(permission -> permission.getParentId() == 0 && !permission.isHasChild()).collect(Collectors.toList()));
        request.setAttribute("ChildPermissions", permissions.stream().filter(permission -> permission.getParentId() != 0 && permission.isHasChild()).collect(Collectors.toList()));
        return "usergroup";
    }

    @RequestMapping("/AsynSave")
    @UserPermission(value = UserPermissionEnum.USERGUOUPINSERTANDUPDATE)
    @UserAction(Action = UserPermissionEnum.USERGUOUP, Description = "添加和更新用户组管理")
    @ResponseBody
    public ResponseMsg SaveUserGroup(@RequestBody VUserGroup userGroup) {
        if (StringUtils.isBlank(userGroup.getGroupName())) {
            return ResponseMsg.builder().statusCode(1).message("用户组名称不能为空。").build();
        }
        Usergroup group = new Usergroup();
        int groupId = StringUtils.isBlank(userGroup.getGroupUniqueId()) ? permissionService.getMaxId() : HexUtil.HexToInt(userGroup.getGroupUniqueId());
        ;
        group.setGroupId(groupId);
        group.setGroupName(userGroup.getGroupName());
        group.setStationGroup(userGroup.getWeatherCategory());
        group.setStation(userGroup.getWeatherStation());
        return permissionService.insertUserGroup(group, userGroup.getPermissions());
    }

    @RequestMapping("/Delete/{groupUniqueId}")
    @UserPermission(value = UserPermissionEnum.USERGUOUPDELETE)
    @UserAction(Action = UserPermissionEnum.USERGUOUP, Description = "删除用户组")
    @ResponseBody
    public ResponseMsg Delete(HttpServletRequest request, @PathVariable("groupUniqueId") String groupUniqueId) {
        int groupId = HexUtil.HexToInt(groupUniqueId);
        int count = this.permissionService.getUserGroupJoinAdminUserCount(groupId);
        Preconditions.checkState(count == 0, "用户组被使用，不能删除。");
        this.permissionService.delUserGroup(groupId);
        return ResponseMsg.builder().statusCode(0).message("删除成功").build();
    }

    @RequestMapping("/stations")
    @ResponseBody
    public JsonResult stations(HttpServletRequest request) {
        String categories = request.getParameter("categories");
        List<Weatherstation> weatherstations = null;
        if (StringUtils.isBlank(categories) || "all".equalsIgnoreCase(categories)) {
            weatherstations = weatherStationService.getAllWeatherStation();
        } else {
            List<String> categoryArray = Arrays.asList(categories.split(","));
            weatherstations = weatherStationService.getWeatherStationsByCategory(
                    Optional.of(categoryArray).orElse(Lists.newArrayListWithCapacity(0))
                            .stream().map(Integer::valueOf).collect(Collectors.toList()));
        }
        return JsonResult.builder().data(weatherStationService.filterWeatherStation(weatherstations, "all", true).toArray()).build();
    }
}
