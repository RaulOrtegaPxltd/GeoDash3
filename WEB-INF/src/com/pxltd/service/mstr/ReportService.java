package com.pxltd.service.mstr;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.microstrategy.utils.log.Level;
import com.microstrategy.web.app.tasks.architect.json.JSONArray;
import com.microstrategy.web.app.tasks.architect.json.JSONException;
import com.microstrategy.web.app.tasks.architect.json.JSONObject;
import com.microstrategy.web.app.utils.HTMLHelper;
import com.microstrategy.web.beans.RequestKeys;
import com.microstrategy.web.beans.ViewBean;
import com.microstrategy.web.objects.SimpleList;
import com.microstrategy.web.objects.WebAxis;
import com.microstrategy.web.objects.WebElementHelper;
import com.microstrategy.web.objects.WebGraph;
import com.microstrategy.web.objects.WebGridData;
import com.microstrategy.web.objects.WebGridHeaders;
import com.microstrategy.web.objects.WebHeader;
import com.microstrategy.web.objects.WebHeaders;
import com.microstrategy.web.objects.WebObjectInfo;
import com.microstrategy.web.objects.WebReportGrid;
import com.microstrategy.web.objects.WebRowValue;
import com.microstrategy.web.objects.WebTemplate;
import com.microstrategy.web.objects.WebTemplateAttribute;
import com.microstrategy.web.objects.WebTemplateConsolidation;
import com.microstrategy.web.objects.WebTemplateCustomGroup;
import com.microstrategy.web.objects.WebTemplateMetric;
import com.microstrategy.web.objects.WebTemplateMetrics;
import com.microstrategy.web.objects.WebTemplateUnit;
import com.microstrategy.web.objects.WebViewInstance;
import com.microstrategy.webapi.EnumDSSXMLAxisName;
import com.microstrategy.webapi.EnumDSSXMLTemplateUnitType;
import com.pxltd.geodash.GeodashConfig;
import com.pxltd.geodash.GeodashException;
import com.pxltd.geodash.GeoshapingService;
import com.pxltd.geodash.ServiceException;
import com.pxltd.geodash.layers.AreaLayer;
import com.pxltd.geodash.layers.DssLayer;
import com.pxltd.geodash.layers.GD;
import com.pxltd.geodash.layers.HeatmapLayer;
import com.pxltd.geodash.layers.HurricaneLayer;
import com.pxltd.geodash.layers.KmlLayer;
import com.pxltd.geodash.layers.Layer;
import com.pxltd.geodash.layers.MarkerLayer;
import com.pxltd.geodash.layers.MassMarkerLayer;
import com.pxltd.geodash.layers.VectorLayer;
import com.pxltd.service.GeocodingService;
import com.pxltd.service.VectorLayerService;
import com.pxltd.spatial.Point;
import com.pxltd.util.Bbox;
import com.pxltd.util.GeoUtil;
import com.pxltd.util.XY;

public class ReportService extends BaseReportService {

	private GD gd = null;
	private GeocodingService gcs;
	private JSONArray sourceColumns = new JSONArray();
	private GeoshapingService geoShaper;
	private VectorLayerService vlService;
	public static final Pattern geoPattern = Pattern.compile("\\-?[0-9]+\\.?[0-9]*\\s*,\\s*\\-?[0-9]+\\.?[0-9]*");

	public ReportService(ViewBean vb) throws ServiceException {
		super(vb);
	}

