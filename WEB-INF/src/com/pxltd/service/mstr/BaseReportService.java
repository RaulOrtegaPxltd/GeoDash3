package com.pxltd.service.mstr;

import com.microstrategy.utils.StringUtils;
import com.microstrategy.utils.log.Level;
import com.microstrategy.web.app.WebAppSessionManager;
import com.microstrategy.web.app.beans.AppContext;
import com.microstrategy.web.beans.ViewBean;
import com.microstrategy.web.objects.WebIServerSession;
import com.microstrategy.web.objects.WebObjectSource;
import com.microstrategy.web.objects.WebObjectsException;
import com.microstrategy.web.objects.WebObjectsFactory;
import com.microstrategy.web.objects.admin.users.WebUser;
import com.microstrategy.web.objects.admin.users.WebUserGroup;
import com.microstrategy.webapi.EnumDSSXMLObjectTypes;
import com.pxltd.geodash.GeodashConfig;
import com.pxltd.geodash.GeodashException;
import com.pxltd.geodash.LicenseManager;
import com.pxltd.geodash.ServiceException;

public class BaseReportService {
	protected ViewBean vb;
	protected String status = "ready";
	protected String[] errors = {};

	public BaseReportService(ViewBean vb) throws ServiceException {
		this.vb = vb;

		try {
			validateLicense();
		} catch (IllegalArgumentException e) {
			setError(Level.SEVERE, "Could not read geodash configuration from the styleCatalog.xml file.", e.getMessage());
		}

	}

	public void validateLicense() throws ServiceException {
		// first we validate the license key
		LicenseManager lic;
		lic = new LicenseManager(GeodashConfig.GEODASH_LICENSE_KEY);

		// validation process when using "user model"
		if (lic.isUserModel()) {
			int numMembers = getMapGroup().getTotalChildCount();
			lic.setNumberOfCurrentUsers(numMembers);
			lic.isValidLicense();
			checkUserGroupMembership();
		} else {
			// validate pageviewmodel, user counts and group membership doesnt matter
			// we only check group membership if one is provided
			lic.isValidLicense();
			if (StringUtils.isNotEmpty(GeodashConfig.GEODASH_GROUP_ID)) {
				checkUserGroupMembership();
			}
		}
	}

	private boolean checkUserGroupMembership() throws ServiceException {
		boolean result = false;
		try {
			if (this.vb.getBeanContext() instanceof AppContext) {
				// Retrieve current user from Session manager
				WebAppSessionManager sm = ((AppContext) this.vb.getBeanContext()).getAppSessionManager();
				WebIServerSession session = sm.getActiveSession();
				// Retrieve user information from session
				WebUser user = ((WebUser) session.getUserInfo());
				user.populate();

				// Does the user belong to the MapGroup group?
				result = user.getParents().contains(getMapGroup());
				if (result == false) {
					throw new ServiceException("You do not have sufficient privileges to use the GeoDash map visualization.  Please ask your administrator to grant you the appropriate privileges.");
				}
			}
		} catch (WebObjectsException ex) {
			setError(Level.SEVERE, "Unable to retrive UserInfo from the session.", ex.getMessage());
		}
		return result;
	}

	/**
	 * Utility method. This method searches for the "MapGroup" group so it can be compared with other user groups at a later time.
	 * 
	 * @return WebUserGroup
	 * @throws ServiceException
	 * @throws GeodashException
	 */
	private WebUserGroup getMapGroup() throws ServiceException {
		WebUserGroup mapGroup = null;
		try {
			if (vb.getBeanContext() instanceof AppContext) {

				WebAppSessionManager sm = ((AppContext) vb.getBeanContext()).getAppSessionManager();
				WebIServerSession session = sm.getActiveSession();

				WebObjectsFactory factory = session.getFactory();
				WebObjectSource source = factory.getObjectSource();

				mapGroup = (WebUserGroup) source.getObject(GeodashConfig.GEODASH_GROUP_ID, EnumDSSXMLObjectTypes.DssXmlTypeUser, true);
			}

		} catch (Exception ex) {
			setError(Level.SEVERE, "Could not find the Geodash user group.  Be sure to add the correct GUID of the GeoDash user group in the styleCatalog.xml file.", ex.getMessage());
		}
		return mapGroup;
	}

	protected void setError(Level l, String msg, String log) throws ServiceException {
		Log.logger.logp(l, this.getClass().getSimpleName(), msg, log);
		throw new ServiceException(msg);
	}
}
