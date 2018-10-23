package org.opentosca.nodetypeimplementations;

import java.util.HashMap;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlElement;

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

		// TODO: Implement your operation here.


		// Output Parameter 'groupName' (optional)
		// TODO: Set groupName parameter here.
		// Do NOT delete the next line of code. Set "" as value if you want to return nothing or an empty result!
		returnParameters.put("groupName", "TODO");

		// Output Parameter 'deviceList' (optional)
		// TODO: Set deviceList parameter here.
		// Do NOT delete the next line of code. Set "" as value if you want to return nothing or an empty result!
		returnParameters.put("deviceList", "TODO");

		sendResponse(returnParameters);
	}



}
