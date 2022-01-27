package com.partners.weather.service;

import java.util.ArrayList;
import java.util.List;

import com.partners.entity.Permission;
import com.partners.entity.Usergroup;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VUserGroup;

public interface IPermissionService {
	public ArrayList<Permission> getPermissions();
	public ResponseMsg insertUserGroup(Usergroup usergroup,int[] permissions);
	public int getMaxId();
	public ArrayList<Usergroup> getUserGroups(VUserGroup vuserGroup);
	public Usergroup getUserGroup(int groupId);
	public int getUserGroupJoinAdminUserCount(int groupId);
	public void delUserGroup(int groupId);
	public List<Integer> getGroupPermissions(int groupId);
	public ArrayList<Usergroup> getALLGroups();	
	public int getUserGroupCount(VUserGroup vuserGroup);
	public List<Permission> getUserNavPermissions(List<Integer> permissions);
}
