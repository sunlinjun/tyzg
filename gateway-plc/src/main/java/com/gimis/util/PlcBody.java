package com.gimis.util;

import java.io.Serializable;

public class PlcBody implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

 
	
	//ID
	private String plcId;
	
	//命令ID
	private int cmdNo;
	
	//整包长度  ,   整包长度=报文长度+3
	private int size;
	
	//报文序号
	private int seqNo;
	
	//数据类型
	private int dataType=0;
 
	//数据内容
	private byte[] data;
	
	public PlcBody(){
		
	}

	public String getPlcId() {
		return plcId;
	}

	public void setPlcId(String plcId) {
		this.plcId = plcId;
	}

	public int getCmdNo() {
		return cmdNo;
	}

	public void setCmdNo(int cmdNo) {
		this.cmdNo = cmdNo;
	}

 

	public int getDataType() {
		return dataType;
	}

	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
	public String toString(){
		StringBuilder sb=new StringBuilder();	
 
		sb.append(" id:").append(plcId);
		sb.append(" cmdNo:").append(cmdNo);
		sb.append(" size:").append(size);
		sb.append(" SeqNo:").append(seqNo);
		sb.append(" dataType:").append(dataType);
		return sb.toString();
	}

 

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getSeqNo() {
		return seqNo;
	}

	public void setSeqNo(int seqNo) {
		this.seqNo = seqNo;
	}
	
 

}
