package com.gimis.ps.msg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
 
import com.gimis.util.MessageTools;
 

public class CANBody implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7093269258375335674L;

	private String gps_id;
	
	private String device_unid;
	
	//数据字典
	private String fiber_unid;
	
	// 上传方式 0-心跳时间到上报 1-盲区上报 2-电话呼叫上报 3- GPRS/SMS呼叫
	private int uploadType;
	
	//上报序号
	private int seqID;
	
	//经度
	private Integer longitude;
	
	//纬度
	private Integer latitude;
	
	//速度
	private Short speed;
	
	//里程  单位 米
	private Integer distance;
	
	//方向
	private Short direction;
	
	//高度
	private Short height;	
	
	//GPS时间
	private Date gpsTime;	
	
	//服务器收到时间
	private String serverTime;
	
	private ArrayList<CANFrame> canlist=new ArrayList<CANFrame>();
	
	private HashMap<String,String> contentList;

	public String getGps_id() {
		return gps_id;
	}

	public void setGps_id(String gps_id) {
		this.gps_id = gps_id;
	}

	public ArrayList<CANFrame> getCanlist() {
		return canlist;
	}

	public void setCanlist(ArrayList<CANFrame> canlist) {
		this.canlist = canlist;
	}

	public String getDevice_unid() {
		return device_unid;
	}

	public void setDevice_unid(String device_unid) {
		this.device_unid = device_unid;
	}

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

    public Double getSpeed()
    {
        return MessageTools.getShortValue(speed) * 0.5;
    }

	public void setSpeed(Short speed) {
		this.speed = speed;
	}

	public Integer getDistance() {
		return distance;
	}

	public void setDistance(Integer distance) {
		this.distance = distance;
	}

	public Short getDirection() {
		return direction;
	}

	public void setDirection(Short direction) {
		this.direction = direction;
	}

	public Short getHeight() {
		return height;
	}

	public void setHeight(Short height) {
		this.height = height;
	}

	public Date getGpsTime() {
		return gpsTime;
	}

	public void setGpsTime(Date gpsTime) {
		this.gpsTime = gpsTime;
	}

	public int getSeqID() {
		return seqID;
	}

	public void setSeqID(int seqID) {
		this.seqID = seqID;
	}

	public String getServerTime() {
		return serverTime;
	}

	public void setServerTime(String serverTime) {
		this.serverTime = serverTime;
	}

	public String getFiber_unid() {
		return fiber_unid;
	}

	public void setFiber_unid(String fiber_unid) {
		this.fiber_unid = fiber_unid;
	}

	public HashMap<String,String> getContentList() {
		return contentList;
	}

	public void setContentList(HashMap<String,String> contentList) {
		this.contentList = contentList;
	}

 

 

 
	
}