	/**
	 * Creates a geodash instance object from the metadata definition. None of the layers will have their data populated but the definition will be set.
	 * 
	 * @return
	 * @throws ServiceException
	 */
	public GD getGeodashInstance() throws ServiceException {
		if (gd != null) {
			return gd;
		}
		// Get properties from the metadata first
		String prop = "";
		try {
			prop = GD.propsToJson(this.vb.getViewInstance().getVisualizationSettings().getProps());
			// System.out.println("prop:\n" + prop);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			setError(Level.SEVERE, "Could not read the visualization properties. + " + e.toString() + " : " + exceptionAsString, e.getMessage());
		}
		int p = prop.indexOf("mapType");

		// if the props are valid geodash then instantiate from props or create empty gd
		if (prop.equalsIgnoreCase("") || p == -1) {
			gd = new GD();
		} else {
			JSONObject gdj;
			try {
				gdj = new JSONObject(prop);
				gd = GD.getInstance(gdj);
			} catch (JSONException e) {
				setError(Level.SEVERE, "Geodash metadata definition is not valid.  If possible, please delete and recreate this report.", e.getMessage());
			}
		}
		// setup config settings
		gd.setClientID(GeodashConfig.GOOGLE_CLIENT_ID);
		gd.setGeodashAPIKey(GeodashConfig.GEODASH_API_KEY);
		gd.setIsSsl(Boolean.parseBoolean(GeodashConfig.USE_SSL));
		// populate geodash
		gd.setSourceColumns(getSourceColumns());
		// loop through and load the column objects for each
		// layer and validate its state.
		Iterator<Layer> it = gd.getLayers().iterator();
		while (it.hasNext()) {
			Layer l = it.next();
			if (!(l instanceof KmlLayer) && l.getSource().equalsIgnoreCase("current")) {
				populateColumns(l);
			}
		}

		return gd;
	}

	public String getSelectorID() throws ServiceException {
		WebTemplate wt = null;
		try {
			wt = vb.getViewInstance().getTemplate();
		} catch (Exception e) {
			setError(Level.SEVERE, "Could not retrieve template from viewBean.  Please check the template for problems.", e.getMessage());
		}
		WebAxis aRows = wt.getAxis(EnumDSSXMLAxisName.DssXmlAxisNameRows);
		return getWebObjectInfo(aRows.get(0)).getID();
	}

	public JSONObject saveLayer(Layer layer) throws ServiceException, JSONException {
		if (layer.isNew()) {
			layer.setID(getGeodashInstance().getNextLayerID());
		} else {
			getGeodashInstance().removeLayer(layer.getID());
		}
		getGeodashInstance().addLayer(layer);
		updateMetadataProperties();

		if (layer instanceof MarkerLayer) {
			MarkerLayer l = (MarkerLayer) layer;
			l.setState("ready");
			return l.toJSON();
		} else if (layer instanceof MassMarkerLayer) {
			MassMarkerLayer l = (MassMarkerLayer) layer;
			l.setState("ready");
			return l.toJSON();
		} else if (layer instanceof AreaLayer) {
			AreaLayer l = (AreaLayer) layer;
			l.setState("not_ready");
			return l.toJSON();
		} else if (layer instanceof KmlLayer) {
			KmlLayer l = (KmlLayer) layer;
			l.setState("ready");
			return l.toJSON();
		} else if (layer instanceof VectorLayer) {
			VectorLayer l = (VectorLayer) layer;
			l.setState("ready");
			return l.toJSON();
		} else if (layer instanceof HeatmapLayer) {
			HeatmapLayer l = (HeatmapLayer) layer;
			l.setState("ready");
			return l.toJSON();
		} else if (layer instanceof HurricaneLayer) {
			HurricaneLayer l = (HurricaneLayer) layer;
			l.setState("ready");
			return l.toJSON();
		} else if (layer instanceof DssLayer) {
			DssLayer l = (DssLayer) layer;
			l.setState("ready");
			return l.toJSON();
		} else {
			throw new ServiceException("Unknown layer type");
		}
	}

	public void deleteLayer(int id) throws ServiceException {
		getGeodashInstance().removeLayer(id);
		updateMetadataProperties();
	}

	public Layer getLayer(int id) throws ServiceException {
		return getGeodashInstance().getLayer(id);
	}

	// removes unnecessary attributes before saving props
	// to the mstr metadata.
	private void updateMetadataProperties() throws ServiceException {
		try {
			JSONObject gdj = getGeodashInstance().toJSON(true);
			gdj.remove("clientID");
			gdj.remove("gdAPIKey");
			gdj.remove("status");
			gdj.remove("errors");
			gdj.remove("sourceColumns");

			JSONArray layers = gdj.getJSONArray("layers");
			for (int i = 0; i < layers.length(); i++) {
				JSONObject lyr = layers.getJSONObject(i);
				lyr.remove("selector");
				lyr.remove("geom");
				lyr.remove("columns");
				lyr.remove("rows");
				lyr.remove("state");
				lyr.remove("errors");
				lyr.remove("layerKey");
			}

			// System.out.println("String: " + GD.toXmlString(gdj));
			vb.getViewInstance().getVisualizationSettings().setProps(GD.toXmlString(gdj));

		} catch (JSONException e) {
			setError(Level.SEVERE, "Could not convert geodash into serialized json.", e.getMessage());
		} catch (Exception e) {
			setError(Level.SEVERE, "A MicroStrategy error prevented saving this layer.  Check the logs.", e.getMessage());
		}
	}

