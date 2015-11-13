package com.pxltd.service;

public class CacheKeyTime {
	private String key;
	private long time;

	public CacheKeyTime(String k) {
		key = k;
		time = currentTime();
	}

	public String getKey() {
		return key;
	}

	public long getTime() {
		return time;
	}
	
	public long age() {
		return currentTime() - time;
	}
	
	public static long currentTime() {
		return System.currentTimeMillis()/1000;
	}

}