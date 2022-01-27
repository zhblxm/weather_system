package com.partners.weather.service;

import java.util.List;

import com.partners.entity.Adminuser;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VUser;

public interface IUserService {
	public Adminuser login(String userName, String userPassword);

	public List<Adminuser> getUsers(VUser vUser);

	public Adminuser getUser(int userId);

	public int checkUserExists(int userId, String userName);

	public ResponseMsg insertUser(Adminuser adminuser);

	public boolean updateUser(Adminuser adminuser);

	public boolean updateUserPwd(Adminuser adminuser);

	public boolean delUser(int userId);
	
	public int getUserCount(VUser vUser);
}
