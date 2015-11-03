package com.pxltd.geodash;

import java.util.ResourceBundle;

public class GeoshapingServiceConfig {
	private static final String BUNDLE_NAME="resources/geoshaping";
	public static String BASE_SERVER = "BASE_SERVER";
	public static String BASE_URL = "BASE_URL";

	static {
		ResourceBundle config = ResourceBundle.getBundle(BUNDLE_NAME);
		BASE_SERVER = getConfigValue(config, BASE_SERVER);
		BASE_URL = BASE_SERVER + getConfigValue(config, BASE_URL);
	}

	private static String getConfigValue(ResourceBundle config, String key) {
		String value = null;
		if(config.containsKey(key)){
			value = config.getString(key);
		} 
		return value;
	}
}