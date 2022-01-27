package com.partners.weather.dao;

import java.util.List;
import com.partners.entity.Adminuser;
import com.partners.view.entity.VUser;

public interface IAdminUserDAO {

	public Adminuser userLogin(String userName, String userPassword);

	public List<Adminuser> getUsers(VUser vUser);

	public Adminuser getUser(int userId);

	public int checkUserExists(int userId, String userName);

	public int insertUser(Adminuser adminuser);

	public void updateUser(Adminuser adminuser);

	public void updateUserPwd(Adminuser adminuser);

	public void delUser(int userId);
	
	public int getUserCount(VUser vUser);
	
}
