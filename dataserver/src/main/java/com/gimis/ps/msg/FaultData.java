package com.gimis.ps.msg;

import java.io.Serializable;
import java.util.Date;

public class FaultData implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2136634899409287720L;

	//故障码
	private long faultCode;
	
	//报警状态
	private byte faultStatus;
	
	//报警时间
	private Date faultTime;

	public long getFaultCode() {
		return faultCode;
	}

	public void setFaultCode(long faultCode) {
		this.faultCode = faultCode;
	}

	public byte getFaultStatus() {
		return faultStatus;
	}

	public void setFaultStatus(byte faultStatus) {
		this.faultStatus = faultStatus;
	}

	public Date getFaultTime() {
		return faultTime;
	}

	public void setFaultTime(Date faultTime) {
		this.faultTime = faultTime;
	}
	

}
