package com.pxltd.geodash.layers;

import com.microstrategy.web.app.tasks.architect.json.JSONArray;
import com.microstrategy.web.app.tasks.architect.json.JSONException;
import com.microstrategy.web.app.tasks.architect.json.JSONObject;
import com.pxltd.spatial.Point;
import com.pxltd.spatial.Points;

public class MassMarkerLayer extends Layer {
	public static final String type = "massMarkerLayer";
	private Points pts = new Points();
	private String iconType = "dynamic-pin";

	private int minMarkerWidth = 10;
	private int maxMarkerWidth = 250;
	private double minValue = Double.MAX_VALUE;
	private double maxValue = Double.MIN_VALUE;

	public Points getPoints(){
		return this.pts;
	}
	
	public void addPoint(Point pt){
		this.pts.addPoint(pt);
	}
	
	public String getIconType() {
		return iconType;
	}

	public void setMinValue(double m) {
		this.minValue = m;
	}

	public double getMinValue() {
		return this.minValue;
	}

	public void setMaxValue(double m) {
		this.maxValue = m;
	}

	public double getMaxValue() {
		return this.maxValue;
	}
	
	public void updateMaxMin(double value) {
		// System.out.println("max: " + this.maxValue + ", " + value + ", " + Math.max(this.maxValue, value));
		// System.out.println("min: " + this.minValue + ", " + value + ", " + Math.min(this.minValue, value));
		this.maxValue = Math.max(this.maxValue, value);
		this.minValue = Math.min(this.minValue, value);
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject j = new JSONObject();
		j.put("id", getID());
		j.put("name", getName());
		j.put("keys", getKeys());
		j.put("showInfoWindow",showInfoWindow());
		j.put("on", isOn());
		// j.put("cluster", isClustered());
		j.put("iconType", getIconType());
		j.put("source",getSource());
		j.put("reportID",getReportID());
		j.put("colorType",getColorType());
		j.put("staticColor",getStaticColor());
		j.put("gdGridId", getGdGridId());
		j.put("type",type);
		j.put("minValue",getMinValue());
		j.put("maxValue",getMaxValue());
		j.put("columns",getColumns());
		
		
		// j.put("rows",getRows());
		// j.put("rowsOfElementIds",getRowsOfElementIds());
		// j.put("geom", getPoints().toJSON());
		j.put("state", getState());
		j.put("infoWindow",getInfoWindow());
		j.put("infoWindowDocumentURL",getInfoWindowDocumentURL());
		j.put("infoWindowWidth",getInfoWindowWidth());
		j.put("infoWindowHeight",getInfoWindowHeight());
		j.put("minMarkerWidth",getMinMarkerWidth());
		j.put("maxMarkerWidth",getMaxMarkerWidth());
		// j.put("idValues",getIdValues());
		j.put("errors",getError());
		JSONArray circlesJ = new JSONArray();
		return j;
	}
	/**
	 * Returns and unpopulated instances.
	 * @param jl
	 * @return
	 */
	public static MassMarkerLayer getInstance(JSONObject jl){
		MassMarkerLayer ml = new MassMarkerLayer();
		JSONObject jkeys = jl.optJSONObject("keys");
		ml.setOrigin(jl);
		//ml.setClustered(jl.optBoolean("cluster"));
		ml.setColorMetricKey(jkeys.optInt("colorMetric"));
		ml.setSizeMetricKey(jkeys.optInt("sizeMetric"));
		ml.setColorType(jl.optString("colorType"));
		ml.setStaticColor(jl.optString("staticColor"));
		ml.setGdGridId(jl.optString("gdGridId"));
		ml.setGeoKey(jkeys.optInt("geo"));
		//ml.setIconType(jl.optString("iconType"));
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
		ml.setMinMarkerWidth(jl.optInt("minMarkerWidth"));
		ml.setMaxMarkerWidth(jl.optInt("maxMarkerWidth"));
		
		return ml;
	}

	public int getMinMarkerWidth() {
		return minMarkerWidth;
	}

	public void setMinMarkerWidth(int minMarkerWidth) {
		if(minMarkerWidth > 0 && minMarkerWidth < 256) {
			this.minMarkerWidth = minMarkerWidth;
		}
	}

	public int getMaxMarkerWidth() {
		return maxMarkerWidth;
	}

	public void setMaxMarkerWidth(int maxMarkerWidth) {
		if(maxMarkerWidth > 0 && maxMarkerWidth < 256) {
			this.maxMarkerWidth = maxMarkerWidth;
		}
	}


}
