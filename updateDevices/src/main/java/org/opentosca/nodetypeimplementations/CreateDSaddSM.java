package org.opentosca.nodetypeimplementations;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

class CreateDSaddSM {

	private int distributionSetID;

	CreateDSaddSM(String softwareModuleID, String credentials, String host, String DS_name) {

		System.out.println("Creating distribution set");
		createDSandAddSM(softwareModuleID, host, credentials, DS_name);
		System.out.println("Created distribution set: " + distributionSetID);

	}

	private void createDSandAddSM(String softwareModuleID, String host, String credentials, String DS_name) {

		JSONObject inputValues = new JSONObject();
		inputValues.put("type", "os");
		inputValues.put("name", DS_name);
		inputValues.put("version", "created by OpenTOSCA");
		inputValues.put("requiredMigrationStep", false);
		inputValues.put("description", "created by OpenTOSCA");
		JSONObject modules_id = new JSONObject();
		modules_id.put("id", softwareModuleID);
		JSONArray modules = new JSONArray();
		modules.put(modules_id);
		inputValues.put("modules", modules);

		JSONArray input = new JSONArray();
		input.put(inputValues);
		String message = input.toString();

		try {
			URL url = new URL(host + "/rest/v1/distributionsets");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Authorization", "Basic " + credentials);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/hal+json;charset=UTF-8");
			connection.setRequestMethod("POST");

			OutputStream os = connection.getOutputStream();
			os.write(message.getBytes("UTF-8"));
			os.close();

			if (connection.getResponseCode() == 201) {
				String getResponse = "";

				InputStream content = connection.getInputStream();
				BufferedReader in = new BufferedReader(new InputStreamReader(content));
				String line;
				while ((line = in.readLine()) != null) {
					getResponse += line;
				}
				in.close();
				JSONArray response = new JSONArray(getResponse);

				distributionSetID = response.getJSONObject(0).getInt("id");
			}
			connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getDistributionSetID() {
		return distributionSetID;
	}
}
