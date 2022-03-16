package com.partners.weather.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import com.partners.entity.Grouppermission;
import com.partners.entity.Permission;
import com.partners.entity.Usergroup;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VUserGroup;
import com.partners.weather.dao.IPermissionDAO;
import com.partners.weather.encrypt.HexUtil;
import com.partners.weather.service.IPermissionService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class PermissionServiceImp implements IPermissionService {
    @Autowired
    private IPermissionDAO permissionDAO;

    @Override
    public ArrayList<Permission> getPermissions() {
        try {
            List<Permission> permissions = permissionDAO.getPermissions();
            permissions.stream().filter(p -> p.getPermissionId() == 0).collect(Collectors.toList()).forEach(
                    p -> {
                        permissions.stream().filter(e -> e.getParentId() == p.getPermissionId()).forEach(
                                ph -> ph.setHasChild(Boolean.TRUE)
                        );
                    }
            );
            return (ArrayList<Permission>) permissions;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Lists.newArrayListWithCapacity(0);
    }

    @Override
    public ResponseMsg insertUserGroup(Usergroup usergroup, int[] permissions) {
        if (ArrayUtils.isNotEmpty(permissions)) {
            return ResponseMsg.builder().statusCode(1).build();
        }
        int groupCount = permissionDAO.checkGroupExists(usergroup.getGroupId(), usergroup.getGroupName());
        if (groupCount > 0) {
            return ResponseMsg.builder().statusCode(1).messageObject(HexUtil.IntToHex(usergroup.getGroupId())).message(usergroup.getGroupName() + "已经存在，请更换其它名字。").build();
        }
        try {
            permissionDAO.insertUserGroup(usergroup);
            List<Grouppermission> groupPermissions = Lists.newArrayListWithCapacity(permissions.length);
            Grouppermission grouppermission;
            for (int permissionId : permissions) {
                grouppermission = new Grouppermission();
                grouppermission.setGroupId(usergroup.getGroupId());
                grouppermission.setPermissionId(permissionId);
                groupPermissions.add(grouppermission);
            }
            permissionDAO.delUserGroupPermission(usergroup.getGroupId());
            permissionDAO.batchInsertUserPermission(groupPermissions);
            return ResponseMsg.builder().statusCode(0).build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return ResponseMsg.builder().statusCode(1).build();
    }

    @Override
    public int getMaxId() {
        return permissionDAO.getMaxId();
    }

    @Override
    public ArrayList<Usergroup> getUserGroups(VUserGroup vuserGroup) {
        Preconditions.checkNotNull(vuserGroup, "VUserGroup cannot be null.");
        try {
            return permissionDAO.getUserGroups(vuserGroup);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Lists.newArrayListWithCapacity(0);
    }

    @Override
    public Usergroup getUserGroup(int groupId) {
        return permissionDAO.getUserGroup(groupId);
    }

    @Override
    public int getUserGroupJoinAdminUserCount(int groupId) {
        return permissionDAO.getUserGroupJoinAdminUserCount(groupId);
    }

    @Override
    public void delUserGroup(int groupId) {
        permissionDAO.delUserGroup(groupId);
    }

    @Override
    public List<Integer> getGroupPermissions(int groupId) {
        return permissionDAO.getGroupPermissions(groupId);
    }

    @Override
    public ArrayList<Usergroup> getALLGroups() {
        try {
            ArrayList<Usergroup> userGroups = permissionDAO.getALLGroups();
            if (CollectionUtils.isEmpty(userGroups)) {
                return Lists.newArrayListWithCapacity(0);
            }
            userGroups.forEach(e -> {
                e.setGroupUniqueId(HexUtil.IntToHex(e.getGroupId()));
                e.setGroupId(0);
            });
            return userGroups;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Lists.newArrayListWithCapacity(0);
    }

    @Override
    public int getUserGroupCount(VUserGroup vuserGroup) {
        return permissionDAO.getUserGroupCount(vuserGroup);
    }

    @Override
    public List<Permission> getUserNavPermissions(List<Integer> permissions) {
        return permissionDAO.getUserNavPermissions(permissions);
    }

}
