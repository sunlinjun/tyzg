package com.gimis.dataserver.terminal;

import java.util.Date;

/*
 * gps终端 与device 设备对应管理
 */
public class GPSDevice {
	
	//gpsID
	private String gpsID;
	
	//device_unid
	private String device_unid;
	
	//数据字典
	private String fiber_unid;
 
	//从redis获取时间
	private Date getDate;
	
	//所属机构
	private int unit_id;
	
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

	public int getUnit_id() {
		return unit_id;
	}

	public void setUnit_id(int unit_id) {
		this.unit_id = unit_id;
	}

 
 
 
}
