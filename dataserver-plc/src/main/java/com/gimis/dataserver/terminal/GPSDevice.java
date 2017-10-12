package com.gimis.dataserver.terminal;

import java.util.Date;

public class GPSDevice {
	
	//gpsID
	private String gpsID;
	
	//device_unid
	private String device_unid;
	
	//数据字典
	private String fiber_unid;
 
	//从redis获取时间
	private Date getDate;
	
	public GPSDevice(){
		getDate=new Date();
	}

	public String getGpsID() {
		return gpsID;
	}

	public void setGpsID(String gpsID) {
		this.gpsID = gpsID;
	}

	public String getDevice_unid() {
		return device_unid;
	}

	public void setDevice_unid(String device_unid) {
		this.device_unid = device_unid;
	}

	public Date getGetDate() {
		return getDate;
	}

	public String getFiber_unid() {
		return fiber_unid;
	}

	public void setFiber_unid(String fiber_unid) {
		this.fiber_unid = fiber_unid;
	}

}
