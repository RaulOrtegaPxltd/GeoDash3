package com.pxltd.geodash.layers;

import java.util.HashMap;
import java.util.Iterator;

import com.microstrategy.web.app.tasks.architect.json.JSONException;
import com.microstrategy.web.app.tasks.architect.json.JSONObject;

public class DssLayer extends Layer{
	public  String type; // = "dssLayer";
	private String dssProductType;
	private HashMap<String, String> dssProductSettings = new HashMap<String, String>();
	private String dssProductItemid;
	private String dssProductItemName;
	
	public JSONObject toJSON() throws JSONException{
		JSONObject j = new JSONObject();
		j.put("id", getID());
		j.put("name", getName());
		j.put("name", getName());
		j.put("on", isOn());
		j.put("type", type);
		j.put("errors", getError());
		j.put("state", getState());
		j.put("dss_product_item_id", getDssProductItemId());
		j.put("dss_product_type", getDssProductType());

		JSONObject settingsJ = new JSONObject();
		for(String key: getDssProductSettings().keySet()) {
			settingsJ.put(key, getDssProductSettings().get(key));
		}
		j.put("settings", settingsJ);
		
		return j;
	}
	
	public static DssLayer getInstance(JSONObject jl){
		DssLayer ml = new DssLayer();
		ml.type = jl.optString("type");
		ml.setOrigin(jl);
		if(!jl.optString("id").trim().equalsIgnoreCase("") ){
			ml.setID(jl.optInt("id"));
		}
		ml.setName(jl.optString("name"));
		ml.setOn(jl.optBoolean("on"));
		ml.setDssProductItemId(jl.optString("dss_product_item_id"));
		ml.setDssProductItemName(jl.optString("dss_product_item_name"));
		ml.setDssProductType(jl.optString("dss_product_type"));
		JSONObject settingsJ = jl.optJSONObject("settings");
		try {
			Iterator keysIter = settingsJ.keys();
			while(keysIter.hasNext()) {
				String key = (String) keysIter.next();
				ml.dssProductSettings.put(key, settingsJ.getString(key));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return ml;
	}

	public String getDssProductItemId() {
		return dssProductItemid;
	}

	public void setDssProductItemId(String dssProductItemId) {
		this.dssProductItemid = dssProductItemId;
	}

	public String getDssProductItemName() {
		return dssProductItemName;
	}

	public void setDssProductItemName(String dssProductItemName) {
		this.dssProductItemName = dssProductItemName;
	}

	public String getDssProductType() {
		return dssProductType;
	}

	public void setDssProductType(String dssProductType) {
		this.dssProductType = dssProductType;
	}

	public HashMap<String, String> getDssProductSettings() {
		return dssProductSettings;
	}

	public void setDssProductSettings(HashMap<String, String> dssProductSettings) {
		this.dssProductSettings = dssProductSettings;
	}
}
