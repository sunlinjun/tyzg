package com.gimis.dataserver.command;

public class SendCommandData {
	private String gpsID;
	private int seqNO;
	private int commanLogID;
	private int count = 3;
	private long dt;
	private byte[] data;

	public String getGpsID() {
		return this.gpsID;
	}

	public void setGpsID(String gpsID) {
		this.gpsID = gpsID;
	}

	public int getSeqNO() {
		return this.seqNO;
	}

	public void setSeqNO(int seqNO) {
		this.seqNO = seqNO;
	}

	public int getCount() {
		return this.count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public long getDt() {
		return this.dt;
	}

	public void setDt(long dt) {
		this.dt = dt;
	}

	public byte[] getData() {
		return this.data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public int getCommanLogID() {
		return this.commanLogID;
	}

	public void setCommanLogID(int commanLogID) {
		this.commanLogID = commanLogID;
	}

	public String toString() {
		return "GPSID:" + this.gpsID + " commanLogID:" + this.commanLogID + " count:" + this.count;
	}
}