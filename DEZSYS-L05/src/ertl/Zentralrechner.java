package ertl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class Zentralrechner extends Thread {
	private static String user = ActiveMQConnection.DEFAULT_USER;
	private static String password = ActiveMQConnection.DEFAULT_PASSWORD;
	private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
	private static ArrayList<String> subject = new ArrayList<>();
	
	private ArrayList<Session> session = new ArrayList<>();
	private ArrayList<Connection> connection = new ArrayList<>();
	private ArrayList<MessageConsumer> consumer = new ArrayList<>();
	private ArrayList<Destination> destination = new ArrayList<>();
	
	private ArrayList<Session> sessionTopic = new ArrayList<>();
	private ArrayList<Connection> connectionTopic = new ArrayList<>();
	private ArrayList<MessageProducer> producerTopic = new ArrayList<>();
	private ArrayList<Destination> destinationTopic = new ArrayList<>();
	
	private boolean run;
	
	public Zentralrechner() {
		this.run = true;
		
		this.loadConfig();
		
		try {
			for (String subject : subject) {
				ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
				Connection con = connectionFactory.createConnection();
				con.start();
				this.connection.add(con);
		
				Session ses = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
				this.session.add(ses);
				Destination des = ses.createQueue(subject);
				this.destination.add(des);
				
				this.consumer.add(ses.createConsumer(des));
			}
		} catch (Exception e) {
			System.out.println("[MessageConsumer] Caught: " + e);
			e.printStackTrace();
			this.run = false;
		}
		
		// Create the connection.
		try {
			for (String subject : subject) {
				ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
				Connection con = connectionFactory.createConnection();
				con.start();
				this.connectionTopic.add(con);

				// Create the session
				Session ses = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
				this.sessionTopic.add(ses);
				Destination des = ses.createTopic(subject);
				this.destinationTopic.add(des);

				// Create the producer.
				MessageProducer pro = ses.createProducer(des);
				pro.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
				this.producerTopic.add(pro);
			}
		} catch (Exception e) {
			System.out.println("[MessageProducer] Caught: " + e);
		} 
		
		this.start();
	}
	
	private void loadConfig() {
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLEventReader eventReader = factory.createXMLEventReader(new FileReader("config.xml"));
			
			while (eventReader.hasNext()) {
				XMLEvent event = eventReader.nextEvent();
				switch (event.getEventType()) {
				case XMLStreamConstants.CHARACTERS:
					Characters characters = event.asCharacters();
					if (characters.getData().trim().length() != 0) {
						subject.add(characters.getData().trim());
					}
					break;
				default: break;
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			return;
		} catch (XMLStreamException e) {
			System.out.println("XML file invailed");
			return;
		}
	}
	
	@Override
	public void run() {
		super.run();
		while(this.run) {
			StringBuilder result = new StringBuilder();
			try {
				for (int i = 0; i < this.consumer.size(); i++) {
					MessageConsumer consumer = this.consumer.get(i);
					TextMessage message = (TextMessage) consumer.receive();
					boolean recevied = false;
					if (message != null) {
						System.out.println("Received new Windpark informations.");
						message.acknowledge();
						recevied = true;
					}
					
					//Topic
					String message3 = "not successfull";
					if (recevied) message3 = "SUCCESS";
					TextMessage message2 = this.sessionTopic.get(i).createTextMessage(message3);
					this.producerTopic.get(i).send(message2);
					
					StringBuilder temp = new StringBuilder(message.getText());
					String windpark = "<windpark id=\"" + subject.get(i) + "\" time=\"" + new Date(System.currentTimeMillis()).toString() + "\">\n" + temp.substring(50, temp.length());
					result.append(windpark);
				}
				
				String[] temp = result.toString().split("\n");
				String file = "";
				for (String string : temp) {
					file += "\t" + string + "\n";
				}
				
				File f = new File("Zentralrechner.xml");
				if(!f.exists() && !f.isDirectory()) { 
				    f.createNewFile();
				    try (BufferedWriter bw = new BufferedWriter(new FileWriter("Zentralrechner.xml"))) {
						String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<zentralrechner>\n";
						bw.write(content);
					} catch (IOException e) {
						System.out.println("Could not write to Zentralrechner.xml");
						return;
					}
				}
				
				try {
			        File inFile = new File("Zentralrechner.xml");
			        File tempFile = new File(inFile.getAbsolutePath() + ".tmp");
			        BufferedReader br = new BufferedReader(new FileReader("Zentralrechner.xml"));
			        PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

			        String line = null;
			        while ((line = br.readLine()) != null) {
			            if (!line.trim().contains("</zentralrechner>")) {
			                pw.println(line);
			                pw.flush();
			            }
			        }
			        pw.close();
			        br.close();

			        if (!inFile.delete()) {
			            System.out.println("Could not delete file");
			            return;
			        }

			        if (!tempFile.renameTo(inFile)) {
			        	System.out.println("Could not rename file");
			        	return;
			        }

			    } catch (FileNotFoundException ex) {
			    	System.out.println("File not found");
			    	return;
			    } catch (IOException ex) {
			        System.out.println("IOException");
			        return;
			    }
				
				try {
					file += "\n</zentralrechner>";
				    Files.write(Paths.get("Zentralrechner.xml"), file.getBytes(), StandardOpenOption.APPEND);
				}catch (IOException e) {
					System.out.println("Could not write to Zentralrechner.xml");
					return;
				}
			} catch (Exception e) {
				System.out.println("[MessageConsumer] Caught: " + e);
				return;
			}
		}
	}
}
