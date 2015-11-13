package com.pxltd.util;

public class StringUtils {
	public static String join(String[] strings, String joinStr) {
		StringBuffer sb = new StringBuffer();
		
		for(int i = 0; i < strings.length; i++) {
			sb.append(strings[i]);
		
			if(i != strings.length - 1) {
				sb.append(joinStr);
			}
		}
		return sb.toString();
	}

}
