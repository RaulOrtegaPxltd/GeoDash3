package com.pxltd.mstr.tasks;

import java.util.HashMap;
import java.util.Map;

import com.microstrategy.web.app.tasks.AbstractAppTask;
import com.microstrategy.web.app.tasks.architect.json.JSONObject;
import com.microstrategy.web.beans.MarkupOutput;
import com.microstrategy.web.objects.WebIServerSession;
import com.microstrategy.web.objects.WebObjectSource;
import com.microstrategy.web.objects.WebObjectsException;
import com.microstrategy.web.objects.WebObjectsFactory;
import com.microstrategy.web.objects.admin.users.WebUser;
import com.microstrategy.web.objects.admin.users.WebUserGroup;
import com.microstrategy.web.tasks.TaskException;
import com.microstrategy.web.tasks.TaskRequestContext;
import com.microstrategy.webapi.EnumDSSXMLObjectTypes;
import com.pxltd.geodash.GeodashConfig;
import com.pxltd.geodash.LicenseCheck;
import com.pxltd.geodash.LicenseManager;

public class GeodashActionGetSettingsTask extends AbstractAppTask {

	public GeodashActionGetSettingsTask() {
		super("Geodash task to get settings");
		addSessionStateParam(true, null);
	}

	@Override
	public void processRequest(TaskRequestContext context, MarkupOutput out) throws TaskException {
		checkForRequiredParameters(context.getRequestKeys());
		WebIServerSession session = context.getWebIServerSession("sessionState", null);
		try {
			if (session.isAlive()) {
				if (validateGeodashPrivileges(session)) {
					renderSettings(out);
				} else {
					throw new Exception("User does not have the privilege to use Geodash");
				}
			} else {
				throw new Exception("Not a valid Session");
			}
		} catch (Exception e) {
			throw new TaskException(e.getMessage());
		}
	}

	/**
	 * renderSettings
	 * 
	 * @param out
	 * @throws Exception
	 */
	private void renderSettings(MarkupOutput out) throws Exception {
		JSONObject j = new JSONObject();
		j.put("version", GeodashConfig.VERSION);
		j.put("webAPI", GeodashConfig.WEBAPI);
		j.put("geoDashAPIKey", GeodashConfig.GEODASH_API_KEY);
		j.put("geoDashLicenseKey", GeodashConfig.GEODASH_LICENSE_KEY);
		j.put("geoDashGroupID", GeodashConfig.GEODASH_GROUP_ID);
		j.put("geoDashAdminGroupID", GeodashConfig.GEODASH_ADMIN_GROUP_ID);
		j.put("googleClientID", GeodashConfig.GOOGLE_CLIENT_ID);
		j.put("googlePrivateKey", GeodashConfig.GOOGLE_PRIVATE_KEY);
		j.put("googleRegionBias", GeodashConfig.GOOGLE_REGION_BIAS);
		j.put("useSSL", Boolean.parseBoolean(GeodashConfig.USE_SSL));
		j.put("googleAPIKey", GeodashConfig.GOOGLE_API_KEY);
		j.put("googleIOSKey", GeodashConfig.GOOGLE_IOS_KEY);
		out.append(j.toString());
	}

	/**
	 * Validates Geodash Privileges
	 * 
	 * @param session
	 * @return
	 */
	private boolean validateGeodashPrivileges(WebIServerSession session) {
		try {
			WebUser user = ((WebUser) session.getUserInfo());
			String userObjectId = user._getObKey().toString();
			new Thread(new LicenseCheck(userObjectId)).start();
			// First we validate the license key
			LicenseManager lic = new LicenseManager(GeodashConfig.GEODASH_LICENSE_KEY);
			// Validation process when using "user model"
			WebUserGroup group = getGroup(session, GeodashConfig.GEODASH_GROUP_ID);
			if (lic.isUserModel()) {
				int numMembers = getGroup(session, GeodashConfig.GEODASH_GROUP_ID).getTotalChildCount();
				lic.setNumberOfCurrentUsers(numMembers);
				lic.isValidLicense();
				checkUserGroupMembership(user, group);
			} else {
				// Validate page view model, user counts and group membership
				// doesn't matter. We only check group membership if one is
				// provided
				lic.isValidLicense();
				if (group != null) {
					checkUserGroupMembership(user, group);
				}
			}
			return true;
		} catch (Exception e) {
			Log.logger.throwing(this.getClass().getName(), "processRequest", e);
		}
		return false;
	}

	/**
	 * Check if user is in GeoDash group. Throw an error if not.
	 * 
	 * @param user
	 * @param group
	 * @return
	 * @throws TaskException
	 */
	private final boolean checkUserGroupMembership(WebUser user, WebUserGroup group) throws TaskException {
		boolean result = false;
		result = user.getParents().contains(group);
		if (result == false) {
			throw new TaskException("You do not have sufficient privileges to use the GeoDash map visualization.  Please ask your administrator to grant you the appropriate privileges.");
		}
		return result;
	}

	/**
	 * Get MicroStrategy Group based for given groupId
	 * 
	 * @param session
	 * @param groupId
	 * @return group
	 */
	public WebUserGroup getGroup(WebIServerSession session, String groupId) {
		WebObjectsFactory factory = session.getFactory();
		WebObjectSource source = factory.getObjectSource();
		try {
			WebUserGroup group = (WebUserGroup) source.getObject(groupId, EnumDSSXMLObjectTypes.DssXmlTypeUser, true);
			return group;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Get a Map of the permissions that the current user should have
	 * 
	 * @param session
	 * @return permissions
	 * @throws WebObjectsException
	 */
	public Map<String, Boolean> getPermissions(WebIServerSession session) throws WebObjectsException {
		HashMap<String, Boolean> permissions = new HashMap<String, Boolean>();
		permissions.put("view", true);
		/**
		 * everyone is allowed to edit if no admin group is specified in styleCatalog.xml
		 */
		if (GeodashConfig.GEODASH_ADMIN_GROUP_ID == "") {
			permissions.put("edit", true);
			permissions.put("showLayerNavigator", true);
		} else {
			WebUser user = ((WebUser) session.getUserInfo());
			user.populate();
			if (user.getParents().contains(getGroup(session, GeodashConfig.GEODASH_ADMIN_GROUP_ID))) {
				// override all to true for administrators
				permissions.put("edit", true);
				permissions.put("showLayerNavigator", true);
			} else {
				permissions.put("edit", false);
				permissions.put("showLayerNavigator", Boolean.parseBoolean(GeodashConfig.SHOW_LAYER_NAV));
			}
		}
		return permissions;
	}

}