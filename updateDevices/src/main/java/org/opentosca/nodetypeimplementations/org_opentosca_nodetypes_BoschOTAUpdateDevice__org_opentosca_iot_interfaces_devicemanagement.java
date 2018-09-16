package org.opentosca.nodetypeimplementations;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.util.Collections;


/**
 * Implementing the devicemanagement operations for Rollout/HawkBit in OpenTOSCA
 * Offers methods upload new update binaries and add them to existing devices
 */
@WebService
public class org_opentosca_nodetypes_BoschOTAUpdateDevice__org_opentosca_iot_interfaces_devicemanagement extends AbstractIAService {

	//TODO: Update Device-assignedDS in TOSCA
	private final IALogger LOG = new IALogger(org_opentosca_nodetypes_BoschOTAUpdateDevice__org_opentosca_iot_interfaces_devicemanagement.class);

	/**
	 * Method to create a new software module, add a binary to it and add the software module to a new distribution set
	 * @param tenant the tenant of Rollout/HawkBit
	 * @param user the user of Rollout/HawkBit
	 * @param password the password of Rollout/HawkBit
	 * @param host the URL of Rollout/HawkBit
	 * @param distributionSetName the name of the new SoftwareModule and Distribution Set
	 * @param urlToBinary the URL to the File containing the Update we want to upload in Rollout/Hawkbit
	 */
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
		LOG.debug("Starting uploadBinary");
		Utils utils = new Utils();
		//http basic auth
		String credentials = utils.createCredentials(tenant,user, password);
		host = utils.generateHost(host);
		LOG.debug("Using Host: " + host);
		urlToBinary = utils.generateHost(urlToBinary);
		LOG.debug("Using FileServer: " + urlToBinary);

		CreateSMaddBinary upload = new CreateSMaddBinary(credentials, host, urlToBinary, distributionSetName);

		if(upload.getSoftwareModuleID() != null) {
			CreateDSaddSM creation = new CreateDSaddSM(upload.getSoftwareModuleID(), credentials, host, distributionSetName);
			//creating parameters for the instance creation in OpenTOSCA
			String csar = utils.getCSAR();
			LOG.debug("Using CSAR: " + csar);
			String servicetemplate = utils.getServiceTemplate(csar);
			LOG.debug("Usind ServiceTemplate: " + servicetemplate);
			String instanceID = utils.getInstanceID(csar, servicetemplate);
			LOG.debug("Usind InstanceID: " + instanceID);

			String nodetemplates = utils.getNodetemplates(csar, servicetemplate, "Distribution");
			utils.createInstance(csar, servicetemplate, nodetemplates, instanceID,
					Collections.singletonList("distributionSet"), Collections.singletonList(Integer.toString(creation.getDistributionSetID())));
		}
		LOG.debug("Ended uploadBinary");
	}

	/**
	 * Method to update a given device with a given distribution set
	 * @param tenant the tenant of Rollout/HawkBit
	 * @param user the user of Rollout/HawkBit
	 * @param password the password of Rollout/HawkBit
	 * @param host the URL of Rollout/HawkBit
	 * @param deviceName the Name of the device to update
	 * @param distributionSetName the name of the new SoftwareModule and Distribution Set with the update
	 */
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
		LOG.debug("Starting updateDevice");
		Utils utils = new Utils();
		String credentials = utils.createCredentials(tenant,user, password);
		host = utils.generateHost(host);

		AddDStoTarget addDStoTarget = new AddDStoTarget(deviceName, distributionSetName, credentials, host);
		LOG.debug("Ended updateDevice");
	}
}
