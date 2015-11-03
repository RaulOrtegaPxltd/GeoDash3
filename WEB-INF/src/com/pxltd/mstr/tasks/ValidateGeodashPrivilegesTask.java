package com.pxltd.mstr.tasks;

import java.util.HashMap;
import java.util.Map;

import com.bdl.service.ServiceException;
import com.microstrategy.web.app.tasks.AbstractAppTask;
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

public class ValidateGeodashPrivilegesTask extends AbstractAppTask {

	public ValidateGeodashPrivilegesTask() {
		super("This task takes care of validating user privilege");
		addSessionStateParam(false, null);
	}

	@Override
	public void processRequest(TaskRequestContext context, MarkupOutput out) throws TaskException {

		WebIServerSession session = context.getWebIServerSession("SessionState", null);
		try {
			WebUser user = ((WebUser) session.getUserInfo());
			String userObjectId = user._getObKey().toString();
			new Thread(new LicenseCheck(userObjectId)).start();

			// first we validate the license key
			LicenseManager lic = new LicenseManager(GeodashConfig.GEODASH_LICENSE_KEY);
			// validation process when using "user model"
			WebUserGroup group = getGroup(session, GeodashConfig.GEODASH_GROUP_ID);
			if (lic.isUserModel()) {
				int numMembers = getGroup(session, GeodashConfig.GEODASH_GROUP_ID).getTotalChildCount();
				lic.setNumberOfCurrentUsers(numMembers);
				lic.isValidLicense();
				checkUserGroupMembership(user, group);
			} else {
				// validate pageviewmodel, user counts and group membership doesnt matter
				// we only check group membership if one is provided
				lic.isValidLicense();
				if (group != null) {
					checkUserGroupMembership(user, group);
				}
			}

		} catch (WebObjectsException e) {
			throw new TaskException("Unable to retrive user from the session.");
		} catch (ServiceException e) {
			throw new TaskException(e.getMessage());
		}

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
