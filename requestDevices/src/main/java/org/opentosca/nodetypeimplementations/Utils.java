package org.opentosca.nodetypeimplementations;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

class Utils {


	String host = "192.168.178.62";

	String createCredentials(String tenant, String user, String password){
		String authString = "";

		//check if it is a local hawkbit or a remote rollout service
		if(!tenant.isEmpty() ){
			authString = tenant + "\\" + user + ":" + password;
		} else {
			authString = user + ":" + password;
		}
		byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
		return new String(authEncBytes);
	}

	String generateHost(String host){
		if (host.startsWith("http://")){
			return host;
		} else {
			return "http://" + host;
		}
	}

	String getCSAR(){
		JSONArray csars = new JSONObject(httpRequests("http://" + host + ":1337/csars/", "", "GET", "application/json")).getJSONArray("csars");
		 for (int i = 0; i < csars.length(); i++){
		 	String boschOTA = csars.getJSONObject(i).getString("id");
		 	if(boschOTA.contains("BoschOTA")){
		 		return boschOTA;
			}
		 }
		return null;
	}

	String getServiceTemplate(String csar){
		JSONObject requestResult = new JSONObject(httpRequests("http://" + host + ":1337/csars/" + csar + "/servicetemplates/", "", "GET", "application/json"));
		JSONArray links = requestResult.getJSONArray("service_templates");

		for (int i = 0; i < links.length(); i++){
			String serviceTemplateURL = links.getJSONObject(i).getJSONObject("_links").getJSONObject("self").getString("href");
			if(serviceTemplateURL.contains("BoschOTA")) {
				String serviceTemplate[] =serviceTemplateURL.split("/");
				return serviceTemplate[serviceTemplate.length -1];
			}
		}
		return null;
	}

	String getNodetemplates(String csar, String servicetemplate, String nodeType) {
		JSONObject requestResult = new JSONObject(httpRequests("http://" + host + ":1337/csars/" + csar + "/servicetemplates/" + servicetemplate + "/nodetemplates/"
				, "", "GET", "application/json"));
		JSONArray nodeTemplates = requestResult.getJSONArray("node_templates");

		for(int i = 0; i < nodeTemplates.length(); i++){
			if(nodeTemplates.getJSONObject(i).getString("id").startsWith(nodeType)){
				String[] nodeTemplate = nodeTemplates.getJSONObject(i).getJSONObject("_links").getJSONObject("self").getString("href").split("/");
				return nodeTemplate[nodeTemplate.length - 1];
			}
		}
		return null;
	}

	String getInstanceID(String csar, String servicetemplate){
		JSONObject requestResult = new JSONObject(httpRequests("http://" + host + ":1337/csars/" + csar + "/servicetemplates/" + servicetemplate + "/instances/"
				, "", "GET", "application/json"));
		JSONArray instances =  requestResult.getJSONArray("service_template_instances");
		Integer instanceID = null;
		long timestamp = 0L;
		for (int i = 0; i < instances.length(); i++){
			JSONObject instance = instances.getJSONObject(i);
			long instanceTimestamp = instance.getLong("created_at");
			String instanceState = instance.getString("state");
			int tempInstanceID = instance.getInt("id");
			if(instanceState.equals("CREATING") && instanceTimestamp > timestamp){
				timestamp = instanceTimestamp;
				instanceID = tempInstanceID;
			}
		}
		return instanceID == null ? null : Integer.toString(instanceID);
	}

	void createInstance(String csar, String servicetemplate, String nodetemplates, Utils utils, String instanceID, List<String> properties, List<String> propertiesValue){
		String baseHost = "http://" + host + ":1337/csars/" + csar + "/servicetemplates/" + servicetemplate
				+ "/nodetemplates/" + nodetemplates + "/instances/";

		//create Instance
		String newInstance = utils.httpRequests(baseHost, instanceID, "POST", "text/plain");
		//set properties
		String propValues = "<Properties>";
		for(int i = 0; i < properties.size(); i++) {
			propValues += "<" + properties.get(i) + ">" + propertiesValue.get(i) + "</" + properties.get(i) + ">";
		}

		propValues += "</Properties>";
		utils.httpRequests(newInstance + "/properties/", propValues, "PUT", "application/xml");
		//set state
		utils.httpRequests(newInstance + "/state/", "STARTED", "PUT", "text/plain");
	}

	JSONObject getHTTPRequestResponse(String requestURL, String authStringEnc) {
		JSONObject response = null;
		try {
			URL url = new URL(requestURL);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Authorization", "Basic " + authStringEnc);
			connection.setRequestMethod("GET");

			InputStream content = connection.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(content));
			String getResponse = "";
			String line;
			while ((line = in.readLine()) != null) {
				getResponse += line;
			}
			if(getResponse.startsWith("{")){
				response = new JSONObject(getResponse);
			}
			connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			return response;
		}
		return response;
	}

	private String httpRequests(final String host, final String requestBody, String httpMethod, String contenType) {
		String getResponse = "";
		boolean escape = false;
		try {
			final URL url = new URL(host);
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", contenType + ";charset=UTF-8");
			connection.setRequestMethod(httpMethod);

			if(!httpMethod.equals("GET")) {
				final OutputStream os = connection.getOutputStream();
				os.write(requestBody.getBytes("UTF-8"));
				os.close();
				escape = true;
			}

			final InputStream content = connection.getInputStream();
			final BufferedReader in = new BufferedReader(new InputStreamReader(content));
			String line;
			while ((line = in.readLine()) != null) {
				getResponse += line;
			}
			in.close();
			connection.disconnect();
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
		if(escape) {
			return getResponse.replace("\"", "");
		} else {
			return getResponse;
		}
	}
}
