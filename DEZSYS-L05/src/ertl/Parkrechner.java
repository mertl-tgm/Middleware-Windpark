package ertl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class Parkrechner extends Thread {
	private static String user = ActiveMQConnection.DEFAULT_USER;
	private static String password = ActiveMQConnection.DEFAULT_PASSWORD;
	private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
	private String subject = "";
	
	private Session session = null;
	private Connection connection = null;
	private MessageProducer producer = null;
	private Destination destination = null;
	
	private Session sessionTopic = null;
	private Connection connectionTopic = null;
	private MessageConsumer consumerTopic = null;
	private Destination destinationTopic = null;
	
	private ArrayList<XmlRpcClient> clients;
	private static final String host = "localhost";
	private boolean run;
	
	public Parkrechner(String subject) {
		this.clients = new ArrayList<>();
		this.run = false;
		this.subject = subject;
		
		//Create Queue
		try {
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
			this.connection = connectionFactory.createConnection();
			this.connection.start();

			this.session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			this.destination = this.session.createQueue(this.subject);

			this.producer = this.session.createProducer(this.destination);
			this.producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		} catch (Exception e) {
			System.out.println("[MessageProducer] Caught: " + e);
		}
		
		//Create Topic
		try {
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
			this.connectionTopic = connectionFactory.createConnection();
			this.connectionTopic.start();

			this.sessionTopic = this.connectionTopic.createSession(false, Session.AUTO_ACKNOWLEDGE);
			this.destinationTopic = this.sessionTopic.createTopic(subject);

			this.consumerTopic = this.sessionTopic.createConsumer(this.destinationTopic);
		} catch (Exception e) {
			System.out.println("[MessageConsumer] Caught: " + e);
		} 
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
			this.sendMessage(this.processResults(results.toString()));
			System.out.println("Windrad Informationen aktualisiert");
			
			//Topic
			try {
				TextMessage message = (TextMessage) consumerTopic.receive();
				if (message != null) {
					System.out.println("Informationen empfangen: " + message.getText());
					message.acknowledge();
				}
			} catch (JMSException e1) {
				System.out.println("Exception receving topic");
			}
			
			try {
				sleep(500);
			} catch (InterruptedException e) {
				System.out.println("InterruptException thrown while waiting");
			}
		}
	}
	
	private void sendMessage(String content) {
		try {
			TextMessage message = this.session.createTextMessage(content);
			this.producer.send(message);
		} catch (Exception e) {
			System.out.println("[MessageProducer] Caught: " + e);
			this.stopConnection();
		}
	}
	
	private void stopConnection() {
		//Queue
		try {
			this.producer.close();
		} catch (Exception e) {
		}
		try {
			this.session.close();
		} catch (Exception e) {
		}
		try {
			this.connection.close();
		} catch (Exception e) {
		}
		
		//Topic
		try {
			this.consumerTopic.close();
		} catch (Exception e) {
		}
		try {
			this.sessionTopic.close();
		} catch (Exception e) {
		}
		try {
			this.connection.close();
		} catch (Exception e) {
		}
	}
	
	private String processResults(String results) {
		String[] windrad = results.split("\n");
		
		try {
			XMLOutputFactory xof = XMLOutputFactory.newInstance();
			XMLStreamWriter xtw = null;
			xtw = xof.createXMLStreamWriter(new FileOutputStream(this.subject + "_output.xml"), "utf-8");
			
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
		
		Path path = FileSystems.getDefault().getPath("", this.subject + "_output.xml");
		String file = "";
		try {
			file = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.out.println("Reading Windrad Informationen throws a IOException");
		}
		return file;
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
