package com.partners.weather.interceptor;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.support.AopUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.alibaba.fastjson.JSON;
import com.partners.annotation.UserPermission;
import com.partners.annotation.UserPermissionEnum;
import com.partners.entity.Adminuser;
import com.partners.view.entity.ResponseMsg;
import com.partners.weather.ajax.AjaxHelper;
import com.partners.weather.common.CommonResources;

public class SecurityInterceptor extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (request.getSession() == null || request.getSession().getAttribute(CommonResources.ADMINUSERKEY) == null) {
			return this.Redirect(request, response);
		}
		request.setAttribute("logo", "logo.png");
		request.setAttribute("user", request.getSession().getAttribute(CommonResources.ADMINUSERKEY));
		String logoPath = request.getSession().getServletContext().getRealPath("/resources/images/logo_new.png");
		File logoFile = new File(logoPath);
		if (logoFile.exists()) {
			request.setAttribute("logo", "logo_new.png");
		}
		if (handler instanceof HandlerMethod) {
			Adminuser adminuser = (Adminuser) request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
			HandlerMethod method = (HandlerMethod) handler;
			Object bean = method.getBean();
			UserPermission classPermission = null;
			if (AopUtils.isAopProxy(bean)) {
				// AopUtils.isJdkDynamicProxy(bean)
				Field field = bean.getClass().getDeclaredField("CGLIB$CALLBACK_0");
				field.setAccessible(true);
				Object dynamicAdvisedInterceptor = field.get(bean);
				Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
				advised.setAccessible(true);
				bean = ((AdvisedSupport) advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget();
			}
			classPermission = bean.getClass().getAnnotation(UserPermission.class);
			UserPermission methodPremission = method.getMethodAnnotation(UserPermission.class);
			int permissionValue = UserPermissionEnum.ALLOWALL.getId();
			if (methodPremission != null) {
				permissionValue = methodPremission.value().getId();
			} else if (classPermission != null) {
				permissionValue = classPermission.value().getId();
			}
			// 验证用户权限
			if (permissionValue == UserPermissionEnum.ALLOWALL.getId() || adminuser.getPermissions().contains(permissionValue)) {
				return true;
			}
			return this.Redirect(request, response);
		}
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		// after action
	}

	private boolean Redirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (AjaxHelper.IsAjaxRequest(request)) {
			ResponseMsg responseMsg = new ResponseMsg();
			responseMsg.setStatusCode(-1);
			responseMsg.setMessage("请登录或没有权限!");
			response.setHeader("content-type", "text/html;charset=UTF-8");
			OutputStream out = response.getOutputStream();
			out.write(JSON.toJSONString(responseMsg).getBytes("UTF-8"));
		} else {
			response.sendRedirect("/login");
		}
		return false;
	}

}