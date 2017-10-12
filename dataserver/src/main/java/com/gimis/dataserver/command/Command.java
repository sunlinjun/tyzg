package com.gimis.dataserver.command;

public class Command {
	private int ID;
	private String gpsID;
	private String deviceUNID;
	private int commandLogID;
	private long validTime;
	private int commandID;
	private String cmdContent;

	public String getGpsID() {
		return this.gpsID;
	}

	public void setGpsID(String gpsID) {
		this.gpsID = gpsID;
	}

	public int getID() {
		return this.ID;
	}

	public void setID(int iD) {
		this.ID = iD;
	}

	public String getDeviceUNID() {
		return this.deviceUNID;
	}

	public void setDeviceUNID(String deviceUNID) {
		this.deviceUNID = deviceUNID;
	}

	public int getCommandLogID() {
		return this.commandLogID;
	}

	public void setCommandLogID(int commandLogID) {
		this.commandLogID = commandLogID;
	}

	public int getCommandID() {
		return this.commandID;
	}

	public void setCommandID(int commandID) {
		this.commandID = commandID;
	}

	public String getCmdContent() {
		return this.cmdContent;
	}

	public void setCmdContent(String cmdContent) {
		this.cmdContent = cmdContent;
	}

	public long getValidTime() {
		return this.validTime;
	}

	public void setValidTime(long validTime) {
		this.validTime = validTime;
	}

	public String toString() {
		return "GPSID:" + this.gpsID + " commandID:" + this.commandID + " cmdContent:" + this.cmdContent + " validTime:"
				+ this.validTime;
	}
}