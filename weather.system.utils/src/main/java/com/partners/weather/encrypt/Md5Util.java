package com.partners.weather.encrypt;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import org.apache.commons.lang3.StringUtils;

public class Md5Util {
	public static String md5(String text) {
		try {
			if(StringUtils.isBlank(text))
			{
				return text;
			}
			return getMD5(text.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String getMD5(byte[] source) {
		String s = null;
		char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(source);
			byte[] tmp = md.digest();

			char[] str = new char[32];

			int k = 0;
			for (int i = 0; i < 16; ++i) {
				byte byte0 = tmp[i];
				str[(k++)] = hexDigits[(byte0 >>> 4 & 0xF)];

				str[(k++)] = hexDigits[(byte0 & 0xF)];
			}
			s = new String(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}

}