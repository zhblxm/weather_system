package com.partners.weather.aoplogging;

import java.lang.reflect.Method;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.partners.annotation.UserAction;
import com.partners.entity.Adminuser;
import com.partners.entity.AuditTrailLog;
import com.partners.weather.common.CommonResources;
import com.partners.weather.ip.IPUtil;
import com.partners.weather.service.IUserAuditTrailLogService;

@Aspect
@Component
public class UserActionAspect {
	private static final Logger logger = LoggerFactory.getLogger(UserActionAspect.class);
	@Autowired
	IUserAuditTrailLogService userAuditTrailLogService;
	ThreadLocal<Long> time = new ThreadLocal<Long>();
	ThreadLocal<String> tag = new ThreadLocal<String>();

	@Before("@annotation(com.partners.annotation.UserAction)")
	public void beforeExec(JoinPoint joinPoint) {
		MethodSignature ms = (MethodSignature) joinPoint.getSignature();
		Method method = ms.getMethod();
		StringBuilder argsBuilder = new StringBuilder();
		UserAction userAction = method.getAnnotation(UserAction.class);
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		argsBuilder.append("请求地址 :" + request.getRequestURL().toString());
		argsBuilder.append(CommonResources.NEWLINE+"请求方式 : " + request.getMethod());
		argsBuilder.append(CommonResources.NEWLINE+"IP地址: " + request.getRemoteAddr());
		argsBuilder.append(CommonResources.NEWLINE+"模块信息 : " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
		String argsString = Arrays.toString(joinPoint.getArgs());
		if (argsString.length() > 1000) {
			if (this.logger.isInfoEnabled()) {
				this.logger.info(argsString);
			}
		} else {
			argsBuilder.append(CommonResources.NEWLINE+"参数信息: " + argsString);
		}
		try {
			AuditTrailLog auditTrailLog = new AuditTrailLog();
			auditTrailLog.setRequestDetail(argsBuilder.toString());
			Object object = request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
			if (object instanceof Adminuser) {
				Adminuser adminUser = (Adminuser) object;
				auditTrailLog.setUserName(adminUser.getUserName());
			}
			if (userAction != null) {
				auditTrailLog.setUserActionId(userAction.Action().getId());
				auditTrailLog.setUserActionName(userAction.Action().getName());
				auditTrailLog.setDescription(userAction.Description());
			}
			auditTrailLog.setClientIP(IPUtil.getIPAddress(request));
			userAuditTrailLogService.delAuditTrailLog();
			userAuditTrailLogService.insertAuditTratilLog(auditTrailLog);
		} catch (Exception e) {
			logger.error("Error in {}", e);
		}
	}
}
