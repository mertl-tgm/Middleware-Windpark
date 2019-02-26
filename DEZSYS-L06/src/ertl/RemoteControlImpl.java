package ertl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemoteControlImpl extends UnicastRemoteObject implements RemoteControl {
	private static final long serialVersionUID = 1L;
	private Windrad w;
	
	public RemoteControlImpl(Windrad w) throws RemoteException {
		super();
		this.w = w;
	}

	@Override
	public boolean setRun(boolean run) throws RemoteException {
		this.w.setRun(run);
		return true;
	}

	@Override
	public String getInformation() throws RemoteException {
		StringBuilder informationen = new StringBuilder();
		informationen.append("Windrad Informationen:\n");
		informationen.append("Running: " + this.w.isRun() + "\n");
		informationen.append("Blattwinkel: " + this.w.getBlattwinkel()  + "\n");
		informationen.append("Gondelwinkel: " + this.w.getGondelwinkel() + "\n");
		informationen.append("Blindleistung: " + this.w.getBlindleistung() + "\n");
		informationen.append("Wirkleistung: " + this.w.getWirkleistung());
		return informationen.toString();
	}

	@Override
	public boolean setBlattwinkel(double winkel) throws RemoteException {
		this.w.setBlattwinkel(winkel);
		return true;
	}

	@Override
	public boolean setGondelwinkel(double winkel) throws RemoteException {
		this.w.setGondelwinkel(winkel);
		return true;
	}

	@Override
	public boolean setBlindleistung(double wert) throws RemoteException {
		this.w.setBlindleistung(wert);
		return true;
	}

	@Override
	public boolean setWirkleistung(double wert) throws RemoteException {
		this.w.setWirkleistung(wert);
		return true;
	}
}
