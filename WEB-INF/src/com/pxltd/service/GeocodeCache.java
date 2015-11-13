package com.pxltd.service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;

import com.pxltd.geodash.GeodashConfig;
import com.pxltd.geodash.GeodashException;

public class GeocodeCache {
	private static HashMap<String, Object> cache = null;
	private static Deque<CacheKeyTime> times;
	private static boolean geocode = false;
	private static int maxEntries = 3000;
	private static int ttl = 60 * 60 * 24 * 10; // 10 days

	/***
	 * Set up caching, create new cache if one doesn't exist
	 * 
	 * @param gc
	 * @throws GeodashException
	 */
	private static void setUp() throws GeodashException {
		if (cache == null) {
			cache = new HashMap<String, Object>();
			times = new ArrayDeque<CacheKeyTime>(maxEntries);
		}
		
		maxEntries = Integer.parseInt(GeodashConfig.GEOCODE_CACHE_MAX_ENTRIES);
		ttl = Integer.parseInt(GeodashConfig.GEOCODE_CACHE_TTL);
		geocode = Boolean.parseBoolean(GeodashConfig.CACHE_GEOCODING);
		
		// Remove all expired
		CacheKeyTime kt = times.peekFirst();

		while (kt != null && kt.age() > ttl) {
			if (kt.age() > ttl) {
				removeOldest();
			} else {
				break;
			}

			kt = times.peekFirst();
		}

	}

	/***
	 * Debugging print out to stdout
	 * @throws GeodashException 
	 */
	public static void printStatus() throws GeodashException {
		System.out.println(status());
	}
	
	/***
	 * Debugging text
	 * @throws GeodashException 
	 */
	public static String status() throws GeodashException {
		setUp();
		StringBuilder status = new StringBuilder();
		status.append("Number of items: " + times.size() + "\n");
		
		for (CacheKeyTime k : times) {
			status.append(k.age() + "|" + k.getKey() + "\n");
		}

		return status.toString();
	}
	
	public static void clearCache() {
		synchronized (cache) {
			times.clear();
			cache.clear();
		}
	}

	/***
	 * Put a value in to the cache
	 * 
	 * @param gc
	 * @param key
	 * @param value
	 * @throws GeodashException
	 */
	public static void put(String key, Object value) throws GeodashException {
		setUp();
		
		if(!Boolean.parseBoolean(GeodashConfig.CACHE_GEOCODING)) {
			return;
		}

		CacheKeyTime kt = new CacheKeyTime(key);

		if(!cache.containsKey(key)) {
			synchronized (cache) {
				cache.put(key, value);
				times.add(kt);
				
				if(cache.size() > maxEntries) {
					removeOldest();
				}
			}
		}
		
	}

	/***
	 * Remove the oldest item in the cache
	 */
	public static void removeOldest() {
		CacheKeyTime lastKeyTime = times.pollFirst();

		synchronized (cache) {
			if (lastKeyTime != null) {
				cache.remove(lastKeyTime.getKey());
			}
		}
	}

	/**
	 * 
	 * @param gc
	 * @param key
	 * @return
	 * @throws GeodashException
	 */
	public static Object get(String key) throws GeodashException {
		setUp();
		if(!geocode) {
			return null;
		}

		Object value = null;
		if (cache != null) {
			value = cache.get(key);
		}

		return value;
	}

}
