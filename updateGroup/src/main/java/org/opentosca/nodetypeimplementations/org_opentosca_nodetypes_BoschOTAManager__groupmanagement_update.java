package org.opentosca.nodetypeimplementations;

import java.util.HashMap;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;


@WebService
public class org_opentosca_nodetypes_BoschOTAManager__groupmanagement_update extends AbstractIAService {

	private final IALogger LOG = new IALogger(org_opentosca_nodetypes_BoschOTAManager__groupmanagement_update.class);


	@WebMethod
	@SOAPBinding
	@Oneway
	public void updateGroup(
		@WebParam(name="distributionSetName", targetNamespace="http://nodetypeimplementations.opentosca.org/") String distributionSetName,
		@WebParam(name="nameOfGroup", targetNamespace="http://nodetypeimplementations.opentosca.org/") String nameOfGroup,
		@WebParam(name="tenant", targetNamespace="http://nodetypeimplementations.opentosca.org/") String tenant,
		@WebParam(name="user", targetNamespace="http://nodetypeimplementations.opentosca.org/") String user,
		@WebParam(name="password", targetNamespace="http://nodetypeimplementations.opentosca.org/") String password,
		@WebParam(name="host", targetNamespace="http://nodetypeimplementations.opentosca.org/") String host
	) {
		// This HashMap holds the return parameters of this operation.
		final HashMap<String,String> returnParameters = new HashMap<String, String>();

		// TODO: Implement your operation here.


		returnParameters.put("success", "success");
		sendResponse(returnParameters);
	}
}
