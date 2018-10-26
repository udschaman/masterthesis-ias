package org.opentosca.nodetypeimplementations;

import java.util.HashMap;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebService
public class org_opentosca_nodetypes_BoschOTAManager__groupmanagement_creategroup extends AbstractIAService {

    private static final Logger logger = LoggerFactory.getLogger(
            org_opentosca_nodetypes_BoschOTAManager__groupmanagement_creategroup.class
    );

	@WebMethod
	@SOAPBinding
	@Oneway
	public void createGroup(
		@WebParam(name="nameOfGroup", targetNamespace="http://nodetypeimplementations.opentosca.org/") String nameOfGroup,
		@WebParam(name="deviceName", targetNamespace="http://nodetypeimplementations.opentosca.org/") String deviceName
	) {
		// This HashMap holds the return parameters of this operation.
		final HashMap<String,String> returnParameters = new HashMap<String, String>();
		logger.debug("Start creating group with name " + nameOfGroup + " and devices " + deviceName);

		String[] devices = deviceName.split(" ");
		StringBuilder deviceList = new StringBuilder();

		for (String device : devices) {
			deviceList.append(device);
			deviceList.append(",");
		}

		deviceList.deleteCharAt(deviceList.length()-1);

		if(nameOfGroup.isEmpty()){
			nameOfGroup = "default";
		}
		if(deviceList.toString().isEmpty()){
			deviceList.append("noMembers");
		}

		returnParameters.put("groupName", nameOfGroup);
		returnParameters.put("deviceList", deviceList.toString());
		sendResponse(returnParameters);
		logger.debug("Finished creating group");
	}
}
