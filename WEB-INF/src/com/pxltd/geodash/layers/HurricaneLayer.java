package com.pxltd.geodash.layers;

import com.microstrategy.web.app.tasks.architect.json.JSONException;
import com.microstrategy.web.app.tasks.architect.json.JSONObject;

public class HurricaneLayer extends Layer{
	public static final String type = "hurricaneLayer";
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
		j.put("dss_product_item_name", getDssProductItemName());
		return j;
	}
	
	public static HurricaneLayer getInstance(JSONObject jl){
		HurricaneLayer ml = new HurricaneLayer();
		ml.setOrigin(jl);
		if(!jl.optString("id").trim().equalsIgnoreCase("") ){
			ml.setID(jl.optInt("id"));
		}
		ml.setName(jl.optString("name"));
		ml.setOn(jl.optBoolean("on"));
		ml.setDssProductItemId(jl.optString("dss_product_item_id"));
		ml.setDssProductItemName(jl.optString("dss_product_item_name"));
		// setting error doesn't make UI editor fail on save. Need to investigate
		// ml.addError("Got an Erorr");
		// ml.setState("failed");
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
}
