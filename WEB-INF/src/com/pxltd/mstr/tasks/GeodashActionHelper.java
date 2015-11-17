package com.pxltd.mstr.tasks;

import java.util.List;

import com.microstrategy.utils.StringUtils;
import com.microstrategy.utils.log.Level;
import com.microstrategy.web.beans.BeanContext;
import com.microstrategy.web.beans.BeanFactory;
import com.microstrategy.web.beans.MarkupOutput;
import com.microstrategy.web.beans.RWBean;
import com.microstrategy.web.beans.ResultSetBean;
import com.microstrategy.web.beans.ViewBean;
import com.microstrategy.web.beans.WebBeanException;
import com.microstrategy.web.objects.WebIServerSession;
import com.microstrategy.web.objects.WebObjectsException;
import com.microstrategy.web.objects.rw.RWGridGraphObject;
import com.microstrategy.web.objects.rw.RWInstance;
import com.microstrategy.web.transform.WebTransformException;

public class GeodashActionHelper {

	/**
	 * getResultSetBean
	 * 
	 * @param context
	 * @param session
	 * @param messageID
	 * @param isRWBean
	 * @return
	 * @throws WebBeanException
	 * @throws WebTransformException
	 */
	public static ResultSetBean getResultSetBean(BeanContext context, WebIServerSession session, String objectID, String messageID, boolean isRWBean) throws WebBeanException, WebTransformException {
		ResultSetBean bean = null;
		if (isRWBean) {
			bean = (ResultSetBean) BeanFactory.getInstance().newBean("RWBean");
		} else {
			bean = (ResultSetBean) BeanFactory.getInstance().newBean("ReportBean");
		}
		bean.setSessionInfo(session);
		if (StringUtils.isNotEmpty(objectID))
			bean.setObjectID(objectID);
		if (StringUtils.isNotEmpty(messageID))
			bean.setMessageID(messageID);
		bean.setBeanContext(context);
		bean.collectData();
		return bean;
	}

	/**
	 * getViewBeanByGridKey
	 * 
	 * @param bean
	 * @param gridKey
	 * @return
	 * @throws WebBeanException
	 * @throws WebObjectsException
	 */
	public static ViewBean getViewBeanByGridKey(RWBean bean, String gridKey) throws Exception {
		Log.logger.logp(Level.FINE, GeodashActionHelper.class.getName(), "getViewBeanByGridKey", "Entering with Grid Key:" + gridKey);
		ViewBean viewBean = null;
		RWInstance rwInstance = bean.getRWInstance();
		List<?> units = rwInstance.getData().findUnits(gridKey);
		if (!units.isEmpty()) {
			Object unit = units.get(0);
			if (unit instanceof RWGridGraphObject) {
				Log.logger.logp(Level.FINE, GeodashActionHelper.class.getName(), "getViewBeanByGridKey", "Found View Bean for Grid Key:" + gridKey);
				viewBean = bean.getViewBean((RWGridGraphObject) unit);
			}
			if (viewBean == null) {
				Log.logger.logp(Level.WARNING, GeodashActionHelper.class.getName(), "getViewBeanByGridKey", "Could not find View Bean for Grid Key:" + gridKey);
			}
		}
		return viewBean;
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
