package com.pxltd.geodash;

import java.util.ResourceBundle;

public class GeoshapingServiceConfig {
	private static final String BUNDLE_NAME = "resources/geoshaping";
	public static String BASE_SERVER = "BASE_SERVER";
	public static String CREATE_SHAPE_LAYER_BASE_URL = "CREATE_SHAPE_LAYER_BASE_URL";
	public static String LICENSE_CHECK_BASE_URL = "LICENSE_CHECK_BASE_URL";

	static {
		ResourceBundle config = ResourceBundle.getBundle(BUNDLE_NAME);
		BASE_SERVER = getConfigValue(config, BASE_SERVER);
		CREATE_SHAPE_LAYER_BASE_URL = BASE_SERVER + getConfigValue(config, CREATE_SHAPE_LAYER_BASE_URL);
		LICENSE_CHECK_BASE_URL = BASE_SERVER + getConfigValue(config, LICENSE_CHECK_BASE_URL);
	}

	private static String getConfigValue(ResourceBundle config, String key) {
		String value = null;
		if (config.containsKey(key)) {
			value = config.getString(key);
		}
		return value;
	}
}