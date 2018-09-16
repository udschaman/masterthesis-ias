package org.opentosca.nodetypeimplementations;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;


class CreateSMaddBinary {

	private String softwareModuleID = null;

	CreateSMaddBinary(String credentials, String host, String urlToBinary, String name){

		System.out.println("Create Software Module");
		softwareModuleID = createSoftwareModule(name, credentials, host);
		System.out.println("Created Software Module with id: " + softwareModuleID);
		if(softwareModuleID != null) {
			System.out.println("Downloading binary");
			File newVersion = downloadBinary(urlToBinary);
			System.out.println("Downloaded binary: " + newVersion.getName());
			if(newVersion.exists()){
				System.out.println("Uploading binary");
				int status = uploadBinary(newVersion, host, credentials);
				System.out.println("Upload ended with: " + status);
				newVersion.delete();
			}
		}
	}

	private String createSoftwareModule(String name, String credentials, String host){
		String id = null;
		JSONObject inputValues = new JSONObject();
		inputValues.put("type", "os");
		inputValues.put("name", name);
		inputValues.put("version", "created by OpenTOSCA");
		inputValues.put("vendor", "created by OpenTOSCA");
		inputValues.put("description", "created by OpenTOSCA");

		JSONArray input = new JSONArray();
		input.put(inputValues);
		String message = input.toString();

		try {
			URL url = new URL(host + "/rest/v1/softwaremodules");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Authorization", "Basic " + credentials);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/hal+json;charset=UTF-8");
			connection.setRequestMethod("POST");

			OutputStream os = connection.getOutputStream();
			os.write(message.getBytes("UTF-8"));
			os.close();

			System.out.println("Software Module Creation ended with :" + connection.getResponseCode());
			if (connection.getResponseCode() == 201){
				String getResponse = "";

				InputStream content = connection.getInputStream();
				BufferedReader in =	new BufferedReader (new InputStreamReader(content));
				String line;
				while ((line = in.readLine()) != null) {
					getResponse += line;
				}
				in.close();
				JSONArray response = new JSONArray(getResponse);

				id = Integer.toString(response.getJSONObject(0).getInt("id"));
			}
			connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return id;
	}

	private File downloadBinary(String urlToBinary){
		File downloadedFile = new File(urlToBinary.split("/")[urlToBinary.split("/").length - 1]);
		System.out.println("URL to request is: " + urlToBinary);
		try {
			URL website = new URL(urlToBinary);
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			FileOutputStream fos = new FileOutputStream(downloadedFile);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return downloadedFile;
	}

	private int uploadBinary(File newVersion, String host, String credentials){
		int result = 0;
		try {
			URL url = new URL(host + "/rest/v1/softwaremodules/" + softwareModuleID +"/artifacts");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Authorization", "Basic " + credentials);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", " multipart/form-data; boundary=" + "----SomeRandomText");
			connection.setRequestMethod("POST");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "----SomeRandomText", Charset.defaultCharset());
			ContentBody contentPart = new FileBody(newVersion);

			reqEntity.addPart("file", contentPart);
			reqEntity.writeTo(connection.getOutputStream());
			connection.disconnect();

			result = connection.getResponseCode();
			System.out.println("Upload Binary ended with: " + result);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	String getSoftwareModuleID() {
		return softwareModuleID;
	}
}
