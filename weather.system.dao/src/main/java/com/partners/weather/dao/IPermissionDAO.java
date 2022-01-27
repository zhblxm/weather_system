package com.partners.weather.dao;

import java.util.ArrayList;
import java.util.List;

import com.partners.entity.Grouppermission;
import com.partners.entity.Permission;
import com.partners.entity.Usergroup;
import com.partners.view.entity.VUserGroup;

public interface IPermissionDAO {	
	public ArrayList<Permission> getPermissions();
	public int batchInsertUserPermission(List<Grouppermission> permissions);
	public int insertUserGroup(Usergroup usergroup);
	public int getMaxId();
	public ArrayList<Usergroup> getUserGroups(VUserGroup vuserGroup);
	public ArrayList<Usergroup> getALLGroups();	
	public Usergroup getUserGroup(int groupId);	
	public int getUserGroupJoinAdminUserCount(int groupId);
	public void delUserGroup(int groupId);
	public List<Integer> getGroupPermissions(int groupId);
	public int checkGroupExists(int groupId,String groupName);
	public void delUserGroupPermission(int groupId);	
	public int getUserGroupCount(VUserGroup vuserGroup);
	public List<Permission> getUserNavPermissions(List<Integer> permissions);
}

