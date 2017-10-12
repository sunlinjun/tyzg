package com.gimis.ps.msg;

import java.io.Serializable;

public class CANFrame implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8366392556966731346L;

	private String CANID;
	
	private byte[] data;

	public String getCANID() {
		return CANID;
	}
	public void setCANID(String cANID) {
		CANID = cANID;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
 
}
