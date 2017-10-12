package com.gimis.util;

import java.util.Date;

public class PlcContent {
	
	//Plc终端Id
	private String plcId;
	
	//设备UNID
	private String deviceUnid;
	
	//变量ID
	private String plcVarId;
	
	//变量值
	private String plcVarValue;
	
	//变量时间
	private Date plcVarTime;

	public String getPlcId() {
		return plcId;
	}

	public void setPlcId(String plcId) {
		this.plcId = plcId;
	}

	public String getDeviceUnid() {
		return deviceUnid;
	}

	public void setDeviceUnid(String deviceUnid) {
		this.deviceUnid = deviceUnid;
	}

	public String getPlcVarId() {
		return plcVarId;
	}

	public void setPlcVarId(String plcVarId) {
		this.plcVarId = plcVarId;
	}

	public String getPlcVarValue() {
		return plcVarValue;
	}

	public void setPlcVarValue(String plcVarValue) {
		this.plcVarValue = plcVarValue;
	}

	public Date getPlcVarTime() {
		return plcVarTime;
	}

	public void setPlcVarTime(Date plcVarTime) {
		this.plcVarTime = plcVarTime;
	}

 
}
