package ertl;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.xmlrpc.XmlRpcException;

public class Test {
	private static ArrayList<Windrad> windrad;
	
	public static void main(String[] args) throws XmlRpcException, IOException {
		windrad = new ArrayList<>();
		Windrad windrad1 = new Windrad();
		windrad1.startWebserver(8081);
		windrad.add(windrad1);
		windrad1 = new Windrad();
		windrad1.startWebserver(8082);
		windrad.add(windrad1);
		windrad1 = new Windrad();
		windrad1.startWebserver(8083);
		windrad.add(windrad1);
		windrad1 = new Windrad();
		windrad1.startWebserver(8084);
		windrad.add(windrad1);
		
		Parkrechner parkrechner = new Parkrechner("Windpark1");
		parkrechner.addClient(8081);
		parkrechner.addClient(8082);
		parkrechner.addClient(8083);
		parkrechner.addClient(8084);
		parkrechner.start();
		
		windpark();
		new Zentralrechner();
		
		try{
		    Thread.sleep(2000);
		}catch(InterruptedException e){
		    e.printStackTrace();
		}
		
		windrad.get(1).shutdownWebserver();
		
		try{
		    Thread.sleep(5000);
		}catch(InterruptedException e){
		    e.printStackTrace();
		}
		
		windrad.get(2).shutdownWebserver();
		
		try{
		    Thread.sleep(5000);
		}catch(InterruptedException e){
		    e.printStackTrace();
		}
		
		windrad1 = new Windrad();
		windrad1.startWebserver(8085);
		windrad.add(windrad1);
		parkrechner.addClient(8085);
	}
	
	public static void windpark() throws XmlRpcException, IOException {
		Windrad win = new Windrad();
		win.startWebserver(8090);
		win = new Windrad();
		win.startWebserver(8091);
		
		Parkrechner park2 = new Parkrechner("Windpark2");
		park2.addClient(8090);
		park2.addClient(8091);
		park2.start();
	}
	
}
