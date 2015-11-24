package com.pxltd.mstr.tasks;

import com.microstrategy.utils.StringUtils;
import com.microstrategy.web.app.beans.AppContext;
import com.microstrategy.web.app.tasks.AbstractAppTask;
import com.microstrategy.web.app.tasks.AppTaskRequestContext;
import com.microstrategy.web.app.tasks.architect.json.JSONException;
import com.microstrategy.web.app.tasks.architect.json.JSONObject;
import com.microstrategy.web.app.utils.HTMLHelper;
import com.microstrategy.web.beans.MarkupOutput;
import com.microstrategy.web.beans.RWBean;
import com.microstrategy.web.beans.ReportBean;
import com.microstrategy.web.beans.ResultSetBean;
import com.microstrategy.web.beans.ViewBean;
import com.microstrategy.web.beans.WebBeanException;
import com.microstrategy.web.objects.WebIServerSession;
import com.microstrategy.web.objects.WebObjectsException;
import com.microstrategy.web.objects.WebReportValidationException;
import com.microstrategy.web.tasks.TaskException;
import com.microstrategy.web.tasks.TaskParameterMetadata;
import com.microstrategy.web.tasks.TaskRequestContext;
import com.pxltd.geodash.ServiceException;
import com.pxltd.geodash.layers.GD;
import com.pxltd.geodash.layers.Layer;
import com.pxltd.service.mstr.ReportService;

public class GeodashActionSaveLayerTask extends AbstractAppTask {
	private TaskParameterMetadata layerParam;
	private TaskParameterMetadata gridKeyParam;
	private TaskParameterMetadata messageIDParam;
	private TaskParameterMetadata objectIDParam;

	public GeodashActionSaveLayerTask() {
		super("Geodash task to save layer");
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
		String objectID = objectIDParam.getValue(context.getRequestKeys());
		String gridKey = gridKeyParam.getValue(context.getRequestKeys());
		WebIServerSession session = context.getWebIServerSession("SessionState", null);
		try {
			AppContext appContext = ((AppTaskRequestContext) context).getAppContext();
			ResultSetBean bean = GeodashActionHelper.getResultSetBean(appContext, session, objectID, messageID, StringUtils.isNotEmpty(gridKey));
			if (bean != null) {
				renderSaveLayer(context, out, bean);
			} else {
				throw new Exception("Could not get an instance of the referenced messageID");
			}
		} catch (Exception e) {
			GeodashActionHelper.renderError(out, e);
		}
	}

	/**
	 * renderSaveLayer
	 * 
	 * @param context
	 * @param out
	 * @param rwBean
	 * @throws JSONException
	 * @throws WebBeanException
	 * @throws WebObjectsException
	 * @throws ServiceException
	 * @throws WebReportValidationException
	 */
	private void renderSaveLayer(TaskRequestContext context, MarkupOutput out, ResultSetBean bean) throws Exception, JSONException, WebBeanException, WebObjectsException, ServiceException, WebReportValidationException {
		JSONObject msg = new JSONObject();
		JSONObject jl = new JSONObject(layerParam.getValue(context.getRequestKeys()));
		Layer l = GD.getLayerInstance(jl);
		l.setName(HTMLHelper.encode(l.getName()));
		String gridKey = gridKeyParam.getValue(context.getRequestKeys());
		ViewBean viewBean = null;
		if (StringUtils.isNotEmpty(gridKey)) {
			viewBean = GeodashActionHelper.getViewBeanByGridKey((RWBean) bean, gridKey);
		} else {
			viewBean = ((ReportBean) bean).getViewBean();
		}
		if (viewBean == null) {
			throw new Exception("Could not get an instance of the referenced messageID");
		}
		ReportService rs = new ReportService(viewBean);
		JSONObject res = rs.saveLayer(l);
		if (StringUtils.isNotEmpty(gridKey)) {
			((RWBean) bean).setRWInstance(((RWBean) bean).getRWInstance().getRWManipulator().applyChanges());
		} else {
			((ReportBean) bean).setReportInstance(((ReportBean) bean).getReportInstance().getReportManipulator().applyChanges());
		}
		String messageId = bean.getMessageID();
		msg.put("messageID", messageId);
		if (l.getSource().equalsIgnoreCase("current")) {
			l = rs.populateColumns(l);
			res.put("columns", l.getColumns());
		} else {
			res.put("columns", jl.optJSONArray("columns"));
		}
		msg.put("layer", res);
		out.append(msg.toString());
	}
}
