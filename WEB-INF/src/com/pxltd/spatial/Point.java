package com.pxltd.spatial;

import java.io.Serializable;

import com.microstrategy.utils.log.Level;
import com.microstrategy.web.app.tasks.architect.json.JSONArray;
import com.microstrategy.web.app.tasks.architect.json.JSONException;
import com.microstrategy.web.app.tasks.architect.json.JSONObject;
import com.pxltd.service.EnumGeoStatusCode;
import com.pxltd.service.EnumLocationType;

public class Point implements Serializable {

	private static final long serialVersionUID = 1L;
	private double latitude;
	private double longitude;
	private EnumLocationType locationType;
	private EnumGeoStatusCode state = EnumGeoStatusCode.NOT_PROCESSED;
	private String address;
	private int attempts = 0;
	private String color;
	private double size = 0;

	public Point(String addrs) {
		address = addrs;
	}

	/**
	 * This constructor is made available in the cases where the data is already Geocoded.
	 */
	public Point() {
		address = " ";
	}

	public boolean isGeocoded() {
		if (getState() == EnumGeoStatusCode.OK) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude
	 *            the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude
	 *            the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @return the locationType
	 */
	public EnumLocationType getLocationType() {
		return locationType;
	}

	/**
	 * @param locationType
	 *            the alocationType to set
	 */
	public void setLocationType(EnumLocationType locationType) {
		this.locationType = locationType;
	}

	/**
	 * @return state
	 */
	public EnumGeoStatusCode getState() {
		return state;
	}

	/**
	 * Anytime the state is set the number of attempts is autoincremented.
	 * 
	 * @param st
	 */
	public void setState(EnumGeoStatusCode st) {
		this.state = st;

		// Anytime the state changes the number of attempts also goes
		incrementAttempts();
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address
	 *            the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * In the future we may use the number of attempts in order to try and geocode the record again in case of failures due to temporary problems like network communication interruptions.
	 * 
	 * @return
	 */
	public int getAttemtpts() {
		return attempts;
	}

	private void incrementAttempts() {
		this.attempts += 1;
	}

	/**
	 * Equality is solely based on matching longitudes and latitudes.
	 * 
	 * @param Object
	 *            should be a GeoRecord object
	 * @return boolean
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Point) {
			Point gr = (Point) obj;
			return getLatitude() == gr.getLatitude() && getLongitude() == gr.getLongitude();
		}
		return false;
	}

	/**
	 * Renders the contents of the GeoRecord in the JSON format. Currently only includes the address, latitude, longitude and state.
	 * 
	 * @return
	 */
	public JSONObject toJSON() {
		JSONObject point = new JSONObject();
		JSONArray pt = new JSONArray();
		try {
			pt.put(getLatitude());
			pt.put(getLongitude());
			point.put("addrs", address);
			point.put("point", pt);
			point.put("color", getColor());
			point.put("state", getState().getCode());
			point.put("size", getSize());
		} catch (JSONException e) {
			Log.logger.logp(Level.SEVERE, "GeoRecord", "toJSON()", Log.EXCEPTION, e);
		}

		return point;
	}

	public String getColor() {
		return this.color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public double getSize() {
		return this.size;
	}

	public void setSize(double size) {
		this.size = size;
	}

}
