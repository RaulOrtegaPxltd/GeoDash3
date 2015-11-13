package com.pxltd.service;

import com.microstrategy.utils.log.Level;

public enum EnumGeoStatusCode {
	/**
	 * indicates that no errors occurred; the address was successfully parsed and 
	 * at least one geocode was returned. 
	 */
	OK(100), 
	
	/**
	 *  indicates that the geocode was successful but returned no results. This may 
	 *  occur if the geocode was passed a non-existent address or a latlng in a remote location.
	 */
	ZERO_RESULTS(101),
	
	/**
	 * indicates that you are over your quota.
	 */
	OVER_QUERY_LIMIT(110),
	
	/**
	 * indicates that your request was denied, generally because of lack of a sensor parameter.
	 */
	REQUEST_DENIED(111), 
	
	/**
	 * generally indicates that the query (address or latlng) is missing.
	 */
	INVALID_REQUEST(112), 
	
	/**
	 * This means the record has not been submitted for geocoding
	 */
	NOT_PROCESSED(102),
	
	/**
	 * Used when receiving an unexpected state from Google
	 */
	UNKNOWN_STATE(113) ; 
	
	private final int code;
	private EnumGeoStatusCode(int code) {
		this.code = code;
	}
	
	public int getCode(){
		return code;
	}
	
	/**
	 * Static method to return the correct enum.  In case google returns an
	 * unexpected value we will return Unknown state as the state.  This should
	 * be used in place of valueOf method.
	 * 
	 * @param sEnum - String enum name
	 * @return 
	 */
	public static EnumGeoStatusCode getEnum(String sEnum){
		EnumGeoStatusCode en;
		try{
			en = EnumGeoStatusCode.valueOf(sEnum.toUpperCase().trim());
		}catch(Exception e){
			Log.logger.logp(Level.WARNING, "EnumGeoStatusCode", "getEnum", "Received new status type:  " +sEnum);
			en = UNKNOWN_STATE;
		}		
		return en;
	}
}
