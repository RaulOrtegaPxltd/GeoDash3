package com.pxltd.mstr.tasks;

import com.microstrategy.web.app.tasks.AbstractAppTask;
import com.microstrategy.web.app.tasks.architect.json.JSONObject;
import com.microstrategy.web.beans.MarkupOutput;
import com.microstrategy.web.objects.WebIServerSession;
import com.microstrategy.web.tasks.TaskException;
import com.microstrategy.web.tasks.TaskRequestContext;
import com.pxltd.geodash.GeodashConfig;

public class GeodashActionGetSettingsTask extends AbstractAppTask {

	public GeodashActionGetSettingsTask() {
		super("Geodash task to get settings");
		addSessionStateParam(true, null);
	}

	@Override
	public void processRequest(TaskRequestContext context, MarkupOutput out) throws TaskException {
		checkForRequiredParameters(context.getRequestKeys());
		WebIServerSession session = context.getWebIServerSession("SessionState", null);
		try {
			if (session.isAlive()) {
				renderSettings(out);
			} else {
				throw new Exception("Not a valid Session");
			}
		} catch (Exception e) {
			throw new TaskException("Not a valid Session");
		}
	}

	/***
	 * renderSettings
	 * 
	 * @param out
	 * @throws Exception
	 */
	private void renderSettings(MarkupOutput out) throws Exception {
		JSONObject j = new JSONObject();
		j.put("geoDashAPIKey", GeodashConfig.GEODASH_API_KEY);
		j.put("geoDashLicenseKey", GeodashConfig.GEODASH_LICENSE_KEY);
		j.put("geoDashGroupID", GeodashConfig.GEODASH_GROUP_ID);
		j.put("geoDashAdminGroupID", GeodashConfig.GEODASH_ADMIN_GROUP_ID);
		j.put("googleClientID", GeodashConfig.GOOGLE_CLIENT_ID);
		j.put("googlePrivateKey", GeodashConfig.GOOGLE_PRIVATE_KEY);
		j.put("googleRegionBias", GeodashConfig.GOOGLE_REGION_BIAS);
		j.put("useSSL", GeodashConfig.USE_SSL);
		j.put("googleAPIKey", GeodashConfig.GOOGLE_API_KEY);
		j.put("googleIOSKey", GeodashConfig.GOOGLE_IOS_KEY);
		out.append(j.toString());
	}
}