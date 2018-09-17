import java.util.Arrays;
import java.util.List;

/**
 * Created by Marc Schmid on 17.09.18.
 */
public class LocalTests {
	private static final IALogger LOG = new IALogger(LocalTests.class);

	public static void main(String[] args){
		Utils utils = new Utils();

		String deviceName = "haveNoDS";
		String distributionSetName = "Still have no DS but shit works";

		//creating parameters for the instance creation in OpenTOSCA
		String csar = utils.getCSAR();
		LOG.debug("Using CSAR: " + csar);
		String servicetemplate = utils.getServiceTemplate(csar);
		LOG.debug("Usind ServiceTemplate: " + servicetemplate);
		String nodeTemplate = utils.getNodetemplates(csar, servicetemplate, "Device");
		LOG.debug("Using NodeTemplate: " + nodeTemplate);
		String baseHost = "http://" + utils.getHost() + ":1337/csars/" + csar + "/servicetemplates/" + servicetemplate
				+ "/nodetemplates/" + nodeTemplate + "/instances/";
		LOG.debug("Using " + "http://" + utils.getHost() + ":1337/csars/" + csar + "/servicetemplates/" + servicetemplate
				+ "/nodetemplates/" + nodeTemplate + "/instances/" + " as URL for updating Instance properties");
		String nodeTemplateID = utils.getInstanceIDbyPropery(baseHost, deviceName);
		LOG.debug("Using NodeTemplateInstanceID: " + nodeTemplateID);

		LOG.debug("Using NodeTemplateInstanceID: " + nodeTemplateID);

		List<String> properties = Arrays.asList("deviceID", "assignedDS");
		List<String> propertiesValue = Arrays.asList(deviceName, distributionSetName);

		LOG.debug("Setting Properties");
		StringBuilder propValues = new StringBuilder("<Properties>");
		for(int i = 0; i < properties.size(); i++) {
			propValues.append("<").append(properties.get(i)).append(">").append(propertiesValue.get(i)).append("</").append(properties.get(i)).append(">");
		}
		propValues.append("</Properties>");
		utils.httpRequests(baseHost + nodeTemplateID + "/properties/", propValues.toString(), "PUT", "application/xml");

		LOG.debug("Ended updateDevice");
	}
}
