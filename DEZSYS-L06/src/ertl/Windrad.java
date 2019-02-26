package ertl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Random;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.webserver.WebServer;

public class Windrad {
	private WebServer webserver;
	private int port = 0;
	private RemoteControlImpl im;
	private String LOOKUPNAME = "";
	
	private static int uniqueId = 0;
	private double blattwinkel;
	private double gondelwinkel;
	private double blindleistung;
	private double wirkleistung;
	private boolean run;
	
	public Windrad() {
		Random rand = new Random();
		this.blattwinkel = rand.nextDouble()*180;
		this.gondelwinkel = rand.nextDouble()*45;
		this.blindleistung = (rand.nextDouble()*3 + 2)*(rand.nextDouble()*0.1);
		this.wirkleistung = (rand.nextDouble()*3 + 2);
		
		//RMI
		this.LOOKUPNAME = "w" + uniqueId++;
		try {
			this.im = new RemoteControlImpl(this);
			Naming.rebind(this.LOOKUPNAME, this.im);
		} catch (RemoteException e) {
			System.out.println("Error while creating RemoteControlImpl");
		} catch (MalformedURLException e) {
			System.out.println("URL Exception");
		}
	}
	
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
		data.append("Blattposition#" + rand.nextDouble()*180 + "#" + "Grad" + "#");
		data.append("Temperatur#" + (rand.nextDouble()*45 - 10) + "#" + "Grad C" + "#");
		data.append("Umdrehungen#" + (rand.nextDouble()*13 + 5) + "#" + "upm");
		
		return data.toString();
	}
	
	public void shutdownWebserver() {
		this.webserver.shutdown();
	}

	public double getBlattwinkel() {
		return blattwinkel;
	}

	public void setBlattwinkel(double blattwinkel) {
		this.blattwinkel = blattwinkel;
	}

	public double getGondelwinkel() {
		return gondelwinkel;
	}

	public void setGondelwinkel(double gondelwinkel) {
		this.gondelwinkel = gondelwinkel;
	}

	public double getBlindleistung() {
		return blindleistung;
	}

	public void setBlindleistung(double blindleistung) {
		this.blindleistung = blindleistung;
	}

	public double getWirkleistung() {
		return wirkleistung;
	}

	public void setWirkleistung(double wirkleistung) {
		this.wirkleistung = wirkleistung;
	}

	public boolean isRun() {
		return run;
	}

	public void setRun(boolean run) {
		this.run = run;
	}
}
