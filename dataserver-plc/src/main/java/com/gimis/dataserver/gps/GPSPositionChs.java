package com.gimis.dataserver.gps;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GPSPositionChs {
	
	//gpsID
	private String gpsId;
	
	//DEVICE UNID
	private String unid;	
	
	//经度
	private Double longitude;
	
	//纬度
	private Double latitude;
	
	//省
	private String province;
	
	//市
	private String city;
	
	//区
	private String area;
	
	//更新位置时间
	private Date locationtime;
	
	public GPSPositionChs(){
		locationtime = new Date();
	}

	public String getGpsId() {
		return gpsId;
	}

	public void setGpsId(String gpsId) {
		this.gpsId = gpsId;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public Date getLocationtime() {
		return locationtime;
	}

	public void setLocationtime(Date locationtime) {
		this.locationtime = locationtime;
	}
	
	public Map<String,String> ToMap(SimpleDateFormat dateformat){
		Map<String,String> map=new HashMap<String,String>();
		map.put("province", this.getProvince());
		map.put("city", this.getCity() );
		map.put("area", this.getArea());
		map.put("locationtime",  dateformat.format(locationtime)  );
		return map;
	}

	public String getUnid() {
		return unid;
	}

	public void setUnid(String unid) {
		this.unid = unid;
	}	
	

}
