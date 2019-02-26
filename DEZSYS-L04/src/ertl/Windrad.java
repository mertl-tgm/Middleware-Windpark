package ertl;

import java.io.IOException;
import java.util.Random;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.webserver.WebServer;

public class Windrad {
	private WebServer webserver;
	private int port = 0;
	
	public void startWebserver(int port) throws XmlRpcException, IOException {
		this.port = port;
		
		this.webserver = new WebServer(this.port);
		XmlRpcServer xmlRpcServer = this.webserver.getXmlRpcServer();
		  
		PropertyHandlerMapping phm = new PropertyHandlerMapping();
		phm.addHandler("Windrad", Windrad.class);
		xmlRpcServer.setHandlerMapping(phm);
		  
		this.webserver.start();
		System.out.println("Windrad Port: " + this.port + " gestartet");
	}

	public String getData(String param) {
		return this.generateData();
	}
	
	private String generateData() {
		StringBuilder data = new StringBuilder();
		
		/*
		 * Data: aktStrom#value#Einheit#Blindstrom#value#Einheit#Wind..#Blatz..#Temp..#Um..
		 */
		Random rand = new Random();
		data.append("aktStrom#" + (rand.nextDouble()*3 + 2) + "#" + "MW" + "#");
		data.append("Blindstrom#" + (rand.nextDouble()*3 + 2)*(rand.nextDouble()*0.1) + "#" + "var" + "#");
		data.append("Windgeschwindigkeit#" + (rand.nextDouble()*4 + 6) + "#" + "ms" + "#");
		data.append("Blattposition#" + rand.nextDouble()*180 + "#" + "°" + "#");
		data.append("Temperatur#" + (rand.nextDouble()*45 - 10) + "#" + "°C" + "#");
		data.append("Umdrehungen#" + (rand.nextDouble()*13 + 5) + "#" + "upm");
		
		return data.toString();
	}
	
	public void shutdownWebserver() {
		this.webserver.shutdown();
	}
}
