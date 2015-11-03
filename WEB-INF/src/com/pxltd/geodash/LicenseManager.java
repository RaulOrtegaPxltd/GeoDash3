package com.pxltd.geodash;

import java.security.Key;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.bdl.service.ServiceException;
import com.bdl.service.mstr.MstrLog;
import com.microstrategy.utils.log.Level;

import sun.misc.BASE64Decoder;

public final class LicenseManager {

	private final Cipher dcipher;
	private String licKey;
	private String[] keys;
	private int currentUserCount;

	public LicenseManager(String bdlLicenseKey) throws ServiceException {
		this.licKey = bdlLicenseKey;
		SecretKey blowfishKey;
		try {
			blowfishKey = KeyGenerator.getInstance("Blowfish").generateKey();
			dcipher = Cipher.getInstance(blowfishKey.getAlgorithm());
			dcipher.init(Cipher.DECRYPT_MODE, blowfishKey);
		} catch (Exception e) {
			MstrLog.logger.logp(Level.SEVERE, "LicenseManager", "Constructor", MstrLog.EXCEPTION, e);
			throw new ServiceException("There was a general error while reading the license keys.  Please make sure you are using a valid key.");
		}
		validateLicenseKey();
	}

	public final void setNumberOfCurrentUsers(int users) {
		this.currentUserCount = users;
	}

	private final String[] validateLicenseKey() throws ServiceException {
		try {
			String alg = "AES";
			byte[] keyValue = new byte[] { 'B', '!', 'G', 'D', 'a', 'T', 'A', '#', 'a', 'b', 's', 'O', 'k', 'K', 'e', '5' };
			Key key = new SecretKeySpec(keyValue, alg);
			Cipher c = Cipher.getInstance(alg);
			c.init(Cipher.DECRYPT_MODE, key);
			byte[] decordedValue = new BASE64Decoder().decodeBuffer(licKey);
			byte[] decValue = c.doFinal(decordedValue);
			String decryptedValue = new String(decValue);
			return keys = decryptedValue.split("!");
		} catch (Exception e) {
			throw new ServiceException("Invalid license key.  Please enter a valid GeoDash license key.  If you do not have one send an email to support@pxlabs.ca.");
		}
	}

	private final void validateExpiration() throws ServiceException {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		try {
			Date expDate = df.parse(keys[1]);
			Date inifinty = df.parse("1269-01-01");
			// if the expiration date is not infinity and expiration date has passed throw exception
			if (!expDate.equals(inifinty)) {
				if (expDate.before(calendar.getTime())) {
					throw new ServiceException("Your license key has expired.  Please contact your Account Rep or send an email to support@pxlabs.ca to get a new license key.");
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private final void validateLicensedUserCount() throws ServiceException {
		int licensedNumberOfUsers = Integer.parseInt(keys[0]);
		if (currentUserCount > licensedNumberOfUsers) {
			throw new ServiceException("There are currently more users allocated to use GeoDash than licensed for.  Current user count:  " + currentUserCount + " Licensed number of users:  " + licensedNumberOfUsers);
		}
	}

	public final boolean isValidLicense() throws ServiceException {
		validateExpiration();
		if (keys[3].equalsIgnoreCase("3")) {
			validateLicensedUserCount();
		}
		return true;
	}

	public final boolean isUserModel() {
		if (keys[3].equalsIgnoreCase("3")) {
			return true;
		}
		return false;
	}
}
