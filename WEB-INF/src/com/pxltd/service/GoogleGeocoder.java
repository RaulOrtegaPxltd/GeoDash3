package com.pxltd.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.microstrategy.utils.log.Level;
import com.microstrategy.web.app.tasks.architect.json.JSONException;
import com.microstrategy.web.app.tasks.architect.json.JSONObject;
import com.pxltd.geodash.GeodashConfig;
import com.pxltd.geodash.GeodashException;
import com.pxltd.geodash.ServiceException;
import com.pxltd.geodash.ServiceException.GeoExceptionCodes;
import com.pxltd.spatial.Point;
import com.pxltd.util.Base64;

/**
 * The GoogleGeocoder is responsible for authenticating and communicating with the Google Geocoding Web Service application. It parses the response from Google and creates a {@link Point} object.
 * 
 * @author Ajo Abraham
 * @since 1.1.0 or earlier
 * @see http://code.google.com/apis/maps/documentation/geocoding/index.html
 * 
 */
public class GoogleGeocoder {
	private final String BASE_URL = "http://maps.google.com/maps/api/geocode/json?sensor=false";
	private final String BASE_SSL_URL = "https://maps-api-ssl.google.com/maps/api/geocode/json?sensor=false";
	private String _clientID;
	private String _privateKey;
	private boolean _ssl;
	private String _regionBias;
	private byte[] _decodedKey;
	private String _sURL;

	public GoogleGeocoder() throws ServiceException, GeodashException {
		this._clientID = GeodashConfig.GOOGLE_CLIENT_ID;
		this._privateKey = GeodashConfig.GOOGLE_PRIVATE_KEY;
		this._ssl = Boolean.parseBoolean(GeodashConfig.USE_SSL);
		this._regionBias = GeodashConfig.GOOGLE_REGION_BIAS;

		_privateKey = _privateKey.replace('-', '+');
		_privateKey = _privateKey.replace('_', '/');

		try {
			_decodedKey = Base64.decode(_privateKey);
		} catch (IOException e) {
			throw new ServiceException("There was a problem while decoding the private key.", GeoExceptionCodes.INVALID_KEY);
		}

		if (_ssl) {
			_sURL = BASE_SSL_URL + "&client=" + _clientID;
			;
		} else {
			_sURL = BASE_URL + "&client=" + _clientID;
		}

		if (_regionBias != null) {
			_sURL += "&region=" + _regionBias;
		}
	}

	public Point geocode(String location) throws JSONException, ServiceException {
		JSONObject rs;
		URL url;

		try {
			url = getSignedURL(location);
		} catch (Exception e) {
			throw new ServiceException("Error while generating url for geocoding:  " + e.getMessage(), GeoExceptionCodes.INVALID_KEY);
		}

		try {
			URLConnection conn;
			// setup proxy if proxy is being used
			// depracted setup here - we should be using the
			// getOpenConnection() method of gc.
			if (GeodashConfig.hasProxy()) {
				InetSocketAddress addr = new InetSocketAddress(GeodashConfig.PROXY_SERVER_ADDRESS, Integer.parseInt(GeodashConfig.PROXY_SERVER_PORT));
				Proxy p = new Proxy(Proxy.Type.HTTP, addr);
				conn = url.openConnection(p);
			} else {
				conn = url.openConnection();
			}

			conn.setRequestProperty("User-Agent", "GeoDash GGeocoder");
			String line;
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}

			String response = builder.toString();
			rs = new JSONObject(response);

		} catch (Exception e) {
			Log.logger.logp(Level.SEVERE, "GoogleGeocoder", "geocode", Log.EXCEPTION, e);
			throw new ServiceException("Error while querying the Google Geocoding service.  " + e.getMessage() + " Attempted URL:  " + url.toString(), GeoExceptionCodes.COMMUNICATION_ERROR);
		}

		Point gr = new Point(location);
		gr.setState(EnumGeoStatusCode.getEnum(rs.optString("status")));

		// if the state is anything other than okay we need to get out and let the
		// geocoding service figure out what to do
		if (gr.getState() != EnumGeoStatusCode.OK) {
			return gr;
		}

		JSONObject res = rs.getJSONArray("results").getJSONObject(0);
		JSONObject loc = res.getJSONObject("geometry").getJSONObject("location");

		gr.setLatitude(loc.optDouble("lat"));
		gr.setLongitude(loc.optDouble("lng"));
		gr.setLocationType(EnumLocationType.getEnum(res.getJSONArray("types").getString(0)));

		return gr;
	}

	/**
	 * Google now requires us to sign the path and query portion of a geocoding request based on an assigned private key. The code below is a modification of the example on http://code.google.com/apis/maps/documentation/premier/guide.html
	 * 
	 * @param resource
	 *            this should be the query path + parameters ie. "/maps/api/geocode/json?address=New+York&sensor=false" you should not include the "http://maps.google.com" portion.
	 * 
	 * @return URL - used to query Google Service
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	private URL getSignedURL(String location) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
		URL tempURL = new URL(getBaseURL() + "&address=" + URLEncoder.encode(location, "utf8"));
		String resource = tempURL.getPath() + '?' + tempURL.getQuery();

		// Get an HMAC-SHA1 signing key from the raw key bytes
		SecretKeySpec sha1Key = new SecretKeySpec(_decodedKey, "HmacSHA1");

		// Get an HMAC-SHA1 Mac instance and initialize it with the HMAC-SHA1 key
		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(sha1Key);

		// compute the binary signature for the request
		byte[] sigBytes = mac.doFinal(resource.getBytes());

		// base 64 encode the binary signature
		String signature = Base64.encodeBytes(sigBytes);

		// convert the signature to 'web safe' base 64
		signature = signature.replace('+', '-');
		signature = signature.replace('/', '_');

		return new URL(tempURL.getProtocol() + "://" + tempURL.getHost() + resource + "&signature=" + signature);
	}

	public String getBaseURL() {
		return _sURL;
	}

}
