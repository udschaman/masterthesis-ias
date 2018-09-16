import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Marc Schmid on 06.09.18.
 */
public class CreateInstances {


	public static void main(String[] args) {

		String csar = "BoschOTARequestDevices_w1-wip1.csar";
		String servicetemplate = "%257Bhttp:%252F%252Fopentosca.org%252Fservicetemplates%257DBoschOTARequestDevices_w1-wip1";
		String nodetemplates = "Device_w1-wip1";
		final String instanceID = "9";



		final String baseHost = "http://localhost:1337/csars/" + csar + "/servicetemplates/" + servicetemplate
				+ "/nodetemplates/" + nodetemplates + "/instances/";

		//create Instance
		String newInstance = htmRequests(baseHost, instanceID, "POST", "text/plain");

		//set properties
		String properites = "<Properties><deviceID>soaptest;TestController;</deviceID></Properties>";
		String prortiesAreSet = htmRequests(newInstance + "/properties/", properites, "PUT", "application/xml");
		System.out.println(prortiesAreSet);

		//set state
		String statesAreSet = htmRequests(newInstance + "/state/", "STARTED", "PUT", "text/plain");

	}

	private static String htmRequests(final String host, final String instanceID, String httpMethod, String contenType) {
		String getResponse = "";
		try {
			final URL url = new URL(host);
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", contenType + ";charset=UTF-8");
			connection.setRequestMethod(httpMethod);

			final OutputStream os = connection.getOutputStream();
			os.write(instanceID.getBytes("UTF-8"));
			os.close();

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
		return getResponse.replace("\"","");
	}
}
