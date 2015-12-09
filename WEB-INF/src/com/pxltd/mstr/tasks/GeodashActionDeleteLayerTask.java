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
import com.microstrategy.web.beans.WebBeanException;
import com.microstrategy.web.objects.WebIServerSession;
import com.microstrategy.web.objects.WebObjectsException;
import com.microstrategy.web.objects.WebReportValidationException;
import com.microstrategy.web.tasks.TaskException;
import com.microstrategy.web.tasks.TaskParameterMetadata;
import com.microstrategy.web.tasks.TaskRequestContext;
import com.pxltd.geodash.ServiceException;
import com.pxltd.service.mstr.ReportService;

public class GeodashActionDeleteLayerTask extends AbstractAppTask {
	private TaskParameterMetadata layerParam;
	private TaskParameterMetadata gridKeyParam;
	private TaskParameterMetadata messageIDParam;
	private TaskParameterMetadata objectIDParam;

	public GeodashActionDeleteLayerTask() {
		super("Geodash action task to delete layer");
		addSessionStateParam(true, null);
		layerParam = addParameterMetadata("layer", "Id of layer to delete", true, null);
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
		WebIServerSession session = context.getWebIServerSession("sessionState", null);
		try {
			AppContext appContext = ((AppTaskRequestContext) context).getAppContext();
			ResultSetBean bean = GeodashActionHelper.getResultSetBean(appContext, session, objectID, messageID, StringUtils.isNotEmpty(gridKey));
			if (bean != null) {
				renderDeleteLayer(context, out, bean);
			} else {
				throw new Exception("Could not get an instance of the referenced messageID");
			}
		} catch (Exception e) {
			GeodashActionHelper.renderError(out, e);
		}
	}

	/**
	 * renderDeleteLayer
	 * 
	 * @param context
	 * @param out
	 * @param rwBean
	 * @throws WebBeanException
	 * @throws WebObjectsException
	 * @throws ServiceException
	 * @throws JSONException
	 * @throws WebReportValidationException
	 */
	private void renderDeleteLayer(TaskRequestContext context, MarkupOutput out, ResultSetBean bean) throws Exception, WebBeanException, WebObjectsException, ServiceException, JSONException, WebReportValidationException {
		String gridKey = gridKeyParam.getValue(context.getRequestKeys());
		ViewBean viewBean = null;
		if (StringUtils.isEmpty(gridKey)) {
			viewBean = ((ReportBean) bean).getViewBean();
		} else {
			viewBean = GeodashActionHelper.getViewBeanByGridKey((RWBean) bean, gridKey);
		}
		if(viewBean == null){
			throw new Exception("Could not get an instance of the referenced messageID");
		}
		ReportService rs = new ReportService(viewBean);
		String kLayer = layerParam.getValue(context.getRequestKeys());
		int id = Integer.parseInt(kLayer);
		rs.deleteLayer(id);
		if (StringUtils.isEmpty(gridKey)) {
			((ReportBean) bean).setReportInstance(((ReportBean) bean).getReportInstance().getReportManipulator().applyChanges());
		} else {
			((RWBean) bean).setRWInstance(((RWBean) bean).getRWInstance().getRWManipulator().applyChanges());
		}
		JSONObject msg = new JSONObject();
		msg.put("state", "ready");
		String[] info = { "successfully deleted layer with id: " + id };
		msg.put("info", info);
		msg.put("messageID", bean.getMessageID());
		out.append(msg.toString());
	}

}
