package com.partners.weather.controller;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.partners.entity.Adminuser;
import com.partners.entity.SystemOption;
import com.partners.entity.Usergroup;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VUser;
import com.partners.weather.common.CommonResources;
import com.partners.weather.common.RedisKey;
import com.partners.weather.service.IPermissionService;
import com.partners.weather.service.ISystemOptionService;
import com.partners.weather.service.IUserService;

@Controller
@RequestMapping("/login")
public class LoginController {

	@Resource
	IUserService userService;
	@Resource
	IPermissionService permissionService;
	@Resource
	ISystemOptionService systemOptionService;

	@RequestMapping
	public String home(HttpServletRequest request) throws ParseException {
		if (request.getSession() != null && request.getSession().getAttribute(CommonResources.ADMINUSERKEY) != null) {
			return "redirect:/home";
		}
		SystemOption systemOption = systemOptionService.getSystemOption(RedisKey.LOGINMAINTITLE);
		request.setAttribute("LoginMainTitle", systemOption.getOptionValue());
		request.setAttribute("Rember", "");
		Cookie[] cookies = request.getCookies();
		for (Cookie cookie : cookies) {
			if ("UserName".equals(cookie.getName())) {
				request.setAttribute("UserName", cookie.getValue());
				request.setAttribute("Rember", "on");
				break;
			}
		}
		request.setAttribute("name", "login.jpg");
		File backgrondFile = new File(request.getSession().getServletContext().getRealPath("/resources/images/background.png"));
		if (backgrondFile.exists()) {
			request.setAttribute("name", "background.png");
		}
		return "login";
	}

	@RequestMapping("/userLogin")
	@ResponseBody
	public ResponseMsg UserLogin(HttpServletRequest request, HttpServletResponse response) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		Adminuser adminuser = new Adminuser();
		adminuser.setUserName(request.getParameter("UserName"));
		adminuser.setUserPassword(request.getParameter("UserPwd"));
		String remberMe = request.getParameter("RemberMe");
		if (StringUtils.isEmpty(adminuser.getUserName()) || StringUtils.isEmpty(adminuser.getUserPassword())) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("用户名或密码不能为空");
		}
		Adminuser adminUser = this.userService.login(adminuser.getUserName(), adminuser.getUserPassword());
		if (adminUser != null) {
			adminUser.setPermissions(permissionService.getGroupPermissions(adminUser.getGroupId()));
			Usergroup usergroup = permissionService.getUserGroup(adminUser.getGroupId());
			adminUser.setWeatherStationGroup(usergroup.getStationGroup());
			adminUser.setWeatherStation(usergroup.getStation());
			request.getSession().setAttribute(CommonResources.ADMINUSERKEY, adminUser);
			VUser tempUser = new VUser();
			tempUser.setUniqueId(adminUser.getUniqueUserId());
			tempUser.setIsForceChangePwd(adminUser == null ? 0 : adminUser.getIsForceChangePwd());
			responseMsg.setMessageObject(tempUser);
			if ("Y".equalsIgnoreCase(remberMe)) {
				Cookie cookie = new Cookie("UserName", adminuser.getUserName());
				cookie.setMaxAge(7 * 24 * 60 * 60);// 有效期7天
				cookie.setPath("/");
				response.addCookie(cookie);
			}
		} else {
			responseMsg.setStatusCode(2);
			responseMsg.setMessage("用户不存在，请输入正确的用户名和密码！");
		}

		return responseMsg;
	}

	@RequestMapping("/logout")
	public void Logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
		request.getSession().invalidate();
		response.sendRedirect("/login");
	}
}
