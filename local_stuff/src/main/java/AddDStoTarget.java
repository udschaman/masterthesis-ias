import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class AddDStoTarget {

	int result;

	AddDStoTarget(String controllerID, int distributionSetID, String credentials, String host){

		System.out.println("Adding DS: " + distributionSetID + " for device: " + controllerID);
		addDStoTarget(controllerID, distributionSetID, credentials, host);
		System.out.println("Added DS: " + distributionSetID + " for device: " + controllerID + " with response: " + getResult());

	}

	private void addDStoTarget(String controllerID, int distributionSetID, String credentials, String host){
		try {
			URL url = new URL(host + "/rest/v1/targets/" + controllerID + "/assignedDS");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Authorization", "Basic " + credentials);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
			connection.setRequestMethod("POST");

			JSONObject inputValues = new JSONObject();
			inputValues.put("id", distributionSetID);

			String message = inputValues.toString();

			OutputStream os = connection.getOutputStream();
			os.write(message.getBytes("UTF-8"));
			os.close();

			result = connection.getResponseCode();
			connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public int getResult() {
		return result;
	}
}
