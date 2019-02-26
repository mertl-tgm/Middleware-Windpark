
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.webserver.WebServer;

public class XMLRPCServer {
	  
private static final int port = 8081;

public static void main(String[] args) throws Exception {
    	  
	// Initialize Web- and RPCServer
  WebServer webserver = new WebServer(port);
  XmlRpcServer xmlRpcServer = webserver.getXmlRpcServer();
  
  // Set Handler for Calculator Instance
  PropertyHandlerMapping phm = new PropertyHandlerMapping();
  phm.addHandler("Calculator", Calculator.class );
  xmlRpcServer.setHandlerMapping(phm);
  
  // Start Webserver
  webserver.start();
  System.out.println( "Webserver/XmlRpcServer started successfully!");
          
      }
  }