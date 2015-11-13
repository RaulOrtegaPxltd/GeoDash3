package com.pxltd.geodash.layers;

import java.util.ArrayList;
import java.util.List;

import com.pxltd.spatial.Point;
import com.pxltd.spatial.Points;
import com.microstrategy.web.app.tasks.architect.json.JSONArray;
import com.microstrategy.web.app.tasks.architect.json.JSONException;
import com.microstrategy.web.app.tasks.architect.json.JSONObject;

public class MarkerLayer extends Layer {
	public static final String type = "markerLayer";
	private Points pts = new Points();
	private String iconType = "dynamic-pin";
	private boolean cluster = false;
	public static final String[] ICON_TYPES = {"dynamic-pin","dynamic-disk","academy","activities","airport","amusement","aquarium","art-gallery","atm",
		"baby","dollar","euro",	"pound","yen","bar","barber","beach","drink","bicycle","books","bowling","bus",	"cafe",	"camping","car",
		"car-rental","car-repair","casino","caution","cemetery","cinema","civic-building","computer","corporate","legal","fire","flag",
		"floral","helicopter","home","info","locomotive","medical","mobile","motorcycle","music","parking",	"gas-station","phone","picnic",
		"postal","repair","restaurant","sail","school","ship","shopping-bag","shopping-cart","ski",	"fast-food","snow",	"sport","star",
		"swim","taxi","train","truck","female","male","couple","wheelchair"};

	private int minMarkerWidth = 10;
	private int maxMarkerWidth = 250;
	private List<Circle> circles = new ArrayList<Circle>();

	public Points getPoints(){
		return this.pts;
	}
	
	public void addPoint(Point p){
		this.pts.addPoint(p);
	}
	
	public String getIconType() {
		return iconType;
	}

	public void setIconType(String it) {
		boolean exists = false;
		for(String icon : ICON_TYPES){
			if(icon.equalsIgnoreCase(it)){
				exists = true;break;
			}
		}
		if(exists){
			this.iconType = it.toLowerCase();
		}		
	}

	public boolean isClustered() {
		return cluster;
	}

	public void setClustered(boolean cluster) {
		this.cluster = cluster;
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject j = new JSONObject();
		j.put("id", getID());
		j.put("name", getName());
		j.put("keys", getKeys());
		j.put("showInfoWindow",showInfoWindow());
		j.put("on", isOn());
		j.put("cluster", isClustered());
		j.put("iconType", getIconType());
		j.put("source",getSource());
		j.put("reportID",getReportID());
		j.put("colorType",getColorType());
		j.put("staticColor",getStaticColor());
		j.put("gdGridId", getGdGridId());
		j.put("type",type);
		j.put("columns",getColumns());
		j.put("rows",getRows());
		j.put("rowsOfElementIds",getRowsOfElementIds());
		j.put("geom", getPoints().toJSON());
		j.put("state", getState());
		j.put("infoWindow",getInfoWindow());
		j.put("infoWindowDocumentURL",getInfoWindowDocumentURL());
		j.put("infoWindowWidth",getInfoWindowWidth());
		j.put("infoWindowHeight",getInfoWindowHeight());
		j.put("minMarkerWidth",getMinMarkerWidth());
		j.put("maxMarkerWidth",getMaxMarkerWidth());
		j.put("idValues",getIdValues());
		j.put("errors",getError());
		JSONArray circlesJ = new JSONArray();
		for(Circle c: getCircles()) {
			JSONObject cj = new JSONObject();
			cj.put("color", c.getColor());
			cj.put("size", c.getSize());
			cj.put("units", c.getUnits());
			circlesJ.put(cj);
		}
		j.put("circles", circlesJ);
		return j;
	}
	/**
	 * Returns and unpopulated instances.
	 * @param jl
	 * @return
	 */
	public static MarkerLayer getInstance(JSONObject jl){
		MarkerLayer ml = new MarkerLayer();
		JSONObject jkeys = jl.optJSONObject("keys");
		ml.setOrigin(jl);
		ml.setClustered(jl.optBoolean("cluster"));
		ml.setColorMetricKey(jkeys.optInt("colorMetric"));
		ml.setSizeMetricKey(jkeys.optInt("sizeMetric"));
		ml.setColorType(jl.optString("colorType"));
		ml.setStaticColor(jl.optString("staticColor"));
		ml.setGdGridId(jl.optString("gdGridId"));
		ml.setGeoKey(jkeys.optInt("geo"));
		ml.setIconType(jl.optString("iconType"));
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
		
		JSONArray circlesArr = jl.optJSONArray("circles");
		if(circlesArr != null) {
			for(int i=0; i<circlesArr.length(); i++) {
				try {
					Circle circle = new Circle();
					JSONObject circleJ = circlesArr.getJSONObject(i);
					circle.setColor(circleJ.optString("color"));
					circle.setSize(circleJ.optString("size"));
					circle.setUnits(circleJ.optString("units"));
					ml.addCircle(circle);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		// ml.setReportID(jl.optJSONArray("circles");
		return ml;
	}

	public int getMinMarkerWidth() {
		return minMarkerWidth;
	}

	public void setMinMarkerWidth(int minMarkerWidth) {
		this.minMarkerWidth = minMarkerWidth;
	}

	public int getMaxMarkerWidth() {
		return maxMarkerWidth;
	}

	public void setMaxMarkerWidth(int maxMarkerWidth) {
		this.maxMarkerWidth = maxMarkerWidth;
	}

	public List<Circle> getCircles() {
		return circles;
	}

	public void setCircles(List<Circle> circles) {
		this.circles = circles;
	}
	
	public void addCircle(Circle c) {
		circles.add(c);
	}

}
