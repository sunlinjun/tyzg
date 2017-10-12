package com.gimis.dataserver.gps;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.gimis.util.MessageTools;

 

public class GPSBody implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5175907237275148404L;

	//1 是从CAN包来的GPS
	private int isCAN=0;
	
    private String gps_id;
    
    private String device_unid;
 
	// 上传方式 0-心跳时间到上报 1-盲区上报 2-电话呼叫上报 3- GPRS/SMS呼叫
	private int uploadType;
	
	//经度
	private Integer longitude;
	
	//纬度
	private Integer latitude;
	
	//速度
	private Short speed;
	
	//里程  单位 米
	private Integer distance=0;
	
	//方向
	private Short direction;
	
	//高度
	private Short height;
	
	//软件版本
	private String softVersion="";
	
	//硬件版本
	private Short hardwareVersion=0;
	
	//GPS时间
	private Date gpsTime;
	
	//服务器收到时间
	private String serverTime;

	//ACC状态  1 :开  0: 关
	private byte accStatus;
	
	//定位状态 1: 定位 0：未定位
	private byte locationStatus;
	
	//GPS模块状态 0：正常1:GPS模块异常 
	private byte gpsModelStatus;
	
	//供电状态   0:外电供电  1:电池供电；
	private byte powerBatteryStatus;  
	
	//收到卫星数  *****************众想是锁车状态*******************************
	private byte satelliteCount;

	public int getUploadType() {
		return uploadType;
	}

	public void setUploadType(int uploadType) {
		this.uploadType = uploadType;
	}
	public Double getLongitude() {
		if (null != longitude)
        {
				    
            return MessageTools.getSixDoubleValue((double) longitude / 1000000);
        }
        else
        {
            return null;
        }
	}

	public void setLongitude(Integer longitude) {
		this.longitude = longitude;
	}

	public Double getLatitude() {
		if (null != latitude)
        {
            return MessageTools.getSixDoubleValue((double) latitude / 1000000);
        }
        else
        {
            return null;
        }
	}

	public void setLatitude(Integer latitude) {
		this.latitude = latitude;
	}
	
	public Integer getDistance() {
		return distance;
	}

	public void setDistance(Integer distance) {
		this.distance = distance;
	}

	public short getDirection() {
		return direction;
	}

	public void setDirection(short direction) {
		this.direction = direction;
	}

	public Short getHeight() {
		return height;
	}

	public void setHeight(Short height) {
		this.height = height;
	}

	public String getSoftVersion() {
		return softVersion;
	}

	public void setSoftVersion(String softVersion) {
		this.softVersion = softVersion;
	}

	public Short getHardwareVersion() {
		return hardwareVersion;
	}

	public void setHardwareVersion(Short hardwareVersion) {
		this.hardwareVersion = hardwareVersion;
	}

 

	public String getServerTime() {
		return serverTime;
	}

	public void setServerTime(String serverTime) {
		this.serverTime = serverTime;
	}

	public byte getAccStatus() {
		return accStatus;
	}

	public void setAccStatus(byte accStatus) {
		this.accStatus = accStatus;
	}

	public byte getLocationStatus() {
		return locationStatus;
	}

	public void setLocationStatus(byte locationStatus) {
		this.locationStatus = locationStatus;
	}

	public byte getGpsModelStatus() {
		return gpsModelStatus;
	}

	public void setGpsModelStatus(byte gpsModelStatus) {
		this.gpsModelStatus = gpsModelStatus;
	}

	public byte getPowerBatteryStatus() {
		return powerBatteryStatus;
	}

	public void setPowerBatteryStatus(byte powerBatteryStatus) {
		this.powerBatteryStatus = powerBatteryStatus;
	}

	public byte getSatelliteCount() {
		return satelliteCount;
	}

	public void setSatelliteCount(byte satelliteCount) {
		this.satelliteCount = satelliteCount;
	}

	public Date getGpsTime() {
		return gpsTime;
	}

	public void setGpsTime(Date gpsTime) {
		this.gpsTime = gpsTime;
	}

    public Double getSpeed()
    {
        return MessageTools.getShortValue(speed) * 0.5;
    }

	public void setSpeed(short speed) {
		this.speed = speed;
	}
 
	
	public String getGps_id() {
		return gps_id;
	}

	public void setGps_id(String gps_id) {
		this.gps_id = gps_id;
	}
	
 

	public HashMap<String, String> ToMap(DecimalFormat floatFormat,SimpleDateFormat dateformat){
		
		if(this.getGps_id()==null){
			return null;
		}
		HashMap<String, String> map=new HashMap<String,String>();
		map.put("gps_id", this.getGps_id());
 		
		if(this.getServerTime()!=null){
 
			map.put("uploadType", Integer.toString(this.getUploadType()));
					
			map.put("longitude", floatFormat.format(this.getLongitude()));
			map.put("latitude", floatFormat.format(this.getLatitude()));
			
			if(this.getDistance()!=null){
				map.put("distance", Integer.toString(this.getDistance()));
			}
			
			map.put("direction",Integer.toString(this.getDirection()) );
			if(this.getHeight()!=null){
				map.put("height",Integer.toString(this.getHeight()) );
			}
			
			
			map.put("gpsTime",  dateformat.format(this.getGpsTime())	  );
			map.put("serverTime", this.getServerTime()  );
			map.put("locationStatus",Integer.toString(this.getLocationStatus() ) );
			
			if(isCAN==0){
				map.put("softVersion", this.getSoftVersion() );
				map.put("hardwareVersion", Integer.toString(this.getHardwareVersion()));
 
				map.put("accStatus",Integer.toString(this.getAccStatus() ) );
				
				map.put("gpsModelStatus",Integer.toString(this.getGpsModelStatus() ) );
							
				map.put("powerBatteryStatus", Integer.toString(this.getPowerBatteryStatus()) );
				map.put("satelliteCount",Integer.toString(this.getSatelliteCount() ) );	
				
 
			}
			

 			
		}
	
		return  map;
	}

	public String getDevice_unid() {
		return device_unid;
	}

	public void setDevice_unid(String device_unid) {
		this.device_unid = device_unid;
	}

	public int getIsCAN() {
		return isCAN;
	}

	public void setIsCAN(int isCAN) {
		this.isCAN = isCAN;
	}

 
 
	 
}