	public Layer populateColumns(Layer l) throws ServiceException {
		JSONArray cols;
		cols = getSourceColumns();
		l.setColumns(cols);

		return l;
	}

	public BufferedImage getPopulatedTile(ViewBean vb, MassMarkerLayer layer) {
		RequestKeys k = vb.getBeanContext().getRequestKeys();
		String x = k.getValue("x");
		String y = k.getValue("y");
		String z = k.getValue("z");
		return getPopulatedTile(vb, layer, x, y, z);
	}

	public BufferedImage getPopulatedTile(ViewBean vb, MassMarkerLayer layer, String xString, String yString, String zString) {
		try {
			int x = Integer.parseInt(xString);
			int y = Integer.parseInt(yString);
			int z = Integer.parseInt(zString);
			layer = getPopulatedMassMarkerLayer(layer, vb);
			Bbox bbox = GeoUtil.bboxForTile(x, y, z);
			Bbox bboxWithNeighbours = GeoUtil.bboxForTileWithNeighbours(x, y, z);
			// System.out.println("------------------");
			// System.out.println("xyz: " + x + ", " + y + ", " + z);
			// System.out.println("bbox tl.lat: " + bbox.getTlLat() + " tl.lng: " + bbox.getTlLng());
			// System.out.println("bbox br.lat: " + bbox.getBrLat() + " br.lng: " + bbox.getBrLng());

			BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = image.createGraphics();

			XY tileTLMerc = GeoUtil.toMercatorCoords(bbox.getTlLat(), bbox.getTlLng());
			g.setComposite(AlphaComposite.SrcOver);

			// g.setColor(new Color(255, 0, 0, 255));
			// g.drawString("y: " + y + " lat: " + tlLat + " - " + brLat , 20, 20);
			// g.drawString("merc tl x,y:" + tileTLMerc.x + ", " + tileTLMerc.y , 20, 20);
			// g.drawString("x,y,z: " + x + ", " + y + ", " + z, 20, 40);

			if (layer.getColumns().length() == 0) {
				populateColumns(layer);
			}
			WebReportGrid wrg = vb.getViewInstance().getGridData().getWebReportGrid();
			Enumeration<WebHeaders> e = wrg.getRowHeaders().elements();
			double pointSize = 0;
			while (e.hasMoreElements()) {
				// process mstr columns
				WebHeaders whs = e.nextElement();
				Enumeration<WebHeader> wh = whs.elements();
				Point p = null;
				String color = "";
				int columnKey = 0;
				while (wh.hasMoreElements()) {
					WebHeader h = wh.nextElement();

					if (layer.getGeoKey() == columnKey) {
						p = geocode(h.getDisplayName());
					}
					if (layer.getColorMetricKey() == columnKey && layer.getColorType().equalsIgnoreCase("threshold")) {
						int c = h.getCssFormat().getIntValue("FormattingPatterns", "FillColor");
						color = HTMLHelper.convertColorDecimalToRGB(c);
					}
					if (layer.getSizeMetricKey() == columnKey) {
						pointSize = toDouble(h.getDisplayName());
					}
					columnKey++;
				}
				Enumeration<WebRowValue> wr = whs.getGridRow().elements();
				while (wr.hasMoreElements()) {
					WebRowValue wrv = wr.nextElement();

					if (layer.getGeoKey() == columnKey) {
						p = geocode(wrv.getValue());
					}
					if (layer.getColorMetricKey() == columnKey && layer.getColorType().equalsIgnoreCase("threshold")) {
						int c = wrv.getCssFormat().getIntValue("FormattingPatterns", "FillColor");
						color = HTMLHelper.convertColorDecimalToRGB(c);
					}
					if (layer.getSizeMetricKey() == columnKey) {
						pointSize = toDouble(wrv.getValue());
					}

					columnKey++;
				}

				p.setColor(color);
				p.setSize(pointSize);
				// System.out.println("Point: " + p + ", color: " + color + ", " + Color.decode(color));

				if (p.getLatitude() < bboxWithNeighbours.getTlLat() && p.getLatitude() > bboxWithNeighbours.getBrLat() && p.getLongitude() > bboxWithNeighbours.getTlLng() && p.getLongitude() < bboxWithNeighbours.getBrLng()) {

					double valueRange = layer.getMaxValue() - layer.getMinValue();
					int diameterRange = layer.getMaxMarkerWidth() - layer.getMinMarkerWidth();
					double diameterPercent = (pointSize - layer.getMinValue()) / valueRange;
					int diameter = layer.getMinMarkerWidth() + (new Double(diameterRange * diameterPercent)).intValue();
					// System.out.println("min, max, pointSize, diameter, minMarkerWidth:" + layer.getMinValue() + ", " + layer.getMaxValue() + ", " + pointSize + ", " + diameter + ", " + layer.getMinMarkerWidth());

					XY pointMerc = GeoUtil.toMercatorCoords(p.getLatitude(), p.getLongitude());

					int tx = ((int) Math.round(pointMerc.mercNormX() * Math.pow(2, z) * 256)) - ((int) Math.round(tileTLMerc.mercNormX() * Math.pow(2, z) * 256));
					int ty = ((int) Math.round(pointMerc.mercNormY() * Math.pow(2, z) * 256)) - ((int) Math.round(tileTLMerc.mercNormY() * Math.pow(2, z) * 256));

					Color c = GeoUtil.toAlpha(Color.decode("#" + color), (float) 0.5);
					g.setColor(c);

					// System.out.println("Drawing at: " + tx + ", " + ty);
					g.fillOval(tx - diameter / 2, ty - diameter / 2, diameter, diameter);
				}
			}

			g.dispose();

			return image;
		} catch (Exception e) {
			e.printStackTrace();
			layer.setState("failed");
			layer.addError(e.getMessage());
		}

		return null;

	}

