package com.partners.weather.service;

import java.util.ArrayList;
import java.util.List;

import com.partners.entity.Permission;
import com.partners.entity.Usergroup;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VUserGroup;

public interface IPermissionService {
    ArrayList<Permission> getPermissions();

    ResponseMsg insertUserGroup(Usergroup usergroup, int[] permissions);

    int getMaxId();

    ArrayList<Usergroup> getUserGroups(VUserGroup vuserGroup);

    Usergroup getUserGroup(int groupId);

    int getUserGroupJoinAdminUserCount(int groupId);

    void delUserGroup(int groupId);

    List<Integer> getGroupPermissions(int groupId);

    ArrayList<Usergroup> getALLGroups();

    int getUserGroupCount(VUserGroup vuserGroup);

    List<Permission> getUserNavPermissions(List<Integer> permissions);
}
