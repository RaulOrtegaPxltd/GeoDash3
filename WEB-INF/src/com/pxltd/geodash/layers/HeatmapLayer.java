package com.pxltd.geodash.layers;

import com.microstrategy.web.app.tasks.architect.json.JSONArray;
import com.microstrategy.web.app.tasks.architect.json.JSONException;
import com.microstrategy.web.app.tasks.architect.json.JSONObject;
import com.pxltd.spatial.Point;
import com.pxltd.spatial.Points;

public class HeatmapLayer extends Layer{
	public static final String type = "heatmapLayer";
	private JSONArray geom = new JSONArray();
	private Points pts = new Points();
	private String layerKey = "";
	private String radius = "30";
	private int weightMetric = -1;

	public JSONObject toJSON() throws JSONException{
		JSONObject j = new JSONObject();

		j.put("id", getID());
		j.put("name", getName());
		j.put("on", isOn());
		j.put("type", type);
		j.put("state", getState());
		j.put("keys", getKeys());
		j.put("layerKey", getLayerKey());
		j.put("gdGridId", getGdGridId());
		j.put("source",getSource());
		j.put("reportID",getReportID());
		j.put("colorType",getColorType());
		j.put("staticColor",getStaticColor());
		j.put("columns",getColumns());
		j.put("rows",getRows());
		j.put("rowsOfElementIds",getRowsOfElementIds());
		j.put("geom", getPoints().toJSON());
		j.put("idValues", getIdValues());
		j.put("radius", getRadius());
		j.put("weightMetric", getWeightMetric());
		
		j.put("errors", getError());

		return j;
	}
	
	public static HeatmapLayer getInstance(JSONObject jl){
		HeatmapLayer ml = new HeatmapLayer();
		JSONObject jkeys = jl.optJSONObject("keys");
		
		ml.setOrigin(jl);
		if(!jl.optString("id").trim().equalsIgnoreCase("") ){
			ml.setID(jl.optInt("id"));
		}
		ml.setName(jl.optString("name"));
		ml.setOn(jl.optBoolean("on"));

		ml.setColorMetricKey(jkeys.optInt("colorMetric"));
		ml.setColorType(jl.optString("colorType"));
		ml.setStaticColor(jl.optString("staticColor"));
		ml.setGdGridId(jl.optString("gdGridId"));
		ml.setLayerKey(jl.optString("layerKey"));
		ml.setGeoKey(jkeys.optInt("geo"));
		ml.setRadius(jl.optString("radius"));
		ml.setWeightMetric(jl.optInt("weightMetric"));
		if(!jl.optString("id").trim().equalsIgnoreCase("") ){
			ml.setID(jl.optInt("id"));
		}

		ml.setColumns(jl.optJSONArray("columns"));
		ml.setReportID(jl.optString("reportID"));
		ml.setSource(jl.optString("source"));
		ml.setTitleKey(jkeys.optInt("title"));
		
		return ml;
	}

	public Points getPoints(){
		return this.pts;
	}
	
	public void addPoint(Point pt){
		this.pts.addPoint(pt);
	}

	public String getLayerKey() {
		return layerKey;
	}
	
	public String getRadius() {
		return radius;
	}
	
	public void setRadius(String radius) {
		this.radius = radius;
	}

	public int getWeightMetric() {
		return weightMetric;
	}
	
	public void setWeightMetric(int wm) {
		this.weightMetric = wm;
	}

	public void addLayerGeom(JSONObject r){
		geom.put(r);
	}
	
	public void setLayerKey(String layerKey) {
		this.layerKey = layerKey;
	}

}
