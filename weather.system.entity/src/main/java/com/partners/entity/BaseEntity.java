package com.partners.entity;

import java.sql.Timestamp;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.partners.weather.common.CommonResources;

public class BaseEntity {
	private Date now = new Date();
	private Timestamp createDate = new Timestamp(now.getTime());
	private String createUser;
	private Timestamp lastUpdateDate = new Timestamp(now.getTime());
	private String lastUpdateUser ;
	private RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}

	public String getCreateUser() {
		if (StringUtils.isBlank(createUser) && requestAttributes != null) {
			ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
			HttpServletRequest request=servletRequestAttributes.getRequest();
			if(request!=null)
			{
				HttpSession httpSession=request.getSession();
				if(httpSession!=null && httpSession.getAttribute(CommonResources.ADMINUSERKEY)!=null)
				{
					Adminuser adminuser=(Adminuser) httpSession.getAttribute(CommonResources.ADMINUSERKEY);
					createUser=adminuser.getUserName();
				}
			}
		}
		if(StringUtils.isBlank(createUser))
		{
			createUser="Anonymou User";
		}
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	public Timestamp getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(Timestamp lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	public String getLastUpdateUser() {
		if (StringUtils.isBlank(lastUpdateUser) && !StringUtils.isBlank(createUser)) {
			lastUpdateUser=createUser;
		}
		if (StringUtils.isBlank(lastUpdateUser) && requestAttributes != null) {
			ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
			HttpServletRequest request=servletRequestAttributes.getRequest();
			if(request!=null)
			{
				HttpSession httpSession=request.getSession();
				if(httpSession!=null && httpSession.getAttribute(CommonResources.ADMINUSERKEY)!=null)
				{
					Adminuser adminuser=(Adminuser) httpSession.getAttribute(CommonResources.ADMINUSERKEY);
					lastUpdateUser=adminuser.getUserName();
				}
			}
		}
		return lastUpdateUser;
	}

	public void setLastUpdateUser(String lastUpdateUser) {
		this.lastUpdateUser = lastUpdateUser;
	}
}
