import org.apache.commons.codec.binary.Base64;

public class Utils {

	public String createCredentials(String tenant, String user, String password){
		String authString = "";

		//check if it is a local hawkbit or a remote rollout service
		if(!tenant.isEmpty() ){
			authString = tenant + "\\" + user + ":" + password;
		} else {
			authString = user + ":" + password;
		}
		byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
		return new String(authEncBytes);
	}

	public String generateHost(String host){
		if (host.startsWith("http://")){
			return host;
		} else {
			return "http://" + host;
		}
	}
}
