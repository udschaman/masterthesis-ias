package org.opentosca.nodetypeimplementations;

import com.sun.org.apache.xpath.internal.SourceTree;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
public class org_opentosca_nodetypes_BoschOTARequestDevice_w1_wip1__org_opentosca_interfaces_lifecycle extends AbstractIAService {

	//TODO show for each device its assigned DS

	@WebMethod
	@SOAPBinding
	@Oneway
	public void install(
		@WebParam(name="tenant", targetNamespace="http://nodetypeimplementations.opentosca.org/") String tenant,
		@WebParam(name="user", targetNamespace="http://nodetypeimplementations.opentosca.org/") String user,
		@WebParam(name="password", targetNamespace="http://nodetypeimplementations.opentosca.org/") String password,
		@WebParam(name="host", targetNamespace="http://nodetypeimplementations.opentosca.org/") String host
	) {
		// This HashMap holds the return parameters of this operation.
		final HashMap<String, String> returnParameters = new HashMap<String, String>();

		Utils utils = new Utils();
		//http basic auth
		String credentials = utils.createCredentials(tenant, user, password);

		String csar = "";//utils.getCSAR();
		String servicetemplate = "";//utils.getServiceTemplate(csar);
		String instanceID = "";//utils.getInstanceID(csar, servicetemplate);

		host = utils.generateHost(host);
		getDevices(host, credentials, utils, csar, servicetemplate, instanceID).forEach(returnParameters::putIfAbsent);


		System.exit(1);



		getDistributionSets(host, credentials, utils, csar, servicetemplate, instanceID).forEach(returnParameters::putIfAbsent);
		sendResponse(returnParameters);
	}

	private HashMap<String, String> getDistributionSets(String host, String authStringEnc, Utils utils, String csar, String servicetemplate, String instanceID) {
		final HashMap<String, String> returnParameters = new HashMap<>();
		//request devices
		String requestURL = host + "/rest/v1/distributionsets";
		JSONObject response = utils.getHTTPRequestResponse(requestURL, authStringEnc);
		//parse json response
		if (response != null) {
			int size = response.getInt("size");
			JSONArray devices = response.getJSONArray("content");
			for (int i = 0; i < size; i++) {
				JSONObject element = devices.getJSONObject(i);
				String resultID = Integer.toString(element.getInt("id"));
				if(i == 0){
					returnParameters.put("distributionSet", resultID);
				} else {
					String nodetemplates = utils.getNodetemplates(csar, servicetemplate, "Distribution");
					utils.createInstance(csar, servicetemplate, nodetemplates, utils, instanceID,
							Arrays.asList("distributionSet"), Arrays.asList(resultID));
				}
			}
		} else {
			returnParameters.put("distributionSet", "error in DistributionSet");
		}
		return returnParameters;
	}

	private HashMap<String, String> getDevices(String host, String authStringEnc, Utils utils, String csar, String servicetemplate, String instanceID) {
		final HashMap<String, String> returnParameters = new HashMap<>();
		//request devices
		String requestURL = host + "/rest/v1/targets";
		JSONObject response = utils.getHTTPRequestResponse(requestURL, authStringEnc);
		//parse json response
		if (response != null) {
			int size = response.getInt("size");
			JSONArray devices = response.getJSONArray("content");
			for (int i = 0; i < size; i++) {
				JSONObject element = devices.getJSONObject(i);
				String resultID = element.getString("controllerId");

				JSONObject assignedDS = utils.getHTTPRequestResponse(requestURL + "/" + resultID + "/assignedDS", authStringEnc);
				String deviceDS = "";
				if(assignedDS != null) {
					deviceDS = Integer.toString(assignedDS.getInt("id"));
				}
				if(i == 0){
					returnParameters.put("deviceID", resultID);
					returnParameters.put("assignedDS", deviceDS);
				} else {
					String nodetemplates = utils.getNodetemplates(csar, servicetemplate, "Device");
					utils.createInstance(csar, servicetemplate, nodetemplates, utils, instanceID,
							Arrays.asList("deviceID", "assignedDS"), Arrays.asList(resultID, deviceDS));
				}
			}
		} else {
			returnParameters.put("deviceID", "error in device");
			returnParameters.put("assignedDS", "error in device");
		}
		return returnParameters;
	}
}
