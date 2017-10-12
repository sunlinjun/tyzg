package com.gimis.dataserver.can;

import java.util.HashMap;
 
/*
 * 解析后的CAN数据体    
 */
public class CANHashBody {
	
    private String gps_id;
    
    private String device_unid;
    
	//数据字典
	private String fiber_unid;
 
    private HashMap<String, String> canData;

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

	public HashMap<String, String> getCanData() {
		return canData;
	}

	public void setCanData(HashMap<String, String> canData) {
		this.canData = canData;
	}

	public String getFiber_unid() {
		return fiber_unid;
	}

	public void setFiber_unid(String fiber_unid) {
		this.fiber_unid = fiber_unid;
	}
}
