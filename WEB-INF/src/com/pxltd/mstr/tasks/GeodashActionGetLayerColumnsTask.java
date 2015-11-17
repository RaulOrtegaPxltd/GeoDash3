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
import com.pxltd.geodash.GeodashException;
import com.pxltd.geodash.ServiceException;
import com.pxltd.geodash.layers.Layer;
import com.pxltd.service.mstr.ReportService;

public class GeodashActionGetLayerColumnsTask extends AbstractAppTask {
	private TaskParameterMetadata gridKeyParam;
	private TaskParameterMetadata messageIDParam;
	private TaskParameterMetadata objectIDParam;

	public GeodashActionGetLayerColumnsTask() {
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
					renderLayerColumns(out, viewBean);
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

	/***
	 * renderSettings
	 * 
	 * @param out
	 * @param viewBean
	 * @throws ServiceException
	 * @throws JSONException
	 * @throws GeodashException
	 */
	private void renderLayerColumns(MarkupOutput out, ViewBean viewBean) throws ServiceException, JSONException, GeodashException {
		ReportService rs = new ReportService(viewBean);
		Layer l = rs.populateColumns(new Layer());
		JSONObject j = new JSONObject();
		j.put("state", "ready");
		j.put("columns", l.getColumns());
		out.append(j.toString());
	}
}