	// Checks layer configuration is valid and populates
	// it with the row level data. Geocodes it as necessary.
	// should be called for the geodash getLayer action only.
	public MarkerLayer getPopulatedMarkerLayer(MarkerLayer layer, ViewBean lvb) {
		try {
			checkLayer(lvb, layer);
			if (layer.getColumns()== null || layer.getColumns().length() == 0) {
				populateColumns(layer);
			}
			WebReportGrid wrg = lvb.getViewInstance().getGridData().getWebReportGrid();
			Enumeration<WebHeaders> e = wrg.getRowHeaders().elements();
			double pointSize = 0;
			while (e.hasMoreElements()) {
				JSONArray row = new JSONArray();
				JSONArray rowOfElementIds = new JSONArray();
				// process mstr columns
				WebHeaders whs = e.nextElement();
				Enumeration<WebHeader> wh = whs.elements();
				Point p = null;
				String color = "";
				int columnKey = 0;
				while (wh.hasMoreElements()) {
					WebHeader h = wh.nextElement();
					row.put(h.getDisplayName());
					rowOfElementIds.put(WebElementHelper.toShortElementID(h.getWebElement().getID()));

					if (layer.getGeoKey() == columnKey) {
						p = geocode(h.getDisplayName());
					}
					if (layer.getColorMetricKey() == columnKey && layer.getColorType().equalsIgnoreCase("threshold")) {
						int c = h.getCssFormat().getIntValue("FormattingPatterns", "FillColor");
						color = HTMLHelper.convertColorDecimalToRGB(c);
					}
					if (layer.getSizeMetricKey() == columnKey) {
						pointSize = toDouble(h.getDisplayName());
					}
					columnKey++;
				}
				Enumeration<WebRowValue> wr = whs.getGridRow().elements();
				while (wr.hasMoreElements()) {
					WebRowValue wrv = wr.nextElement();
					row.put(wrv.getValue());
					rowOfElementIds.put("");

					if (layer.getGeoKey() == columnKey) {
						p = geocode(wrv.getValue());
					}
					if (layer.getColorMetricKey() == columnKey && layer.getColorType().equalsIgnoreCase("threshold")) {
						int c = wrv.getCssFormat().getIntValue("FormattingPatterns", "FillColor");
						color = HTMLHelper.convertColorDecimalToRGB(c);
					}
					if (layer.getSizeMetricKey() == columnKey) {
						pointSize = toDouble(wrv.getValue());
					}

					columnKey++;

				}
				p.setColor(color);
				p.setSize(pointSize);
				layer.addPoint(p);
				layer.addRow(row);
				layer.addRowOfElementIds(rowOfElementIds);
			}
			layer.setState("ready");
		} catch (Exception e) {
			e.printStackTrace();
			layer.setState("failed");
			layer.addError(e.getMessage());
		}
		return layer;
	}

