package com.partners.weather.service;

import com.partners.entity.Adminuser;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VUser;

import java.util.List;

public interface IUserService {
    Adminuser login(String userName, String userPassword);

    List<Adminuser> getUsers(VUser vUser);

    Adminuser getUser(int userId);

    int checkUserExists(int userId, String userName);

    ResponseMsg insertUser(Adminuser adminuser);

    boolean updateUser(Adminuser adminuser);

    boolean updateUserPwd(Adminuser adminuser);

    boolean delUser(int userId);

    int getUserCount(VUser vUser);
}
