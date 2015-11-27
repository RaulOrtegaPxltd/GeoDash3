package com.pxltd.mstr.tasks;

import java.util.Iterator;

import com.microstrategy.utils.StringUtils;
import com.microstrategy.web.app.beans.AppContext;
import com.microstrategy.web.app.tasks.AbstractAppTask;
import com.microstrategy.web.app.tasks.AppTaskRequestContext;
import com.microstrategy.web.app.tasks.architect.json.JSONArray;
import com.microstrategy.web.app.tasks.architect.json.JSONException;
import com.microstrategy.web.app.tasks.architect.json.JSONObject;
import com.microstrategy.web.beans.MarkupOutput;
import com.microstrategy.web.beans.RWBean;
import com.microstrategy.web.beans.ReportBean;
import com.microstrategy.web.beans.ResultSetBean;
import com.microstrategy.web.beans.ViewBean;
import com.microstrategy.web.objects.WebIServerSession;
import com.microstrategy.web.tasks.TaskException;
import com.microstrategy.web.tasks.TaskParameterMetadata;
import com.microstrategy.web.tasks.TaskRequestContext;
import com.pxltd.geodash.ServiceException;
import com.pxltd.geodash.layers.AreaLayer;
import com.pxltd.geodash.layers.DssLayer;
import com.pxltd.geodash.layers.HeatmapLayer;
import com.pxltd.geodash.layers.HurricaneLayer;
import com.pxltd.geodash.layers.KmlLayer;
import com.pxltd.geodash.layers.Layer;
import com.pxltd.geodash.layers.MarkerLayer;
import com.pxltd.geodash.layers.MassMarkerLayer;
import com.pxltd.geodash.layers.VectorLayer;
import com.pxltd.service.mstr.ReportService;

public class GeodashActionGetLayersTask extends AbstractAppTask {
	private TaskParameterMetadata gridKeyParam;
	private TaskParameterMetadata messageIDParam;
	private TaskParameterMetadata objectIDParam;

	public GeodashActionGetLayersTask() {
		super("Geodash task to get layer columns");
		addSessionStateParam(true, null);
		messageIDParam = addParameterMetadata("messageID", "the messageID", false, null);
		objectIDParam = addParameterMetadata("objectID", "The objectID", false, null);
		gridKeyParam = addParameterMetadata("gridKey", "The grid key if the messageID references a document instance", false, null);
	}

	@Override
	public void processRequest(TaskRequestContext context, MarkupOutput out) throws TaskException {
		checkForRequiredParameters(context.getRequestKeys());
		String messageID = messageIDParam.getValue(context.getRequestKeys());
		String objectID = objectIDParam.getValue(context.getRequestKeys());
		String gridKey = gridKeyParam.getValue(context.getRequestKeys());
		WebIServerSession session = context.getWebIServerSession("SessionState", null);
		try {
			AppContext appContext = ((AppTaskRequestContext) context).getAppContext();
			ResultSetBean bean = GeodashActionHelper.getResultSetBean(appContext, session, objectID, messageID, StringUtils.isNotEmpty(gridKey));
			if (bean != null) {
				ViewBean viewBean = null;
				if (StringUtils.isNotEmpty(gridKey)) {
					viewBean = GeodashActionHelper.getViewBeanByGridKey((RWBean) bean, gridKey);
				} else {
					viewBean = ((ReportBean) bean).getViewBean();
				}
				if (viewBean != null) {
					renderLayers(out, viewBean);
				} else {
					throw new Exception("Could not get an instance of the referenced messageID");
				}
			} else {
				throw new Exception("Could not get an instance of the referenced messageID");
			}
		} catch (Exception e) {
			GeodashActionHelper.renderError(out, e);
		}
	}

	public void renderLayers(MarkupOutput out, ViewBean viewBean) throws ServiceException {
		ReportService rs = new ReportService(viewBean);
		Iterator<Layer> it = rs.getGeodashInstance().getLayers().iterator();
		JSONArray layers = new JSONArray();
		while (it.hasNext()) {
			JSONObject layer = new JSONObject();
			try {
				Layer l = it.next();
				if (l instanceof MarkerLayer) {
					MarkerLayer ml = ((MarkerLayer) l);
					JSONObject mj = ml.toJSON();
					layer.put("layer", mj);
					layer.put("scriptClass", "bdl.geodash.MarkerLayer");
					// out.append("gd.layers.add(new bdl.geodash.MarkerLayer(" + mj.toString() + "));");
				} else if (l instanceof MassMarkerLayer) {
					MassMarkerLayer mml = ((MassMarkerLayer) l);
					JSONObject aj = mml.toJSON();
					layer.put("layer", aj);
					layer.put("scriptClass", "bdl.geodash.MassMarkerLayer");
					// out.append("gd.layers.add(new bdl.geodash.MassMarkerLayer(" + aj.toString() + "));");
				} else if (l instanceof AreaLayer) {
					AreaLayer al = ((AreaLayer) l);
					JSONObject aj = al.toJSON();
					layer.put("layer", aj);
					layer.put("scriptClass", "bdl.geodash.AreaLayer");
					// out.append("gd.layers.add(new bdl.geodash.AreaLayer(" + aj.toString() + "));");
				} else if (l instanceof KmlLayer) {
					KmlLayer al = ((KmlLayer) l);
					JSONObject aj = al.toJSON();
					layer.put("layer", aj);
					layer.put("scriptClass", "bdl.geodash.KmlLayer");
					// out.append("gd.layers.add(new bdl.geodash.KmlLayer(" + aj.toString() + "));");
				} else if (l instanceof VectorLayer) {
					VectorLayer vl = ((VectorLayer) l);
					JSONObject vj = vl.toJSON();
					layer.put("layer", vj);
					layer.put("scriptClass", "bdl.geodash.VectorLayer");
					// out.append("gd.layers.add(new bdl.geodash.VectorLayer(" + vj.toString() + "));");
				} else if (l instanceof HeatmapLayer) {
					HeatmapLayer hl = ((HeatmapLayer) l);
					JSONObject vj = hl.toJSON();
					layer.put("layer", vj);
					layer.put("scriptClass", "bdl.geodash.HeatmapLayer");
					// out.append("gd.layers.add(new bdl.geodash.HeatmapLayer(" + vj.toString() + "));");
				} else if (l instanceof HurricaneLayer) {
					HurricaneLayer hl = ((HurricaneLayer) l);
					JSONObject vj = hl.toJSON();
					layer.put("layer", vj);
					layer.put("scriptClass", "bdl.geodash.HurricaneLayer");
					// out.append("gd.layers.add(new bdl.geodash.HurricaneLayer(" + vj.toString() + "));");
				} else if (l instanceof DssLayer) {
					DssLayer hl = ((DssLayer) l);
					JSONObject vj = hl.toJSON();
					layer.put("layer", vj);
					layer.put("scriptClass", "bdl.geodash.DssLayer");
					// out.append("gd.layers.add(new bdl.geodash.DssLayer(" + vj.toString() + "));");
				}
			} catch (JSONException j) {
				try {
					layer = new JSONObject("{\"scriptClass\":\"bdl.geodash.Layer\", \"layer\":{\"state\": \"failed\",\"errors\":[\"" + j.getMessage() + "\"]}}");
				} catch (JSONException e) {
					layer = null;
				}
			}
			if (layer != null)
				layers.put(layer);
		}
		out.append(layers.toString());
	}

}