	public MassMarkerLayer getPopulatedMassMarkerLayer(MassMarkerLayer layer, ViewBean lvb) {
		try {
			checkLayer(lvb, layer);
			if (layer.getColumns().length() == 0) {
				populateColumns(layer);
			}
			WebReportGrid wrg = lvb.getViewInstance().getGridData().getWebReportGrid();
			Enumeration<WebHeaders> e = wrg.getRowHeaders().elements();
			double pointSize = 0;
			while (e.hasMoreElements()) {
				JSONArray row = new JSONArray();
				JSONArray rowOfElementIds = new JSONArray();
				// process mstr columns
				WebHeaders whs = e.nextElement();
				Enumeration<WebHeader> wh = whs.elements();
				Point p = null;
				String color = "";
				int columnKey = 0;
				while (wh.hasMoreElements()) {
					WebHeader h = wh.nextElement();
					row.put(h.getDisplayName());
					rowOfElementIds.put(WebElementHelper.toShortElementID(h.getWebElement().getID()));

					if (layer.getGeoKey() == columnKey) {
						p = geocode(h.getDisplayName());
					}
					if (layer.getColorMetricKey() == columnKey && layer.getColorType().equalsIgnoreCase("threshold")) {
						int c = h.getCssFormat().getIntValue("FormattingPatterns", "FillColor");
						color = HTMLHelper.convertColorDecimalToRGB(c);
					}
					if (layer.getSizeMetricKey() == columnKey) {
						pointSize = toDouble(h.getDisplayName());
					}
					columnKey++;
				}
				Enumeration<WebRowValue> wr = whs.getGridRow().elements();
				while (wr.hasMoreElements()) {
					WebRowValue wrv = wr.nextElement();
					row.put(wrv.getValue());
					rowOfElementIds.put("");

					if (layer.getGeoKey() == columnKey) {
						p = geocode(wrv.getValue());
					}
					if (layer.getColorMetricKey() == columnKey && layer.getColorType().equalsIgnoreCase("threshold")) {
						int c = wrv.getCssFormat().getIntValue("FormattingPatterns", "FillColor");
						color = HTMLHelper.convertColorDecimalToRGB(c);
					}
					if (layer.getSizeMetricKey() == columnKey) {
						pointSize = toDouble(wrv.getValue());
					}

					columnKey++;
				}

				layer.updateMaxMin(pointSize);

				p.setColor(color);
				p.setSize(pointSize);
				layer.addPoint(p);
				// layer.addRow(row);
				layer.addRowOfElementIds(rowOfElementIds);
			}
			layer.setState("ready");
		} catch (Exception e) {
			e.printStackTrace();
			layer.setState("failed");
			layer.addError(e.getMessage());
		}
		return layer;
	}

	private JSONArray getSourceColumns() throws ServiceException {
		if (sourceColumns.length() > 0) {
			return sourceColumns;
		}
		WebTemplate wt = null;
		try {
			wt = vb.getViewInstance().getTemplate();
		} catch (Exception e) {
			setError(Level.SEVERE, "Could not retrieve template from viewBean.  Please check the template for problems.  If the " + "external report is prompted, make sure the source has the same prompts.", e.getMessage());
		}
		WebAxis aRows = wt.getAxis(EnumDSSXMLAxisName.DssXmlAxisNameRows);
		WebTemplateMetrics aCols = wt.getTemplateMetrics();
		SimpleList atts = wt.getTemplateAttributes();

		try {
			for (int i = 0; i < aRows.size(); i++) {
				WebTemplateAttribute wa = (WebTemplateAttribute) aRows.get(i).getTarget();
				String attID = wa.getAttributeInfo().getID();

				JSONObject c = new JSONObject();
				c.put("name", HTMLHelper.encode(aRows.get(i).getAlias()));
				c.put("type", "attribute");
				c.put("attID", attID);

				sourceColumns.put(c);
			}

			for (int j = 0; j < aCols.size(); j++) {
				JSONObject c = new JSONObject();
				c.put("name", HTMLHelper.encode(aCols.get(j).getAlias()));
				c.put("type", "metric");
				sourceColumns.put(c);
			}
		} catch (Exception e) {
			setError(Level.SEVERE, "Could not populate source columns.  Please check the template for problems.", e.getMessage());
		}
		return sourceColumns;
	}

