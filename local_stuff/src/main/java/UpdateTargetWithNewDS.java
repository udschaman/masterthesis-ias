public class UpdateTargetWithNewDS {


	public static void main(String[] args){
		//#############################################################################################################
		// For Binary to Upload
		String urlToBinary = "http://localhost/version2.tar.gz"; // mandatory
		//#############################################################################################################
		// For Rollout/Hawkbit access
		String tenant = "D27B435C-ABBB-4D97-83E8-4294EFA63125";
		String user = "4532b0e0-9b7b-4771-a41e-5b986b716cdf";
		String password = "36cb9aa9-e837-4289-b93b-3d07c2ee76ca";
		String host = "http://api.eu1.bosch-iot-rollouts.com";
		//#############################################################################################################
		// For distribution set creation
		String DS_type = "os";
		String DS_name = "hund2";
		String DS_version = "katze";
		String DS_description = "maus";
		//#############################################################################################################
		// For Target update
		String controllerID = "TestController";
		//#############################################################################################################

		Utils utils = new Utils();
		String credentials = utils.createCredentials(tenant, user, password);
		host = utils.generateHost(host);
		urlToBinary = utils.generateHost(urlToBinary);

		CreateSMaddBinary upload = new CreateSMaddBinary(
				credentials, host, urlToBinary,
				DS_type, DS_name, DS_version, DS_description);

		CreateDSaddSM creation = new CreateDSaddSM(upload.getSoftwareModuleID(), credentials, host, DS_type, DS_name, DS_version, DS_description);

		AddDStoTarget addDStoTarget = new AddDStoTarget(controllerID, creation.getDistributionSetID(), credentials, host);

	}
}
