package com.pxltd.geodash.layers;

import java.net.MalformedURLException;
import java.net.URL;

import com.microstrategy.web.app.tasks.architect.json.JSONException;
import com.microstrategy.web.app.tasks.architect.json.JSONObject;

public class KmlLayer extends Layer{
	public static final String type = "kmlLayer";
	private URL url;	
	
	public void setUrl(String url) throws MalformedURLException {
		this.url = new URL(url);
	}
	public URL getUrl() {
		return url;
	}

	public JSONObject toJSON() throws JSONException{
		JSONObject j = new JSONObject();
		j.put("id", getID());
		j.put("name", getName());
		j.put("url", getUrl().toString());
		j.put("showInfoWindow",showInfoWindow());
		j.put("on", isOn());
		j.put("type", type);
		j.put("errors", getError());
		j.put("state", getState());
		return j;
	}
	public static KmlLayer getInstance(JSONObject jl){
		KmlLayer ml = new KmlLayer();
		ml.setOrigin(jl);
		if(!jl.optString("id").trim().equalsIgnoreCase("") ){
			ml.setID(jl.optInt("id"));
		}
		ml.setName(jl.optString("name"));
		ml.setOn(jl.optBoolean("on"));
		ml.setShowInfoWindow(jl.optBoolean("showInfoWindow"));
		try {
			ml.setUrl(jl.optString("url"));
		} catch (MalformedURLException e) {
			ml.setState("failed");
			ml.addError("Bad url:  "+e.getMessage());
		}
		return ml;
	}
}
