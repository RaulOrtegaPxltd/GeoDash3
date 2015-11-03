package com.pxltd.geodash;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import com.microstrategy.utils.log.Level;
import com.microstrategy.web.app.tasks.architect.json.JSONException;
import com.microstrategy.web.app.tasks.architect.json.JSONObject;
import com.pxltd.geodash.ServiceException.GeoExceptionCodes;

public class GeoshapingService {
	private URL url;
	private boolean matchShape = true;
	private JSONObject content = new JSONObject();
	private JSONObject response;
	private JSONObject layer;

	public GeoshapingService() throws GeodashException {
		try {
			this.url = new URL(GeoshapingServiceConfig.BASE_URL + "api_key=" + GeodashConfig.GEODASH_API_KEY);
		} catch (MalformedURLException e) {
			throw new GeodashException(e.getMessage());
		}
	}

	public void addShape(String location, String color, int rowID) throws JSONException {
		color.replace("#", "");
		content.put(location, color + "|" + rowID);
	}

	public void setLevel(String level) throws JSONException {
		content.put("level", level);
	}

	public void setCountry(String country) throws JSONException {
		content.put("country", country);
	}

	public void setShouldShapeSearch(boolean shapeSearch) throws JSONException {
		this.matchShape = shapeSearch;
	}

	public JSONObject submitLayer() throws GeodashException, ServiceException {
		try {
			String data = "layer[content]" + "=" + URLEncoder.encode(this.content.toString(), "UTF-8");
			data += "&layer[source]=mstr";
			data += "&layer[match_shape]=" + matchShape;
			// System.out.println(url);
			// System.out.println(this.content.toString());
			// send data
			URLConnection conn = GeodashConfig.getOpenConnection(url);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(data);
			wr.flush();

			// get response
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			StringBuilder builder = new StringBuilder();
			while ((line = rd.readLine()) != null) {
				builder.append(line);
			}
			response = new JSONObject(builder.toString());

			wr.close();
			rd.close();
			return response;
		} catch (Exception e) {
			Log.logger.logp(Level.SEVERE, "GeoshapingService", "submitLayer", Log.EXCEPTION, e);
			throw new ServiceException("Error while querying GeoDash API services.  " + e.getMessage() + " Attempted URL:  " + url.toString(), GeoExceptionCodes.COMMUNICATION_ERROR);
		}
	}

	public JSONObject getResponse() throws ServiceException {
		if (response == null) {
			throw new ServiceException("Layer must first be submitted.", GeoExceptionCodes.COMMUNICATION_ERROR);
		} else
			try {
				if (!response.getString("status").equalsIgnoreCase("OK")) {
					throw new ServiceException("GeoDash failed to create layer.  Response:  " + response.toString(), GeoExceptionCodes.COMMUNICATION_ERROR);
				} else {
					return response;
				}
			} catch (JSONException e) {
				Log.logger.logp(Level.SEVERE, "GeoshapingService", "submitLayer", response.toString(), e);
				throw new ServiceException("Error while querying GeoDash API services.  Could not read response.", GeoExceptionCodes.COMMUNICATION_ERROR);
			}
	}

	public JSONObject getLayer() throws ServiceException {
		if (layer != null) {
			return layer;
		}

		try {
			layer = getResponse().getJSONObject("response");
			return layer;
		} catch (JSONException e) {
			Log.logger.logp(Level.SEVERE, "GeoshapingService", "submitLayer", response.toString(), e);
			throw new ServiceException("Error while querying GeoDash API services.  Could not read response.", GeoExceptionCodes.COMMUNICATION_ERROR);
		}
	}

	public String getLayerKey() throws ServiceException {
		String lk = "";
		try {
			JSONObject l = getLayer();
			lk = l.getString("layer_key");
		} catch (Exception e) {
			Log.logger.logp(Level.SEVERE, "GeoshapingService", "submitLayer", response.toString(), e);
			throw new ServiceException("Error while querying Geodash API services.  Could not read response.", GeoExceptionCodes.COMMUNICATION_ERROR);
		}
		return lk;
	}

}
