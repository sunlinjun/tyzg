package com.gimis.ps.msg;

import java.io.Serializable;
import java.util.List;

public class FaultBody implements Serializable{
	
    /**
	 * 
	 */
	private static final long serialVersionUID = -7476232955957002199L;

	private String gps_id;
    
    private String device_unid;
	
 
	private short status;
	
	private List<FaultData> list;

	public short getStatus() {
		return status;
	}

	public void setStatus(short status) {
		this.status = status;
	}

	public List<FaultData> getList() {
		return list;
	}

	public void setList(List<FaultData> list) {
		this.list = list;
	}

	public String getGps_id() {
		return gps_id;
	}

	public void setGps_id(String gps_id) {
		this.gps_id = gps_id;
	}

	public String getDevice_unid() {
		return device_unid;
	}

	public void setDevice_unid(String device_unid) {
		this.device_unid = device_unid;
	}

}