	private Point geocode(String address) throws ServiceException, GeodashException {
		Matcher m = geoPattern.matcher(address);
		Point p = new Point();
		if (m.find()) {
			String[] points = address.split(",");
			double lat = Double.parseDouble(points[0]);
			double lng = Double.parseDouble(points[1]);
			p.setLatitude(lat);
			p.setLongitude(lng);
		} else {
			if (!GeodashConfig.WEBAPI.equals("google")) {
				p = new Point(address);
				p.setLongitude(0.0);
				p.setLatitude(0.0);
				// throw new GeodashException("Geocoding only supported with Google web API");
			} else {
				p = getGeocoder().geocode(address);
			}
		}
		return p;
	}

	private GeocodingService getGeocoder() throws GeodashException, ServiceException {

		if (this.gcs == null) {
			this.gcs = new GeocodingService();
		}
		return gcs;
	}

	public static WebObjectInfo getWebObjectInfo(WebTemplateUnit tu) {
		WebObjectInfo to = null;
		if (tu.getUnitType() == EnumDSSXMLTemplateUnitType.DssXmlTemplateAttribute) {
			to = ((WebTemplateAttribute) tu.getTarget()).getAttributeInfo();
		} else if (tu.getUnitType() == EnumDSSXMLTemplateUnitType.DssXmlTemplateConsolidation) {
			to = ((WebTemplateConsolidation) tu.getTarget()).getConsolidationInfo();
		} else if (tu.getUnitType() == EnumDSSXMLTemplateUnitType.DssXmlTemplateMetrics) {
			to = ((WebTemplateMetric) tu.getTarget()).getMetric();
		} else if (tu.getUnitType() == EnumDSSXMLTemplateUnitType.DssXmlTemplateCustomGroup) {
			to = ((WebTemplateCustomGroup) tu.getTarget()).getCustomGroupInfo();
		}
		return to;
	}

	// validates template configuration and the presence of require attributes and metrics.
	private void checkLayer(ViewBean lvb, Layer layer) throws ServiceException {
		WebViewInstance vi;
		try {
			vi = lvb.getViewInstance();
			WebAxis cols = vi.getTemplate().getColumns();
			// loop through each column object and make sure there are no attributes in the column
			Enumeration<WebTemplateUnit> c = cols.elements();
			while (c.hasMoreElements()) {
				WebTemplateUnit tu = c.nextElement();
				if (tu.getUnitType() != EnumDSSXMLTemplateUnitType.DssXmlTemplateMetrics) {
					throw new ServiceException("The template is not configured properly.  Please place all attributes/consolidations/custom groups in the row axis and all the metrics in the column axis.");
				}
			}
			// check and make sure the metrics arent on the rows
			WebAxis rows = vi.getTemplate().getRows();
			if (rows.containsMetrics()) {
				throw new ServiceException("The template is not configured properly.  Please place all attributes in the row axis and all the metrics in the column axis.");
			}
		} catch (ServiceException e) {
			throw new ServiceException(e.getMessage());
		} catch (Exception e) {
			setError(Level.SEVERE, "MicroStrategy error.  Unable to read template configuration from the underlying viewBean.", e.getMessage());
		}

	}

