package ertl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class Parkrechner extends Thread {
	private ArrayList<XmlRpcClient> clients;
	private static final String host = "localhost";
	private boolean run;
	
	public Parkrechner() {
		this.clients = new ArrayList<>();
		this.run = false;
	}
	
	@Override
	public void start() {
		super.start();
		this.run = true;
	}
	
	@Override
	public void run() {
		super.run();
		while (this.run) {
			StringBuilder results = new StringBuilder();
			for (int i = 0; i < this.clients.size(); i++) {
				XmlRpcClient client = this.clients.get(i);
				Object[] params = new Object[]{""};
				String result = "";
				try {
					result = (String) client.execute("Windrad.getData", params);
					results.append(result + "\n");
				} catch (XmlRpcException e) {
					this.clients.remove(i);
				}
			}
			this.processResults(results.toString());
			System.out.println("Windrad Informationen aktualisiert");
			try {
				sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void processResults(String results) {
		String[] windrad = results.split("\n");
		
		try {
			XMLOutputFactory xof = XMLOutputFactory.newInstance();
			XMLStreamWriter xtw = null;
			xtw = xof.createXMLStreamWriter(new FileOutputStream("output.xml"), "utf-8");
			
			xtw.writeStartDocument("utf-8", "1.0");
			xtw.writeCharacters("\n");
			xtw.writeStartElement("windpark");
			xtw.writeCharacters("\n");
			for (String result : windrad) {
				xtw.writeCharacters("\t");
				xtw.writeStartElement("windrad");
				xtw.writeCharacters("\n");
				
				String[] values = result.split("#");
				for (int i = 0; i < values.length; i += 3) {
					xtw.writeCharacters("\t\t");
					xtw.writeStartElement(values[i]);
					xtw.writeCharacters("\n\t\t\t");
					xtw.writeStartElement("value");
					xtw.writeCharacters(values[i+1]);
					xtw.writeEndElement();
					xtw.writeCharacters("\n\t\t\t");
					xtw.writeStartElement("unit");
					xtw.writeCharacters(values[i+2]);
					xtw.writeEndElement();
					xtw.writeCharacters("\n\t\t");
					xtw.writeEndElement();
					xtw.writeCharacters("\n");
				}
				xtw.writeCharacters("\t");
				xtw.writeEndElement();
				xtw.writeCharacters("\n");
			}
			xtw.writeEndElement();
			xtw.writeCharacters("\n");

			xtw.writeEndDocument();
		} catch (FileNotFoundException | XMLStreamException e) {
			System.out.println("Exception beim Schreiben der XML Datei!");
		}
	}
	
	public void addClient(int port) {
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		try {
			config.setServerURL(new URL("http://"+ host + ":" + port + "/xmlrpc") );
		} catch (MalformedURLException e) {
			System.out.println("Angegebenes Windrad nicht vorhanden!");
		}
		XmlRpcClient client = new XmlRpcClient();
		client.setConfig(config);
		
		this.clients.add(client);
	}
	
	public ArrayList<XmlRpcClient> getClients() {
		return this.clients;
	}

	public void setClients(ArrayList<XmlRpcClient> clients) {
		this.clients = clients;
	}

	public boolean isRun() {
		return run;
	}

	public void setRun(boolean run) {
		this.run = run;
	}
	
}
