package com.partners.weather.pinyin;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PinyinUtil {
	public static String chineseToPinyin(String chinese) throws BadHanyuPinyinOutputFormatCombination {
		StringBuilder pingyinBuilder=new StringBuilder();
		char[] newChar = chinese.toCharArray();
		HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
		defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		for (char c : newChar) {
			if (c <=128) {
				pingyinBuilder.append(c);
				continue;
			}
			pingyinBuilder.append(PinyinHelper.toHanyuPinyinStringArray(c, defaultFormat)[0]);
		}
		return pingyinBuilder.toString();
	}
}
