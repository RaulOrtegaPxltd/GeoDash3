package com.pxltd.mstr.tasks;

import com.microstrategy.utils.StringUtils;
import com.microstrategy.web.app.beans.AppContext;
import com.microstrategy.web.app.tasks.AbstractAppTask;
import com.microstrategy.web.app.tasks.AppTaskRequestContext;
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
import com.pxltd.geodash.layers.GD;
import com.pxltd.geodash.layers.HeatmapLayer;
import com.pxltd.geodash.layers.HurricaneLayer;
import com.pxltd.geodash.layers.KmlLayer;
import com.pxltd.geodash.layers.Layer;
import com.pxltd.geodash.layers.MarkerLayer;
import com.pxltd.geodash.layers.VectorLayer;
import com.pxltd.service.mstr.ReportService;

public class GeodashActionGetLayerTask extends AbstractAppTask {
	private TaskParameterMetadata layerParam;
	private TaskParameterMetadata messageIDParam;
	private TaskParameterMetadata objectIDParam;
	private TaskParameterMetadata gridKeyParam;

	public GeodashActionGetLayerTask() {
		super("Geodash task to get layer");
		addSessionStateParam(true, null);
		layerParam = addParameterMetadata("layer", "JSON of layer", true, null);
		messageIDParam = addParameterMetadata("messageID", "The messageID", false, null);
		objectIDParam = addParameterMetadata("objectID", "The objectID", false, null);
		gridKeyParam = addParameterMetadata("gridKey", "The grid key if the messageID references a document instance", false, null);
	}

	@Override
	public void processRequest(TaskRequestContext context, MarkupOutput out) throws TaskException {
		checkForRequiredParameters(context.getRequestKeys());
		String messageID = messageIDParam.getValue(context.getRequestKeys());
		String reportID = objectIDParam.getValue(context.getRequestKeys());
		String gridKey = gridKeyParam.getValue(context.getRequestKeys());
		WebIServerSession session = context.getWebIServerSession("sessionState", null);
		try {
			AppContext appContext = ((AppTaskRequestContext) context).getAppContext();
			ResultSetBean bean = GeodashActionHelper.getResultSetBean(appContext, session, reportID, messageID, StringUtils.isNotEmpty(gridKey));
			if (bean != null) {
				ViewBean viewBean = null;
				if (StringUtils.isNotEmpty(gridKey)) {
					viewBean = GeodashActionHelper.getViewBeanByGridKey((RWBean) bean, gridKey);
				} else {
					viewBean = ((ReportBean) bean).getViewBean();
				}
				if (viewBean != null) {
					renderGetLayer(context, out, viewBean);
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

	/**
	 * renderGetLayer
	 * 
	 * @param context
	 * @param out
	 * @param viewBean
	 * @throws JSONException
	 * @throws ServiceException
	 */
	private void renderGetLayer(TaskRequestContext context, MarkupOutput out, ViewBean viewBean) throws JSONException, ServiceException {
		JSONObject jl = new JSONObject(layerParam.getValue(context.getRequestKeys()));
		Layer l = GD.getLayerInstance(jl);
		ReportService rs = new ReportService(viewBean);
		if (l instanceof MarkerLayer) {
			MarkerLayer ml = rs.getPopulatedMarkerLayer((MarkerLayer) l, viewBean);
			out.append(ml.toJSON().toString());
		} else if (l instanceof AreaLayer) {
			AreaLayer al = rs.getPopulatedAreaLayer((AreaLayer) l, viewBean);
			out.append(al.toJSON().toString());
		} else if (l instanceof VectorLayer) {
			VectorLayer vl = rs.getPopulatedVectorLayer((VectorLayer) l, viewBean);
			out.append(vl.toJSON().toString());
		} else if (l instanceof HeatmapLayer) {
			HeatmapLayer hl = rs.getPopulatedHeatmapLayer((HeatmapLayer) l, viewBean);
			out.append(hl.toJSON().toString());
		} else if (l instanceof KmlLayer) {
			out.append(((KmlLayer) l).toJSON().toString());
		} else if (l instanceof HurricaneLayer) {
			out.append(((HurricaneLayer) l).toJSON().toString());
		} else if (l instanceof DssLayer) {
			out.append(((DssLayer) l).toJSON().toString());
		}
	}
}