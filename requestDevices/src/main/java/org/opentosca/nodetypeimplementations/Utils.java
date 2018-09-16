package org.opentosca.nodetypeimplementations;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * This class offers utility methods to handle the needed API logic to work with Rollout/HawkBit in the Context
 * of OpenTOSCA
 */
class Utils {
	//TODO: set to container
	private String host = "192.168.178.62";
	private final Logger LOG = LoggerFactory.getLogger(Utils.class);

	/**
	 * Creating the credentials for a HawkBit or Rollout instance
	 * @param tenant the tenant of the Rollout instance or empty is HawkBit instance
	 * @param user the user of the OTA-Manager
	 * @param password the password of the OTA-Manager
	 * @return an Base64 encoded String for Basic HTTP Authentication
	 */
	String createCredentials(String tenant, String user, String password){
		String authString;

		//check if it is a local hawkbit or a remote rollout service
		if(!tenant.isEmpty() ){
			authString = tenant + "\\" + user + ":" + password;
			LOG.debug("Creating authentication for Rollout");
		} else {
			// HawkBit does not have a tenant
			authString = user + ":" + password;
			LOG.debug("Creating authentication for HawkBit");
		}
		//encode for Basic HTTP Authentication
		byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
		return new String(authEncBytes);
	}

	/**
	 * Generates a URL for Javas HTTP API
	 * @param host the url, with http or without
	 * @return returns the url with leading http://
	 */
	String generateHost(String host){
		if (host.startsWith("http://")){
			return host;
		} else {
			return "http://" + host;
		}
	}

	/**
	 * Request all CSARs from your OpenTOSCA Container and checks if one is a BoschOTA Csar
	 * @return the CSAR of a BoschOTA if found, else null
	 */
	String getCSAR(){
		JSONArray csars = new JSONObject(httpRequests("http://" + host + ":1337/csars/", "", "GET", "application/json")).getJSONArray("csars");
		LOG.debug("Requesting CSARs from: " + "http://" + host + ":1337/csars/");
		 for (int i = 0; i < csars.length(); i++){
		 	String boschOTA = csars.getJSONObject(i).getString("id");
		 	LOG.debug("Got CSAR: " + boschOTA);
		 	if(boschOTA.contains("BoschOTA")){
		 		LOG.debug("Using OTA-CSAR: " + boschOTA);
		 		return boschOTA;
			}
		 }
		 LOG.debug("No CSAR implementing BoschOTA was found");
		return null;
	}

	/**
	 * Request all service templates from the given CSAR and check for a BoschOTA Service Template
	 * @param csar the csar to get the service templates from
	 * @return the proper BoschOTA ServiceTemplate if found, else null
	 */
	String getServiceTemplate(String csar){
		JSONObject requestResult = new JSONObject(httpRequests("http://" + host + ":1337/csars/" + csar + "/servicetemplates/", "", "GET", "application/json"));
		LOG.debug("Requesting ServiceTemplates from: " + "http://" + host + ":1337/csars/" + csar + "/servicetemplates/");
		JSONArray links = requestResult.getJSONArray("service_templates");

		for (int i = 0; i < links.length(); i++){
			String serviceTemplateURL = links.getJSONObject(i).getJSONObject("_links").getJSONObject("self").getString("href");
			LOG.debug("Got ServiceTemplate: " + serviceTemplateURL);
			if(serviceTemplateURL.contains("BoschOTA")) {
				String serviceTemplate[] =serviceTemplateURL.split("/");
				LOG.debug("Using ServiceTemplate: " + serviceTemplate[serviceTemplate.length -1]);
				return serviceTemplate[serviceTemplate.length -1];
			}
		}
		LOG.debug("No fitting OTA-ServiceTemplate from Type BoschOTA was found");
		return null;
	}

	/**
	 * Request all NodeType from a given csar, servicetemplate and node type
	 * @param csar the csar to search in
	 * @param servicetemplate the service template to search in
	 * @param nodeType the nodetype to find templates for
	 * @return the nodetemplate of the given nodetype if found, else null
	 */
	String getNodetemplates(String csar, String servicetemplate, String nodeType) {
		JSONObject requestResult = new JSONObject(httpRequests("http://" + host + ":1337/csars/" + csar + "/servicetemplates/" + servicetemplate + "/nodetemplates/"
				, "", "GET", "application/json"));
		LOG.debug("Requesting NodeTemplates from: " + "http://" + host + ":1337/csars/" + csar + "/servicetemplates/" + servicetemplate + "/nodetemplates/");
		JSONArray nodeTemplates = requestResult.getJSONArray("node_templates");

		for(int i = 0; i < nodeTemplates.length(); i++){
			LOG.debug("Check if: " + nodeTemplates.getJSONObject(i).getString("id") + " starts with: " + nodeType);
			if(nodeTemplates.getJSONObject(i).getString("id").startsWith(nodeType)){
				String[] nodeTemplate = nodeTemplates.getJSONObject(i).getJSONObject("_links").getJSONObject("self").getString("href").split("/");
				LOG.debug("Using NodeTemplate: " + nodeTemplate[nodeTemplate.length - 1]);
				return nodeTemplate[nodeTemplate.length - 1];
			}
		}
		LOG.debug("No NodeTemplate of Type: " + nodeType + " was found");
		return null;
	}

