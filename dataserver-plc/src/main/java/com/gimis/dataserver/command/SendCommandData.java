package com.gimis.dataserver.command;

public class SendCommandData {
	
	// GPSID
	private String gpsID;

	// 发送命令序号
	private int seqNO;

	// COMMAND_LOG表的ID
	private int commanLogID;

	// 发送次数
	private int count = 1;

	// 发送时间
	private long dt;

	// 发送命令
	private byte[] data;

	public String getGpsID() {
		return gpsID;
	}

	public void setGpsID(String gpsID) {
		this.gpsID = gpsID;
	}

	public int getSeqNO() {
		return seqNO;
	}

	public void setSeqNO(int seqNO) {
		this.seqNO = seqNO;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public long getDt() {
		return dt;
	}

	public void setDt(long dt) {
		this.dt = dt;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public int getCommanLogID() {
		return commanLogID;
	}

	public void setCommanLogID(int commanLogID) {
		this.commanLogID = commanLogID;
	}
	
	public String toString(){
		return "GPSID:"+gpsID
				+" commanLogID:"+commanLogID
		        +" count:"+count;
	}

}
