package com.pxltd.mstr.tasks;

import com.microstrategy.utils.StringUtils;
import com.microstrategy.web.beans.BeanContext;
import com.microstrategy.web.beans.BeanFactory;
import com.microstrategy.web.beans.MarkupOutput;
import com.microstrategy.web.beans.ReportBean;
import com.microstrategy.web.beans.ResultSetBean;
import com.microstrategy.web.beans.WebBeanException;
import com.microstrategy.web.objects.WebIServerSession;
import com.microstrategy.web.transform.WebTransformException;

public class GeodashActionHelper {

	/**
	 * getReportBean
	 * 
	 * @param context
	 * @param session
	 * @param reportID
	 * @param messageID
	 * @return
	 * @throws WebBeanException
	 * @throws WebTransformException
	 */
	public static ResultSetBean getReportBean(BeanContext context, WebIServerSession session, String reportID, String messageID) throws WebBeanException, WebTransformException {
		ReportBean bean = null;
		bean = (ReportBean) BeanFactory.getInstance().newBean("ReportBean");
		bean.setSessionInfo(session);
		if (StringUtils.isNotEmpty(reportID))
			bean.setObjectID(reportID);
		if (StringUtils.isNotEmpty(messageID))
			bean.setMessageID(messageID);
		bean.setBeanContext(context);
		bean.collectData();
		return bean;
	}
	
	/**
	 * renderError
	 * 
	 * @param out
	 * @param e
	 */
	public static void renderError(MarkupOutput out, Exception e) {
		String er = "{\"state\": \"failed\",\"errors\":[\"" + e.getMessage() + "\"]}";
		out.append(er);
	}

}
