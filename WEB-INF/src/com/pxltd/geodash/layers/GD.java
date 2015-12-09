package com.pxltd.geodash.layers;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.microstrategy.web.app.tasks.architect.json.JSONArray;
import com.microstrategy.web.app.tasks.architect.json.JSONException;
import com.microstrategy.web.app.tasks.architect.json.JSONObject;

public class GD {
	private ArrayList<Layer> layers = new ArrayList<Layer>();
	private String clientID = "";
	private String geodashAPIKey = "";
	private int mapType = 0;
	private static final String version = "2.0.0";
	private JSONArray sourceColumns = new JSONArray();
	private String webapi;
	private boolean isSsl = false;
	
	// This conveys the overall well being of the
	// application.  values:  ready,failed,bad_license
	private String status = "ready";
	// Captures system wide errors ie connection
	// failure, licensing issues etc.
	private String[] errors = {};
	
	public void addLayer(Layer l) {
		layers.add(l);
	}
	public ArrayList<Layer> getLayers(){
		return this.layers;
	}
	public void removeLayer(int id) {
		Iterator<Layer> it = layers.iterator();
		while(it.hasNext()){
			Layer l = it.next();
			if(l.getID() == id){
				layers.remove(l);
				break;
			}
		}
	}
	public Layer getLayer(int id) {
		Iterator<Layer> it = layers.iterator();
		Layer l = null;
		while(it.hasNext()){
			l = it.next();
			if(l.getID() == id){
				break;
			}
		}
		return l;
	}
	public String getClientID() {
		return clientID;
	}
	public String getWebapi() {
		return webapi;
	}
	public void setClientID(String clientID) {
		if(clientID == "" || clientID == null){ return ;}
		this.clientID = clientID;
	}
	public void setWebapi(String webapi) {
		this.webapi = webapi;
	}
	public String getGeodashAPIKey() {
		return geodashAPIKey;
	}
	public void setGeodashAPIKey(String geodashAPIKey) {
		if(geodashAPIKey == "" || geodashAPIKey == null){ return ;}
		this.geodashAPIKey = geodashAPIKey;
	}
	public void setErrors(String[] errors) {
		this.errors = errors;
	}
	public void addError(String error){
		this.errors[this.errors.length] = error;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatus() {
		return status;
	}
	public void setSourceColumns(JSONArray cols) {
		this.sourceColumns = cols;		
	}
	public JSONArray getSourceColumns() {
		return sourceColumns;
	}
	public void setMapType(int mapType) {
		this.mapType = mapType;
	}
	public int getMapType() {
		return mapType;
	}
	public void setIsSsl(boolean isSsl) {
		this.isSsl = isSsl;
	}
	public boolean getIsSll() {
		return isSsl;
	}
	
	public int getNextLayerID() {
		Iterator<Layer> it = layers.iterator();
		if( layers.size() ==0){ return 0; }
		int id = 0;
		while(it.hasNext()){
			int tempid = it.next().getID();
			if(tempid > id) { id = tempid; }
		}
		return id +1;
	}
	
	public static String propsToJson(String props) {
	    if(props.startsWith("{")) {
	    	// must be JSON style props
	    	return props;
	    }
	    if(Thread.currentThread().getContextClassLoader() == null) {
	    	Thread.currentThread().setContextClassLoader(GD.class.getClassLoader());
	    }
	    XPathFactory xpathFactory = XPathFactory.newInstance();
	    XPath xpath = xpathFactory.newXPath();
	    
	    InputSource source = new InputSource(new StringReader(props));
	    try {
	    	String msg = xpath.evaluate("/widgetProps/fmt/geoDashJson/@value", source);
	    	return msg;
	    } catch (XPathExpressionException e) {
	    	return "";
	    }
	}
	
	/**
	 * Return an XML string to set as the MSTR props value from JSON representing layers
	 * definitions
	 * 
	 * @param json
	 * @return
	 * @throws ParserConfigurationException
	 * @throws DOMException
	 * @throws TransformerException
	 */
	public static String toXmlString(JSONObject json) throws ParserConfigurationException, DOMException, TransformerException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
		Document doc = docBuilder.newDocument();
		
		Element root = doc.createElement("widgetProps");
		Element fmt = doc.createElement("fmt");
		Element geoDashJson = doc.createElement("geoDashJson");
		
		doc.appendChild(root);
		root.appendChild(fmt);
		fmt.appendChild(geoDashJson);
		geoDashJson.setAttribute("value", json.toString());
		
		// Output XML
		StreamResult xmlOutput = new StreamResult(new StringWriter());
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(source, xmlOutput);
		
		String str = xmlOutput.getWriter().toString();
		
		return str;
	}
	
