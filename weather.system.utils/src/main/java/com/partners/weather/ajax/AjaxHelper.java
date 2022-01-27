package com.partners.weather.ajax;

import javax.servlet.http.HttpServletRequest;

public class AjaxHelper {
	public static boolean IsAjaxRequest(HttpServletRequest request) {
		if (request == null) {
			throw new NullPointerException("request");
		}
		return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
	}
}
