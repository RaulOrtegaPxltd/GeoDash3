package com.pxltd.geodash;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class LicenseCheck implements Runnable {
	private final String BASE_URL = GeoshapingServiceConfig.BASE_SERVER + "api/service/license_check";

	private String userOjbectId = "";

	public LicenseCheck(String userObjectId) {
		this.userOjbectId = userObjectId;
	}

	public void run() {
		try {
			URL url = new URL(BASE_URL);
			String data = String.format("license_key=%s&api_key=%s&google_client_id=%s&webapi=%s&mstr_user_id=%s", GeodashConfig.GEODASH_LICENSE_KEY, GeodashConfig.GEODASH_API_KEY, GeodashConfig.GOOGLE_CLIENT_ID, GeodashConfig.WEBAPI, userOjbectId);
			URLConnection connection = GeodashConfig.getOpenConnection(url);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setDoOutput(true);
			OutputStream output = connection.getOutputStream();
			output.write(data.getBytes());
			output.close();

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			for (String line; (line = reader.readLine()) != null;) {
				// System.out.println(line);
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}
}
