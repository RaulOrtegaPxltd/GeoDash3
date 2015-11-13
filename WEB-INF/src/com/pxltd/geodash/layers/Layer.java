package com.pxltd.geodash.layers;

import com.microstrategy.web.app.tasks.architect.json.JSONArray;
import com.microstrategy.web.app.tasks.architect.json.JSONException;
import com.microstrategy.web.app.tasks.architect.json.JSONObject;

public class Layer {
	private int id = -1;
	private String name = "New Layer";
	private JSONArray columns = new JSONArray();
	private JSONArray rows = new JSONArray();
	private JSONArray rowsOfElementIds = new JSONArray();
	private JSONObject keys = new JSONObject();
	private boolean on = false;
	private boolean showInfoWindow = false;
	private String staticColor = "#ff9900";
	private String state = "not_ready";
	private String gdGridId = "";
	private JSONArray errors = new JSONArray();
	// editor params
	private String source = "current";
	private String colorType = "thresholds";
	private String reportID = "";
	private String infoWindow = "default";
	private String infoWindowDocumentURL = "";
	private int infoWindowWidth = 400;
	private int infoWindowHeight = 120;
	private JSONObject origin = null;
	private JSONObject idValues = new JSONObject();
	private String ids;	

	public JSONObject getKeys(){
		return keys;
	}
	
	public int getID() {
		return id;
	}
	public void setID(int id) {
		this.id=id;
	}
	public boolean isNew(){
		if(getID() == -1){
			return true;
		}else{
			return false;
		}
	}
	/**
	 * Empties the columns and rows arrays.
	 * @param name
	 */
	public void clear(){
		this.columns = new JSONArray();
		this.rows = new JSONArray();
	}
	public boolean isPopulated(){
		boolean val = true;
		if(columns.length() ==0 || rows.length()==0){
			val = false;
		}
		return val;
	}
	public void addColumn(JSONObject col){
		columns.put(col);
	}
	
	public JSONArray getColumns(){
		return columns;
	}
	
	public JSONArray getRows(){
		return rows;
	}
	
	public void addRow(JSONArray row){
		rows.put(row);
	}

	public void addRowOfElementIds(JSONArray row){
		rowsOfElementIds.put(row);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
	}

	public boolean showInfoWindow() {
		return showInfoWindow;
	}

	public void setShowInfoWindow(boolean showInfoWindow) {
		this.showInfoWindow = showInfoWindow;
	}

	public int getTitleKey() {
		return keys.optInt("title");
	}

	public void setTitleKey(int titleColKey) {
		try {
			this.keys.putOpt("title", titleColKey);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setStaticColor(String staticColor) {
		this.staticColor = staticColor;
	}
	public String getStaticColor() {
		return staticColor;
	}
	public int getGeoKey() {
		return keys.optInt("geo");
	}

	public void setGeoKey(int geoColKey) {
		try {
			this.keys.putOpt("geo", geoColKey);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public int getColorMetricKey() {
		return keys.optInt("colorMetric");
	}

	public int getSizeMetricKey() {
		return keys.optInt("sizeMetric");
	}

	public void setColorMetricKey(int colorMetricColKey) {
		try {
			this.keys.putOpt("colorMetric", colorMetricColKey);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void setSizeMetricKey(int sizeMetricColKey) {
		try {
			this.keys.putOpt("sizeMetric", sizeMetricColKey);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String getColorType() {
		return colorType;
	}

	public void setColorType(String colorType) {
		this.colorType = colorType;
	}

	public String getReportID() {
		return reportID;
	}

	public void setReportID(String reportID) {
		this.reportID = reportID;
	}
	
	public void addError(String msg){
		this.errors.put(msg);
	}
	
	public JSONArray getError(){
		return this.errors;
	}
	public void setOrigin(JSONObject origin) {
		this.origin  = origin;
	}
	/**
	 * Origin is the original JSONObject a layer is instantiated from.
	 * It may contain additional host specific data.
	 * @return
	 */
	public JSONObject getOrigin() {
		return origin;
	}

	public void setColumns(JSONArray cols) {
		this.columns = cols;		
	}

	public String getInfoWindow() {
		return infoWindow == "" ? "default" : infoWindow;
	}

	public void setInfoWindow(String infoWindow) {
		this.infoWindow = infoWindow;
	}

	public String getInfoWindowDocumentURL() {
		return infoWindowDocumentURL;
	}

	public void setInfoWindowDocumentURL(String infoWindowDocumentURL) {
		this.infoWindowDocumentURL = infoWindowDocumentURL;
	}

	public int getInfoWindowWidth() {
		return infoWindowWidth;
	}

	public void setInfoWindowWidth(int infoWindowWidth) {
		this.infoWindowWidth = infoWindowWidth;
	}

	public int getInfoWindowHeight() {
		return infoWindowHeight;
	}

	public void setInfoWindowHeight(int infoWindowHeight) {
		this.infoWindowHeight = infoWindowHeight;
	}

	public JSONObject getIdValues() {
		return idValues;
	}

	public void setIdValues(JSONObject idValues) {
		this.idValues = idValues;
	}

	public JSONArray getRowsOfElementIds() {
		return rowsOfElementIds;
	}

	public void setRowsOfElementIds(JSONArray rowsOfElementIds) {
		this.rowsOfElementIds = rowsOfElementIds;
	}

	public String getGdGridId() {
		return gdGridId;
	}

	public void setGdGridId(String gdGridId) {
		this.gdGridId = gdGridId;
	}
	
	public String getIds() {
		return ids;
	}

	public void setIds(String ids) {
		this.ids = ids;
	}
	
}