	// used for render just the base when rendering geodash initially
	// used for converting instance to pure json for storage.
	public JSONObject toJSON(boolean includeLayers) throws JSONException{
		JSONObject j = new JSONObject();
		j.put("api", getWebapi());
		j.put("mapType", getMapType());
		j.put("version", GD.version);
		j.put("clientID", getClientID());
		j.put("gdAPIKey", getGeodashAPIKey());
		j.put("status", getStatus());
		j.put("sourceColumns", getSourceColumns());
		j.put("isSSL", getIsSll());
		j.put("errors", this.errors);
		if(includeLayers){
			JSONArray lyrs = new JSONArray();
			Iterator<Layer> it = layers.iterator();
			while(it.hasNext()){
				Layer layer = it.next();
				if(layer instanceof MarkerLayer){
					lyrs.put(((MarkerLayer)layer).toJSON());
				}else if(layer instanceof MassMarkerLayer){
					lyrs.put(((MassMarkerLayer)layer).toJSON());
				}else if(layer instanceof AreaLayer){
					lyrs.put(((AreaLayer)layer).toJSON());
				}else if(layer instanceof KmlLayer){
					lyrs.put(((KmlLayer)layer).toJSON());
				}else if(layer instanceof VectorLayer){
					lyrs.put(((VectorLayer)layer).toJSON());
				}else if(layer instanceof HeatmapLayer){
					lyrs.put(((HeatmapLayer)layer).toJSON());
				}else if(layer instanceof HurricaneLayer){
					lyrs.put(((HurricaneLayer)layer).toJSON());
				}else if(layer instanceof DssLayer){
					lyrs.put(((DssLayer)layer).toJSON());
	    		}
			}
			j.put("layers", lyrs);
		}
		return j;
	}
	
	public static Layer getLayerInstance(JSONObject jl){
		String type = jl.optString("type");
		if(type == null) { return null;}
		Layer l = null;
		if(type.equalsIgnoreCase("markerLayer")){
			l = MarkerLayer.getInstance(jl);
		}else if(type.equalsIgnoreCase("massMarkerLayer")){
			l = MassMarkerLayer.getInstance(jl);
		}else if(type.equalsIgnoreCase("areaLayer")){
			l = AreaLayer.getInstance(jl);
		}else if(type.equalsIgnoreCase("kmlLayer")){
			l = KmlLayer.getInstance(jl);
		}else if(type.equalsIgnoreCase("vectorLayer")){
			l = VectorLayer.getInstance(jl);
		}else if(type.equalsIgnoreCase("heatmapLayer")){
			l = HeatmapLayer.getInstance(jl);
		}else if(type.equalsIgnoreCase("hurricaneLayer")){
			l = HurricaneLayer.getInstance(jl);
		}else if((type.toLowerCase()).startsWith("dsslayer")){
			l = DssLayer.getInstance(jl);
		}
		return l;
	}
	/**
	 * 
	 * 
	 * @param gdj
	 * @return
	 */
	public static GD getInstance(JSONObject gdj){
		GD gd = new GD();
		gd.setClientID(gdj.optString("clientID"));
		gd.setWebapi(gdj.optString("webapi"));
		gd.setGeodashAPIKey(gdj.optString("gdAPIKey"));
		gd.setMapType(gdj.optInt("mapType"));
		JSONArray jlayers = gdj.optJSONArray("layers");
		if(jlayers != null){			
			for(int i=0;i<jlayers.length();i++){
				JSONObject l = jlayers.optJSONObject(i);
				if(l != null){
					gd.addLayer(GD.getLayerInstance(l));
				}
			}
		}
		
		return gd;
	}

}
