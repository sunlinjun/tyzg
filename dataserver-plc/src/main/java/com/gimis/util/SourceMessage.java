package com.gimis.util;

import java.io.Serializable;
import java.util.Date;

public class SourceMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 922587048459621901L;

	// 终端地址
	private String address;

	// 网关号
	private String gateWayID;

	// gps编号
	private String gpsID;

	// 终端数据
	private byte[] data;

	// 时间
	private String DT;

	public String getAddress() {
		return address;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public SourceMessage(String gateWayID, String address, byte[] data) {
		this.setGateWayID(gateWayID);
		this.address = address;
		this.data = data;
		setDT((new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()));
	}

	public String getGateWayID() {
		return gateWayID;
	}

	public void setGateWayID(String gateWayID) {
		this.gateWayID = gateWayID;
	}

	public String getDT() {
		return DT;
	}

	public void setDT(String dT) {
		DT = dT;
	}

	public String getGpsID() {
		return gpsID;
	}

	public void setGpsID(String gpsID) {
		this.gpsID = gpsID;
	}
	
	public String toString(){
		StringBuilder sb=new StringBuilder();
		sb.append(gateWayID);
		sb.append(",").append(DT);
		sb.append(",").append(address);		
		String strData=MessageTools.bytesToString(data);
		sb.append(",").append(strData);
		return sb.toString();
	}

}
