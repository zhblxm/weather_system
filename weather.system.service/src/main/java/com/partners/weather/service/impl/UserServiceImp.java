package com.partners.weather.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.partners.entity.Adminuser;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VUser;
import com.partners.weather.dao.IAdminUserDAO;
import com.partners.weather.encrypt.HexUtil;
import com.partners.weather.encrypt.Md5Util;
import com.partners.weather.service.IUserService;

@Service
@Transactional
public class UserServiceImp implements IUserService {
	private static final Logger logger = LoggerFactory.getLogger(UserServiceImp.class);
	@Autowired
	private IAdminUserDAO adminUserDAO;

	@Override
	public Adminuser login(String userName, String userPassword) {
		Adminuser adminuser = null;
		try {
			adminuser = adminUserDAO.userLogin(userName, Md5Util.md5(userPassword));
			if(adminuser!=null)
			{
				adminuser.setUniqueUserId(HexUtil.IntToHex(adminuser.getUserId()));
			}
			// adminUserDAO.createNewTable("TerminalHistory_001_B30_2017");
		} catch (Exception e) {
			logger.error("Error in {}", e);
		}
		return adminuser;
	}

	@Override
	public List<Adminuser> getUsers(VUser vUser) {
		List<Adminuser> adminusers = null;
		try {
			adminusers = adminUserDAO.getUsers(vUser);
		} catch (Exception e) {
			logger.error("Error in {}", e);
		}
		return adminusers == null ? new ArrayList<Adminuser>(0) : adminusers;
	}

	@Override
	public Adminuser getUser(int userId) {
		Adminuser adminusers = null;
		try {
			adminusers = adminUserDAO.getUser(userId);
		} catch (Exception e) {
			logger.error("Error in {}", e);
		}
		return adminusers;
	}

	@Override
	public int checkUserExists(int userId, String userName) {
		int count = 0;
		try {
			count = adminUserDAO.checkUserExists(userId, userName);
		} catch (Exception e) {
			logger.error("Error in {}", e);
		}
		return count;
	}

	@Override
	public ResponseMsg insertUser(Adminuser adminuser) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		int userId = adminuser.getUserId();
		Adminuser adminuserTemp = null;
		int categpruCount = adminUserDAO.checkUserExists(userId, adminuser.getUserName());
		if (categpruCount > 0) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage(adminuser.getUserName() + "已经存在，请更换其它名字。");
			return responseMsg;
		}
		if (userId > 0) {
			adminuserTemp = adminUserDAO.getUser(userId);
		}
		if (adminuserTemp == null) {
			adminUserDAO.insertUser(adminuser);

		} else {
			adminUserDAO.updateUser(adminuser);
		}
		responseMsg.setMessageObject(HexUtil.IntToHex(adminuser.getUserId()));
		return responseMsg;
	}

	@Override
	public boolean updateUser(Adminuser adminuser) {
		boolean blnUpdateSuccessed = true;
		try {
			adminUserDAO.updateUser(adminuser);
		} catch (Exception e) {
			blnUpdateSuccessed = false;
			logger.error("Error in {}", e);
		}
		return blnUpdateSuccessed;
	}

	@Override
	public boolean updateUserPwd(Adminuser adminuser) {
		boolean blnUpdateSuccessed = true;
		try {
			adminUserDAO.updateUserPwd(adminuser);
		} catch (Exception e) {
			blnUpdateSuccessed = false;
			logger.error("Error in {}", e);
		}
		return blnUpdateSuccessed;

	}

	@Override
	public boolean delUser(int userId) {
		boolean blnDelSuccessed = true;
		try {
			adminUserDAO.delUser(userId);
		} catch (Exception e) {
			blnDelSuccessed = false;
			logger.error("Error in {}", e);
		}
		return blnDelSuccessed;

	}

	@Override
	public int getUserCount(VUser vUser) {
		int userCount=0;
		try {
			userCount = adminUserDAO.getUserCount(vUser);
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		return userCount;
	}

}
