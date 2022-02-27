package com.partners.weather.request;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.partners.view.entity.vBaseEntity;
import com.partners.weather.common.CommonResources;

public class RequestHelper {
	private static final Logger logger = LoggerFactory.getLogger(RequestHelper.class);
	public static vBaseEntity prepareRequest(HttpServletRequest request, boolean isCustomeSize) {
		if (Objects.isNull(request)) {
			throw new NullPointerException("request");
		}
		vBaseEntity entity = new vBaseEntity();
		String name = request.getParameter("search[value]");
		logger.info("Before of request name is {}",name);
		if (!StringUtils.isBlank(name)) {
			try {
				name = URLDecoder.decode(name, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.error(e.getMessage());
				name = "";
			}
		}
		logger.info("After of request name is {}",name);
		entity.setName(name);
		String orderField = "";
		if (!StringUtils.isBlank(request.getParameter("order[0][column]"))) {
			orderField = request.getParameter("columns[" + request.getParameter("order[0][column]") + "][name]");
		}
		String orderType = request.getParameter("order[0][dir]");
		int startIndex = StringUtils.isBlank(request.getParameter("start")) ? 0 : Integer.valueOf(request.getParameter("start").trim());
		entity.setStartIndex(startIndex);
		orderType = StringUtils.isBlank(orderType) ? "DESC" : orderType;
		orderField = StringUtils.isBlank(orderField) ? "DATE" : orderField;
		if (!"NAME".equalsIgnoreCase(orderField) && !"DATE".equalsIgnoreCase(orderField)) {
			orderField = "";
		}
		if (!"DESC".equalsIgnoreCase(orderType) && !"ASC".equalsIgnoreCase(orderType)) {
			orderType = "DESC";
		}
		entity.setOrderType(orderType);
		entity.setOrderField(orderField);
		entity.setSize(CommonResources.PageSize);
		if (!StringUtils.isBlank(request.getParameter("length")) && isCustomeSize) {
			entity.setSize(Integer.valueOf(request.getParameter("length").trim()));
		}
		entity.setId(0);
		Date now = new Date();
		entity.setCreateDate(new Timestamp(now.getTime()));
		return entity;
	}

	public static vBaseEntity prepareRequest(HttpServletRequest request) {
		if (request == null) {
			throw new NullPointerException("request");
		}
		return prepareRequest(request, false);
	}

}
