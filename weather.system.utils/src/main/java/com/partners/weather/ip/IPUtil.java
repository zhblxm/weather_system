package com.partners.weather.ip;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

public class IPUtil {
    public static String getIPAddress(HttpServletRequest request) throws Exception {
        String ip = request.getHeader("X-Real-IP");
        if (!StringUtils.isBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        String forwardIp = request.getHeader("X-Forwarded-For");
        if (StringUtils.isBlank(forwardIp) || "unknown".equalsIgnoreCase(forwardIp)) {
            return request.getRemoteAddr();
        }
        return forwardIp.indexOf(',') != -1 ? forwardIp.substring(0, forwardIp.indexOf(',')) : forwardIp;

    }
}
