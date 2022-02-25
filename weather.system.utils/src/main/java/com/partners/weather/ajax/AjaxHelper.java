package com.partners.weather.ajax;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

public class AjaxHelper {
	public static boolean IsAjaxRequest(HttpServletRequest request) {
		if (Objects.isNull(request)) {
			throw new NullPointerException("request");
		}
		return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
	}
}
