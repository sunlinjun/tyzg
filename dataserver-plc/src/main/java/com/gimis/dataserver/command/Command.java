package com.gimis.dataserver.command;

public class Command {
	
	//ID
	private int ID;
	
	//设备ID
	private String gpsID;
	
	//DEVICE UNID
	private String deviceUNID;
		
	//COMMAND_LOG表的ID
	private int commandLogID;
	
	//有效期
	private long validTime;
	
	//命令ID
	private int commandID;
	
	//命令内容
	private String cmdContent;

	public String getGpsID() {
		return gpsID;
	}

	public void setGpsID(String gpsID) {
		this.gpsID = gpsID;
	}
 
	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public String getDeviceUNID() {
		return deviceUNID;
	}

	public void setDeviceUNID(String deviceUNID) {
		this.deviceUNID = deviceUNID;
	}

	public int getCommandLogID() {
		return commandLogID;
	}

	public void setCommandLogID(int commandLogID) {
		this.commandLogID = commandLogID;
	}

	public int getCommandID() {
		return commandID;
	}

	public void setCommandID(int commandID) {
		this.commandID = commandID;
	}

	public String getCmdContent() {
		return cmdContent;
	}

	public void setCmdContent(String cmdContent) {
		this.cmdContent = cmdContent;
	}

	public long getValidTime() {
		return validTime;
	}

	public void setValidTime(long validTime) {
		this.validTime = validTime;
	} 
	
	
	public String toString(){
		return "GPSID:"+gpsID
				+" commandID:"+commandID
		        +" cmdContent:"+cmdContent
		        +" validTime:"+validTime;
				
		
		
	}
	

}