	public AreaLayer getPopulatedAreaLayer(AreaLayer layer, ViewBean lvb) {
		try {
			geoShaper = new GeoshapingService();
			checkLayer(lvb, layer);
			if (layer.getColumns().length() == 0) {
				populateColumns(layer);
			}
			WebGraph webGraph = lvb.getViewInstance().getGraphObject();
			WebGridData webGridData = lvb.getViewInstance().getGridData();
			WebReportGrid wrg = lvb.getViewInstance().getGridData().getWebReportGrid();

			WebGridHeaders webGridHeaders = wrg.getRowHeaders();
			Enumeration<WebHeaders> e = webGridHeaders.elements(); // one element per row

			int rowNum = 0;
			while (e.hasMoreElements()) {
				JSONArray row = new JSONArray();
				JSONArray rowOfElementIds = new JSONArray();
				// process mstr columns
				WebHeaders whs = e.nextElement();
				Enumeration<WebHeader> webHeadersElements = whs.elements();
				String color = "";
				String shapeName = "";
				int columnKey = 0;

				while (webHeadersElements.hasMoreElements()) {
					WebHeader h = webHeadersElements.nextElement();

					row.put(h.getDisplayName());
					rowOfElementIds.put(WebElementHelper.toShortElementID(h.getWebElement().getID()));

					if (layer.getGeoKey() == columnKey) {
						shapeName = h.getDisplayName();
					}
					if (layer.getColorMetricKey() == columnKey && layer.getColorType().equalsIgnoreCase("threshold")) {
						int c = h.getCssFormat().getIntValue("FormattingPatterns", "FillColor");
						color = HTMLHelper.convertColorDecimalToRGB(c);
					}
					columnKey++;
				}

				Enumeration<WebRowValue> wr = whs.getGridRow().elements(); // metrics
				while (wr.hasMoreElements()) {
					WebRowValue wrv = wr.nextElement();
					row.put(wrv.getValue());
					rowOfElementIds.put("");

					if (layer.getGeoKey() == columnKey) {
						shapeName = wrv.getValue();
					}
					if (layer.getColorMetricKey() == columnKey && layer.getColorType().equalsIgnoreCase("threshold")) {
						int c = wrv.getCssFormat().getIntValue("FormattingPatterns", "FillColor");
						color = HTMLHelper.convertColorDecimalToRGB(c);
					}
					columnKey++;
				}
				if (layer.getColorType().equalsIgnoreCase("static")) {
					color = layer.getStaticColor();
				}
				layer.addLayerGeom(geoShape(shapeName, color, rowNum));
				layer.addRow(row);
				layer.addRowOfElementIds(rowOfElementIds);
				rowNum++;
			}
			geoShaper.submitLayer();
			layer.setLayerKey(geoShaper.getLayerKey());
			layer.setState("not_ready");
		} catch (Exception e) {
			layer.setState("failed");
			layer.addError(e.getMessage());
		}

		return layer;
	}

	public VectorLayer getPopulatedVectorLayer(VectorLayer layer, ViewBean lvb) {
		try {
			vlService = new VectorLayerService();
			checkLayer(lvb, layer);
			if (layer.getColumns().length() == 0) {
				populateColumns(layer);
			}
			WebReportGrid wrg = lvb.getViewInstance().getGridData().getWebReportGrid();

			WebGridHeaders webGridHeaders = wrg.getRowHeaders();
			Enumeration<WebHeaders> e = webGridHeaders.elements(); // one element per row

			int rowNum = 0;
			while (e.hasMoreElements()) {
				JSONArray row = new JSONArray();
				JSONArray rowOfElementIds = new JSONArray();
				// process mstr columns
				WebHeaders whs = e.nextElement();
				Enumeration<WebHeader> webHeadersElements = whs.elements();
				String color = "";
				String shapeName = "";
				int columnKey = 0;

				while (webHeadersElements.hasMoreElements()) {
					WebHeader h = webHeadersElements.nextElement();

					row.put(h.getDisplayName());
					rowOfElementIds.put(WebElementHelper.toShortElementID(h.getWebElement().getID()));

					if (layer.getGeoKey() == columnKey) {
						shapeName = h.getDisplayName();
					}
					if (layer.getColorMetricKey() == columnKey && layer.getColorType().equalsIgnoreCase("threshold")) {
						int c = h.getCssFormat().getIntValue("FormattingPatterns", "FillColor");
						color = HTMLHelper.convertColorDecimalToRGB(c);
					}
					columnKey++;
				}

				Enumeration<WebRowValue> wr = whs.getGridRow().elements(); // metrics
				while (wr.hasMoreElements()) {
					WebRowValue wrv = wr.nextElement();
					row.put(wrv.getValue());
					rowOfElementIds.put("");

					if (layer.getGeoKey() == columnKey) {
						shapeName = wrv.getValue();
					}
					if (layer.getColorMetricKey() == columnKey && layer.getColorType().equalsIgnoreCase("threshold")) {
						int c = wrv.getCssFormat().getIntValue("FormattingPatterns", "FillColor");
						color = HTMLHelper.convertColorDecimalToRGB(c);
					}
					columnKey++;
				}
				if (layer.getColorType().equalsIgnoreCase("static")) {
					color = layer.getStaticColor();
				}
				// layer.addLayerGeom(geoShape(shapeName,color,rowNum));
				layer.addLayerGeom(geoShapeV(shapeName, color, rowNum));
				layer.addRow(row);
				layer.addRowOfElementIds(rowOfElementIds);
				rowNum++;
			}
			vlService.submitLayer();
			layer.setLayerKey(vlService.getLayerKey());
			layer.setState("not_ready");
		} catch (Exception e) {
			e.printStackTrace();
			layer.setState("failed");
			layer.addError(e.getMessage());
		}

		return layer;
	}

