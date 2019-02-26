
import java.net.URL;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class XMLRPCClient {

	private static final int port = 8081;
	private static final String host = "localhost";
	private static Integer[] data = { 99, 20, 30, 40, 50, 60, 71, 80, 90, 91 };
	
	public static void main(String[] args) throws Exception {
		
		// Set Client Configuration
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(new URL("http://"+ host + ":" + port + "/xmlrpc") );
		XmlRpcClient client = new XmlRpcClient();
		client.setConfig(config);
		
		// Set Parameter
		Object[] params = new Object[]{ data[ 0 ], data[ 1 ] };
		
		// RPC Call
		Integer result = (Integer) client.execute("Calculator.add", params);
		
		// Display Result
		System.out.println( result );

	}

}
