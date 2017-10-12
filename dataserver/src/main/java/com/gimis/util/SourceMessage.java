package com.gimis.util;

import java.util.Date;

/*
 * 源码消息体
 */
public class SourceMessage {
		
	//终端地址
	private String address;
	
	//网关号
	private String gateWayID;
	
	//终端数据
	private byte[] data;
		
	//时间
	private String DT;
	
	public SourceMessage(String str){
		String[] strArray=str.split(",");
		if(strArray!=null && strArray.length==4){
			gateWayID=strArray[0];
			DT=strArray[1];
			address=strArray[2];
			data=MessageTools.hexStringToByte(strArray[3]);	
		}
	}
	
	public SourceMessage(String gateWayID,String address,byte[] data){
		this.gateWayID=gateWayID;
		this.address=address;
		this.data=data;
		DT=(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date());
	}
	
	public String getDT(){
		return DT;
	}
	
	public String getAddress(){
		return address;
	}
	
	public void setData(byte[] data){
		this.data=data;
	}
	
	public byte[] getData(){
		return data;
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
