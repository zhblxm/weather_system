package com.partners.weather.encrypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

public class HexUtil {

	public static String IntToHex(int value) {
		byte[] result = new byte[4];
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = new ObjectOutputStream(bos);
			out.writeInt(value);
			out.close();
			result = bos.toByteArray();

			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Hex.encodeHexString(result);
	}

	public static int HexToInt(String value) {
		int result = 0;
		if (StringUtils.isBlank(value))
			return result;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(Hex.decodeHex(value.toCharArray()));
			ObjectInputStream ois = new ObjectInputStream(bis);
			result = ois.readInt();
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DecoderException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String StringToHex(String value) {
		String result = "";
		if (StringUtils.isBlank(value))
			return result;
		try {
			result = Hex.encodeHexString(value.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String HexToString(String value) {
		String result = "";
		if (StringUtils.isBlank(value))
			return result;
		try {
			result = new String(Hex.decodeHex(value.toCharArray()), "UTF-8");
		} catch (DecoderException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}
}
