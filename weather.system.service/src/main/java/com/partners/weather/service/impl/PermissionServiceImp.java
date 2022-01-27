package com.partners.weather.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.partners.entity.Grouppermission;
import com.partners.entity.Permission;
import com.partners.entity.Usergroup;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VUserGroup;
import com.partners.weather.dao.IPermissionDAO;
import com.partners.weather.encrypt.HexUtil;
import com.partners.weather.service.IPermissionService;

@Service
@Transactional
public class PermissionServiceImp implements IPermissionService {
	private static final Logger logger = LoggerFactory.getLogger(PermissionServiceImp.class);
	@Autowired
	private IPermissionDAO permissionDAO;

	@Override
	public ArrayList<Permission> getPermissions() {
		ArrayList<Permission> permissions = null;
		try {
			permissions = permissionDAO.getPermissions();
			for (Permission permission : permissions) {
				if(permission.getParentId()==0)
				{
					for (Permission childPermission : permissions) {
						if(childPermission.getParentId()==permission.getPermissionId() )
						{
							permission.setHasChild(true);
							break;
						}
					}
				}
			}
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		permissions = permissions == null ? new ArrayList<Permission>(0) : permissions;
		return permissions;
	}

	@Override
	public ResponseMsg insertUserGroup(Usergroup usergroup, int[] permissions) {
		ResponseMsg responseMsg=new ResponseMsg();
		responseMsg.setStatusCode(0);
		try {
			int groupId = usergroup.getGroupId();	
			responseMsg.setMessageObject(HexUtil.IntToHex(groupId));
			int groupCount=permissionDAO.checkGroupExists(groupId,usergroup.getGroupName());
			if(groupCount>0)
			{
				responseMsg.setStatusCode(1);
				responseMsg.setMessage(usergroup.getGroupName()+"已经存在，请更换其它名字。");
				return responseMsg;
			}
			permissionDAO.insertUserGroup(usergroup);		
			if (groupId > 0 && permissions.length > 0) {
				List<Grouppermission> grouppermissions = new ArrayList<>(permissions.length);
				Grouppermission grouppermission;
				for (int permissionId : permissions) {
					grouppermission = new Grouppermission();
					grouppermission.setGroupId(groupId);
					grouppermission.setPermissionId(permissionId);
					grouppermissions.add(grouppermission);
				}
				permissionDAO.delUserGroupPermission(groupId);
				permissionDAO.batchInsertUserPermission(grouppermissions);
			}
		} catch (Exception exception) {
			responseMsg.setStatusCode(-1);
			responseMsg.setMessage(exception.getMessage());
			logger.error("Error in {}", exception);
		}
		return responseMsg;
	}

	@Override
	public int getMaxId() {
		int maxId = 0;
		try {
			maxId = permissionDAO.getMaxId();

		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		return maxId;
	}

	@Override
	public ArrayList<Usergroup> getUserGroups(VUserGroup vuserGroup) {
		ArrayList<Usergroup> usergroups = null;
		if (vuserGroup == null) {
			throw new NullPointerException("VUserGroup cannot be null.");
		}
		try {
			usergroups = permissionDAO.getUserGroups(vuserGroup);
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		return usergroups == null ? new ArrayList<Usergroup>(0) : usergroups;
	}

	@Override
	public Usergroup getUserGroup(int groupId) {
		Usergroup usergroup = null;
		try {
			usergroup = permissionDAO.getUserGroup(groupId);
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		return usergroup;
	}

	@Override
	public int getUserGroupJoinAdminUserCount(int groupId) {
		int count = 0;
		try {
			count = permissionDAO.getUserGroupJoinAdminUserCount(groupId);
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		return count;
	}

	@Override
	public void delUserGroup(int groupId) {
		try {
			permissionDAO.delUserGroup(groupId);
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
	}

	@Override
	public List<Integer> getGroupPermissions(int groupId) {
		List<Integer> userPermissions = null;
		try {
			userPermissions = permissionDAO.getGroupPermissions(groupId);
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		return userPermissions == null ? new ArrayList<Integer>(0) : userPermissions;
	}

	@Override
	public ArrayList<Usergroup> getALLGroups() {
		ArrayList<Usergroup> usergroups = null;
		try {
			usergroups = permissionDAO.getALLGroups();
			if(usergroups!=null && usergroups.size()>0)
			{
				for(Usergroup usergroup:usergroups){
					usergroup.setGroupUniqueId(HexUtil.IntToHex(usergroup.getGroupId()));
					usergroup.setGroupId(0);
				}
			}
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		return usergroups == null ? new ArrayList<Usergroup>(0) : usergroups;
	}

	@Override
	public int getUserGroupCount(VUserGroup vuserGroup) {
		int userGroupCount=0;
		try {
			userGroupCount = permissionDAO.getUserGroupCount(vuserGroup);
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		return userGroupCount;
	}

	@Override
	public List<Permission> getUserNavPermissions(List<Integer> permissions) {
		List<Permission> userPermissions = null;
		try {
			userPermissions = permissionDAO.getUserNavPermissions(permissions);
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		userPermissions = userPermissions == null ? new ArrayList<Permission>(0) : userPermissions;
		return userPermissions;
	}

}
