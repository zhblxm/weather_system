package com.partners.weather.dao;

import java.util.ArrayList;
import java.util.List;

import com.partners.entity.Grouppermission;
import com.partners.entity.Permission;
import com.partners.entity.Usergroup;
import com.partners.view.entity.VUserGroup;

public interface IPermissionDAO {
	ArrayList<Permission> getPermissions();
	int batchInsertUserPermission(List<Grouppermission> permissions);
	int insertUserGroup(Usergroup usergroup);
	int getMaxId();
	ArrayList<Usergroup> getUserGroups(VUserGroup vuserGroup);
	ArrayList<Usergroup> getALLGroups();	
	Usergroup getUserGroup(int groupId);	
	int getUserGroupJoinAdminUserCount(int groupId);
	void delUserGroup(int groupId);
	List<Integer> getGroupPermissions(int groupId);
	int checkGroupExists(int groupId,String groupName);
	void delUserGroupPermission(int groupId);	
	int getUserGroupCount(VUserGroup vuserGroup);
	List<Permission> getUserNavPermissions(List<Integer> permissions);
}

