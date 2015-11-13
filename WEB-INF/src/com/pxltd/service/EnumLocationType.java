package com.pxltd.service;

import com.microstrategy.utils.log.Level;

public enum EnumLocationType {
	/**
	 * street_address indicates a precise street address
	 */
	STREET_ADDRESS,
	
	/**
	 * route indicates a named route (such as "US 101").
	 */
	ROUTE,
	
	/**
	 * intersection indicates a major intersection, usually of two major roads.
	 */
	INTERSECTION,
	
	/**
	 * political indicates a political entity. Usually, this type indicates a polygon of some civil administration.
	 */
	POLITICAL,
	
	/**
	 * country indicates the national political entity, and is typically the highest order type returned by the Geocoder.
	 */
	COUNTRY,
	
	/**
	 * administrative_area_level_1 indicates a first-order civil entity below the country level. 
	 * Within the United States, these administrative levels are states. Not all nations exhibit these administrative levels.
	 */
	ADMINISTRATIVE_AREA_LEVEL_1,
	
	/**
	 * administrative_area_level_2 indicates a second-order civil entity below the country level. 
	 * Within the United States, these administrative levels are counties. Not all nations exhibit these administrative levels.
	 */
	ADMINISTRATIVE_AREA_LEVEL_2,
	
	/**
	 * administrative_area_level_3 indicates a third-order civil entity below the country level. 
	 * This type indicates a minor civil division. Not all nations exhibit these administrative levels.
	 * In the US this shows up as a two digit code in some areas.
	 */
	ADMINISTRATIVE_AREA_LEVEL_3,
	
	/**
	 * colloquial_area indicates a commonly-used alternative name for the entity.
	 */
	COLLOQUIAL_AREA,
	
	/**
	 * locality indicates an incorporated city or town political entity.
	 */
	LOCALITY,
	
	/**
	 * sublocality indicates an first-order civil entity below a locality
	 */
	SUBLOCALITY,
	
	/**
	 * neighborhood indicates a named neighborhood.  Nob Hill in san francisco is an example.
	 */
	NEIGHBORHOOD,
	
	/**
	 * premise indicates a named location, usually a building or collection of 
	 * buildings with a common name
	 */
	PREMISE,
	
	/**
	 * subpremise indicates a first-order entity below a named location, usually a 
	 * singular building within a collection of buildings with a common name
	 */
	SUBPREMISE,
	
	/**
	 * postal_code indicates a postal code as used to address postal mail within the country.
	 */
	POSTAL_CODE,
	
	/**
	 * natural_feature indicates a prominent natural feature.
	 */
	NATURAL_FEATURE,
	
	/**
	 * airport indicates an airport.
	 */
	AIRPORT,
	
	/**
	 * park indicates a named park.
	 */
	PARK,
	
	/**
	 * point_of_interest
	 */
	POINT_OF_INTEREST,
	
	/**
	 * post_box indicates a specific postal box.
	 */
	POST_BOX,
	
	/**
	 * street_number indicates the precise street number.
	 */
	STREET_NUMBER,
	
	/**
	 * floor indicates the floor of a building address.
	 */
	FLOOR,
	
	/**
	 * room indicates the room of a building address.
	 */
	ROOM,
	
	/**
	 * train_station indicates a train station.
	 */
	TRAIN_STATION,
	
	/**
	 * unknown used in cases Google returns something new or unexpected
	 */
	UNKNOWN;
	
	/**
	 * Static method to return the correct enum.  In case google returns an
	 * unexpected value we will return Unknown location as the location type.  
	 * This should be used in place of valueOf method.
	 * 
	 * @param sEnum - String enum name
	 * @return 
	 */
	public static EnumLocationType getEnum(String sEnum){
		EnumLocationType en;
		try{
			en = EnumLocationType.valueOf(sEnum.toUpperCase().trim());
		}catch(Exception e){
			Log.logger.logp(Level.INFO, "EnumLocationType", "getEnum", "Received new location type:  " +sEnum);
			en = UNKNOWN;
		}		
		return en;
	}
	
		
}
