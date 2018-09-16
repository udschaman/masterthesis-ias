package org.opentosca.nodetypeimplementations;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
public class org_opentosca_nodetypes_BoschOTAUpdateDevice__org_opentosca_iot_interfaces_devicemanagement extends AbstractIAService {

	//TODO: Update Device-assignedDS in TOSCA

	@WebMethod
	@SOAPBinding
	@Oneway
	public void uploadBinary(
		@WebParam(name="tenant", targetNamespace="http://nodetypeimplementations.opentosca.org/") String tenant,
		@WebParam(name="user", targetNamespace="http://nodetypeimplementations.opentosca.org/") String user,
		@WebParam(name="password", targetNamespace="http://nodetypeimplementations.opentosca.org/") String password,
		@WebParam(name="host", targetNamespace="http://nodetypeimplementations.opentosca.org/") String host,
		@WebParam(name="distributionSetName", targetNamespace="http://nodetypeimplementations.opentosca.org/") String distributionSetName,
		@WebParam(name="urlToBinary", targetNamespace="http://nodetypeimplementations.opentosca.org/") String urlToBinary
	) {
		Utils utils = new Utils();
		String credentials = utils.createCredentials(tenant,user, password);
		host = utils.generateHost(host);
		urlToBinary = utils.generateHost(urlToBinary);

		CreateSMaddBinary upload = new CreateSMaddBinary(credentials, host, urlToBinary, distributionSetName);

		if(upload.getSoftwareModuleID() != null) {
			CreateDSaddSM creation = new CreateDSaddSM(upload.getSoftwareModuleID(), credentials, host, distributionSetName);

			String csar = utils.getCSAR();
			String servicetemplate = utils.getServiceTemplate(csar);
			String instanceID = utils.getInstanceID(csar, servicetemplate);

			String nodetemplates = utils.getNodetemplates(csar, servicetemplate, "Distribution");
			utils.createInstance(csar, servicetemplate, nodetemplates, utils, instanceID, "distributionSet", Integer.toString(creation.getDistributionSetID()));
		}
	}

	@WebMethod
	@SOAPBinding
	@Oneway
	public void updateDevice(
		@WebParam(name="tenant", targetNamespace="http://nodetypeimplementations.opentosca.org/") String tenant,
		@WebParam(name="user", targetNamespace="http://nodetypeimplementations.opentosca.org/") String user,
		@WebParam(name="password", targetNamespace="http://nodetypeimplementations.opentosca.org/") String password,
		@WebParam(name="host", targetNamespace="http://nodetypeimplementations.opentosca.org/") String host,
		@WebParam(name="deviceName", targetNamespace="http://nodetypeimplementations.opentosca.org/") String deviceName,
		@WebParam(name="distributionSetName", targetNamespace="http://nodetypeimplementations.opentosca.org/") String distributionSetName
	) {
		Utils utils = new Utils();
		String credentials = utils.createCredentials(tenant,user, password);
		host = utils.generateHost(host);

		AddDStoTarget addDStoTarget = new AddDStoTarget(deviceName, distributionSetName, credentials, host);
	}
}
