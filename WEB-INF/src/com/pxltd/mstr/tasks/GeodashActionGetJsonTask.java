package com.pxltd.mstr.tasks;

import com.microstrategy.utils.StringUtils;
import com.microstrategy.web.app.beans.AppContext;
import com.microstrategy.web.app.tasks.AbstractAppTask;
import com.microstrategy.web.app.tasks.AppTaskRequestContext;
import com.microstrategy.web.beans.MarkupOutput;
import com.microstrategy.web.beans.RWBean;
import com.microstrategy.web.beans.ReportBean;
import com.microstrategy.web.beans.ResultSetBean;
import com.microstrategy.web.beans.ViewBean;
import com.microstrategy.web.beans.WebBeanException;
import com.microstrategy.web.objects.WebIServerSession;
import com.microstrategy.web.objects.WebObjectsException;
import com.microstrategy.web.tasks.TaskException;
import com.microstrategy.web.tasks.TaskParameterMetadata;
import com.microstrategy.web.tasks.TaskRequestContext;
import com.pxltd.geodash.layers.GD;

public class GeodashActionGetJsonTask extends AbstractAppTask {

	private TaskParameterMetadata messageIDParam;
	private TaskParameterMetadata gridKeyParam;
	
	public GeodashActionGetJsonTask() {
		super("Geodash task to get Geodash Json");
		addSessionStateParam(true, null);
		messageIDParam = addParameterMetadata("messageID", "The messageID", false, null);
		gridKeyParam = addParameterMetadata("gridKey", "The grid key if the messageID references a document instance", false, null);
	}
	
	public void processRequest(TaskRequestContext context, MarkupOutput out) throws TaskException {
		checkForRequiredParameters(context.getRequestKeys());
		String messageID = messageIDParam.getValue(context.getRequestKeys());
		String gridKey = gridKeyParam.getValue(context.getRequestKeys());
		WebIServerSession session = context.getWebIServerSession("sessionState", null);
		
		try {
			AppContext appContext = ((AppTaskRequestContext) context).getAppContext();
			ResultSetBean bean = GeodashActionHelper.getResultSetBean(appContext, session, null, messageID, StringUtils.isNotEmpty(gridKey));
			if (bean != null) {
				ViewBean viewBean = null;
				if (StringUtils.isNotEmpty(gridKey)) {
					viewBean = GeodashActionHelper.getViewBeanByGridKey((RWBean) bean, gridKey);
				} else {
					viewBean = ((ReportBean) bean).getViewBean();
				}
				if (viewBean != null) {
					renderGetJson(context, out, viewBean);
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

	private void renderGetJson(TaskRequestContext context, MarkupOutput out, ViewBean viewBean) {
		String props = "";
		
		try {
			props = GD.propsToJson(viewBean.getViewInstance().getVisualizationSettings().getProps());
		} catch (WebObjectsException | WebBeanException e) {
			GeodashActionHelper.renderError(out, e);
		}
		out.append(props);
	}
}