	/**
	 * Requesting the newest Instance ID of a given ServiceTemplate of a given CSAR
	 * @param csar the csar to get the service template from
	 * @param servicetemplate the servicetemplate to get the instances from
	 * @return the id of the newest service template if one found, else null
	 */
	String getInstanceID(String csar, String servicetemplate){
		JSONObject requestResult = new JSONObject(httpRequests("http://" + host + ":1337/csars/" + csar + "/servicetemplates/" + servicetemplate + "/instances/"
				, "", "GET", "application/json"));
		LOG.debug("Requesting ServiceTemplateInstances from " + "http://" + host + ":1337/csars/" + csar + "/servicetemplates/" + servicetemplate + "/instances/");
		JSONArray instances =  requestResult.getJSONArray("service_template_instances");
		Integer instanceID = null;
		long timestamp = 0L;

		//check for an instance which is in a CREATED state and is the newest created instance (there may be older versions laying around)
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
		String response = instanceID == null ? null : Integer.toString(instanceID);
		LOG.debug("Using ServceTemplateInstance with ID: " + response);
		return response;
	}

	/**
	 *
	 * Create an OpenTOSCA instance of a nodetemplate for the given parameters (we need this, as we can only return one value per return value to OpenTOSCA)
	 * @param csar the csar of the nodetemplate instance
	 * @param servicetemplate the servicetemplate of the nodetemplate instance
	 * @param nodetemplates the nodetemplate where we want to create an instance
	 * @param instanceID the ID of the new instance
	 * @param properties the property names of the new instance
	 * @param propertiesValue the values of the properties
	 */
	void createInstance(String csar, String servicetemplate, String nodetemplates, String instanceID, List<String> properties, List<String> propertiesValue){
		String baseHost = "http://" + host + ":1337/csars/" + csar + "/servicetemplates/" + servicetemplate
				+ "/nodetemplates/" + nodetemplates + "/instances/";
		LOG.debug("Using " + "http://" + host + ":1337/csars/" + csar + "/servicetemplates/" + servicetemplate
				+ "/nodetemplates/" + nodetemplates + "/instances/" + " as URL for creating Instances");

		//create Instance
		LOG.debug("Creating Instances");
		String newInstance = httpRequests(baseHost, instanceID, "POST", "text/plain");

		//set properties
		LOG.debug("Setting Properties");
		String propValues = "<Properties>";
		for(int i = 0; i < properties.size(); i++) {
			propValues += "<" + properties.get(i) + ">" + propertiesValue.get(i) + "</" + properties.get(i) + ">";
		}
		propValues += "</Properties>";
		httpRequests(newInstance + "/properties/", propValues, "PUT", "application/xml");

		//set state
		LOG.debug("Setting the state");
		httpRequests(newInstance + "/state/", "STARTED", "PUT", "text/plain");
	}

	/**
	 * Making a HTTP GET request, getting an JSON Object back
	 * @param requestURL the url to reques
	 * @param authStringEnc the credentials for the basic http auth
	 * @return a json object containing the result, or null
	 */
	JSONObject getHTTPRequestResponse(String requestURL, String authStringEnc) {
		JSONObject response = null;
		LOG.debug("Requesting URL: " + requestURL);
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

	/**
	 * Making a HTTP request for a given HTTP verb, getting an response message back.
	 * For not GET verbs we need to escape the message properly
	 * @param host the host for the request
	 * @param requestBody the body for the request
	 * @param httpMethod the http verb
	 * @param contenType the contentype of the request
	 * @return the respose of the http request as string (escaped if it is a not GET)
	 */
	private String httpRequests(final String host, final String requestBody, String httpMethod, String contenType) {
		LOG.debug("Requesting URL: " + host + " with HTTP Method: " + httpMethod + " with ContentType: " + contenType + " and Body:" + requestBody);
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
			getResponse = getResponse.replace("\"", "");
			LOG.debug("The Response of the Request is:" + getResponse);
			return getResponse;
		} else {
			LOG.debug("The Response of the Request is:" + getResponse);
			return getResponse;
		}
	}
}
