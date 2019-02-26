package ertl;

public interface RemoteControl extends java.rmi.Remote {
	public String getInformation() throws java.rmi.RemoteException;
	public boolean setRun(boolean run) throws java.rmi.RemoteException;
	public boolean setBlattwinkel(double winkel) throws java.rmi.RemoteException;
	public boolean setGondelwinkel(double winkel) throws java.rmi.RemoteException;
	public boolean setBlindleistung(double wert) throws java.rmi.RemoteException;
	public boolean setWirkleistung(double wert) throws java.rmi.RemoteException;
}
