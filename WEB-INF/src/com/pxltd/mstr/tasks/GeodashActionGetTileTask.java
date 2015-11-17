package com.pxltd.mstr.tasks;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

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
import com.pxltd.geodash.layers.GD;
import com.pxltd.geodash.layers.MassMarkerLayer;
import com.pxltd.service.mstr.ReportService;
import com.pxltd.util.Base64;

public class GeodashActionGetTileTask extends AbstractAppTask {
	private TaskParameterMetadata layerParam;
	private TaskParameterMetadata gridKeyParam;
	private TaskParameterMetadata objectIDParam;
	private TaskParameterMetadata messageIDParam;
	private TaskParameterMetadata xParam;
	private TaskParameterMetadata yParam;
	private TaskParameterMetadata zParam;

	public GeodashActionGetTileTask() {
		super("Geodash task to get title");
		addSessionStateParam(true, null);
		layerParam = addParameterMetadata("layer", "JSON of layer", true, null);
		messageIDParam = addParameterMetadata("messageID", "the messageID", false, null);
		objectIDParam = addParameterMetadata("objectID", "The objectID", false, null);
		gridKeyParam = addParameterMetadata("gridKey", "The grid key if the messageID/objectID references a document instance", false, null);
		xParam = addParameterMetadata("x", "the x", true, null);
		yParam = addParameterMetadata("y", "the x", true, null);
		zParam = addParameterMetadata("z", "the x", true, null);
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
					renderTile(context, out, viewBean);
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
	 * renderTitle
	 * 
	 * @param context
	 * @param out
	 * @param viewBean
	 * @throws ServiceException
	 * @throws JSONException
	 * @throws IOException
	 */
	public void renderTile(TaskRequestContext context, MarkupOutput out, ViewBean viewBean) throws ServiceException, JSONException, IOException {
		JSONObject j = new JSONObject();
		ReportService rs = new ReportService(viewBean);
		JSONObject jl = new JSONObject(layerParam.getValue(context.getRequestKeys()));
		String x = xParam.getValue(context.getRequestKeys());
		String y = yParam.getValue(context.getRequestKeys());
		String z = zParam.getValue(context.getRequestKeys());
		MassMarkerLayer l = (MassMarkerLayer) GD.getLayerInstance(jl);
		BufferedImage image = rs.getPopulatedTile(viewBean, l, x, y, z);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "png", baos);
		byte[] bytes = baos.toByteArray();
		String base64str = Base64.encodeBytes(bytes);
		j.put("image", "data:image/png;base64," + base64str);
		out.append(j.toString());
	}
}
