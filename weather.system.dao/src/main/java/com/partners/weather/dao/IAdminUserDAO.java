package com.partners.weather.dao;

import com.partners.entity.Adminuser;
import com.partners.view.entity.VUser;

import java.util.List;

public interface IAdminUserDAO {

    Adminuser userLogin(String userName, String userPassword);

    List<Adminuser> getUsers(VUser vUser);

    Adminuser getUser(int userId);

    int checkUserExists(int userId, String userName);

    int insertUser(Adminuser adminuser);

    void updateUser(Adminuser adminuser);

    void updateUserPwd(Adminuser adminuser);

    void delUser(int userId);

    int getUserCount(VUser vUser);

}
