package com.pxltd.geodash.layers;

import com.microstrategy.web.app.tasks.architect.json.JSONArray;
import com.microstrategy.web.app.tasks.architect.json.JSONException;
import com.microstrategy.web.app.tasks.architect.json.JSONObject;

public class AreaLayer extends Layer {
	public static final String type = "areaLayer";
	private JSONArray geom = new JSONArray();
	private String strokeColor = "#ffffff";
	private String highliteColor = "#ff9900";
	private String layerKey = "";

	public String getLayerKey() {
		return layerKey;
	}
	
	public void addLayerGeom(JSONObject r){
		geom.put(r);
	}
	
	public void setLayerKey(String layerKey) {
		this.layerKey = layerKey;
	}

	public void setStrokeColor(String strokeColor) {
		this.strokeColor = strokeColor;
	}

	public String getStrokeColor() {
		return strokeColor;
	}

	public void setHighliteColor(String highliteColor) {
		this.highliteColor = highliteColor;
	}

	public String getHighliteColor() {
		return highliteColor;
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject j = new JSONObject();
		j.put("id", getID());
		j.put("name", getName());
		j.put("keys", getKeys());
		j.put("showInfoWindow",showInfoWindow());
		j.put("on", isOn());
		j.put("strokeColor", getStrokeColor());
		j.put("layerKey", getLayerKey());
		j.put("gdGridId", getGdGridId());
		j.put("highliteColor", getHighliteColor());
		j.put("source",getSource());
		j.put("reportID",getReportID());
		j.put("colorType",getColorType());
		j.put("staticColor",getStaticColor());
		j.put("type",type);
		j.put("columns",getColumns());
		j.put("rows",getRows());
		j.put("rowsOfElementIds",getRowsOfElementIds());
		j.put("geom", geom);
		j.put("state", getState());
		j.put("infoWindow",getInfoWindow());
		j.put("infoWindowDocumentURL",getInfoWindowDocumentURL());
		j.put("infoWindowWidth",getInfoWindowWidth());
		j.put("infoWindowHeight",getInfoWindowHeight());
		j.put("idValues",getIdValues());
		j.put("errors",getError());
		return j;
	}	

	/**
	 * Returns and unpopulated instances.
	 * @param jl
	 * @return
	 */
	public static AreaLayer getInstance(JSONObject jl){
		AreaLayer ml = new AreaLayer();
		JSONObject jkeys = jl.optJSONObject("keys");
		ml.setOrigin(jl);
		ml.setStrokeColor((jl.optString("strokeColor")));
		ml.setColorMetricKey(jkeys.optInt("colorMetric"));
		ml.setColorType(jl.optString("colorType"));
		ml.setStaticColor(jl.optString("staticColor"));
		ml.setGdGridId(jl.optString("gdGridId"));
		ml.setLayerKey(jl.optString("layerKey"));
		ml.setGeoKey(jkeys.optInt("geo"));
		ml.setHighliteColor(jl.optString("highliteColor"));
		if(!jl.optString("id").trim().equalsIgnoreCase("") ){
			ml.setID(jl.optInt("id"));
		}
		ml.setColumns(jl.optJSONArray("columns"));
		ml.setName(jl.optString("name"));
		ml.setOn(jl.optBoolean("on"));
		ml.setReportID(jl.optString("reportID"));
		ml.setShowInfoWindow(jl.optBoolean("showInfoWindow"));
		ml.setSource(jl.optString("source"));
		ml.setTitleKey(jkeys.optInt("title"));
		ml.setInfoWindow(jl.optString("infoWindow"));
		ml.setInfoWindowDocumentURL(jl.optString("infoWindowDocumentURL"));
		ml.setInfoWindowWidth(jl.optInt("infoWindowWidth"));
		ml.setInfoWindowHeight(jl.optInt("infoWindowHeight"));
		return ml;
	}

}