	public HeatmapLayer getPopulatedHeatmapLayer(HeatmapLayer layer, ViewBean lvb) {
		try {
			checkLayer(lvb, layer);
			if (layer.getColumns().length() == 0) {
				populateColumns(layer);
			}
			WebReportGrid wrg = lvb.getViewInstance().getGridData().getWebReportGrid();
			Enumeration<WebHeaders> e = wrg.getRowHeaders().elements();
			double pointSize = 0;
			while (e.hasMoreElements()) {
				JSONArray row = new JSONArray();
				JSONArray rowOfElementIds = new JSONArray();
				// process mstr columns
				WebHeaders whs = e.nextElement();
				Enumeration<WebHeader> wh = whs.elements();
				Point p = null;
				String color = "";
				int columnKey = 0;
				while (wh.hasMoreElements()) {
					WebHeader h = wh.nextElement();
					row.put(h.getDisplayName());
					rowOfElementIds.put(WebElementHelper.toShortElementID(h.getWebElement().getID()));

					if (layer.getGeoKey() == columnKey) {
						p = geocode(h.getDisplayName());
					}
					if (layer.getColorMetricKey() == columnKey && layer.getColorType().equalsIgnoreCase("threshold")) {
						int c = h.getCssFormat().getIntValue("FormattingPatterns", "FillColor");
						color = HTMLHelper.convertColorDecimalToRGB(c);
					}
					if (layer.getSizeMetricKey() == columnKey) {
						pointSize = toDouble(h.getDisplayName());
					}
					columnKey++;
				}
				Enumeration<WebRowValue> wr = whs.getGridRow().elements();
				while (wr.hasMoreElements()) {
					WebRowValue wrv = wr.nextElement();
					row.put(wrv.getValue());
					rowOfElementIds.put("");

					if (layer.getGeoKey() == columnKey) {
						p = geocode(wrv.getValue());
					}
					if (layer.getColorMetricKey() == columnKey && layer.getColorType().equalsIgnoreCase("threshold")) {
						int c = wrv.getCssFormat().getIntValue("FormattingPatterns", "FillColor");
						color = HTMLHelper.convertColorDecimalToRGB(c);
					}
					if (layer.getSizeMetricKey() == columnKey) {
						pointSize = toDouble(wrv.getValue());
					}

					columnKey++;

				}
				p.setColor(color);
				p.setSize(pointSize);
				layer.addPoint(p);
				layer.addRow(row);
				layer.addRowOfElementIds(rowOfElementIds);
			}
			layer.setState("ready");
		} catch (Exception e) {
			layer.setState("failed");
			layer.addError(e.getMessage());
		}
		return layer;
	}

	private JSONObject geoShapeV(String shapeName, String color, int row) throws JSONException {
		JSONObject geom = new JSONObject();
		vlService.addShape(shapeName, color, row);
		geom.put("color", color);
		geom.put("row", row);
		return geom;
	}

	private JSONObject geoShape(String shapeName, String color, int row) throws JSONException {
		JSONObject geom = new JSONObject();
		geoShaper.addShape(shapeName, color, row);
		geom.put("color", color);
		geom.put("row", row);
		return geom;
	}

	private double toDouble(String s) {
		try {
			NumberFormat format = NumberFormat.getInstance();
			Number number = format.parse(s);
			return number.doubleValue();
		} catch (NumberFormatException e) {
			// System.out.println("GeoDash: Error parsing number: " + s);
			return 0;
		} catch (ParseException e) {
			// System.out.println("GeoDash: Error parsing number: " + s);
			return 0;
		}
	}

}
