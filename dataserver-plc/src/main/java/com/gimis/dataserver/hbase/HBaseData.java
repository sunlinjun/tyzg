package com.gimis.dataserver.hbase;

import java.util.HashMap;

public class HBaseData {
	
	//UNID_VarID
	private byte[] rowKey;
	
	private long timestamp;
	
	private HashMap<String, String> record;
 
	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public HashMap<String, String> getRecord() {
		return record;
	}

	public void setRecord(HashMap<String, String> record) {
		this.record = record;
	}

	public byte[] getRowKey() {
		return rowKey;
	}

	public void setRowKey(byte[] rowKey) {
		this.rowKey = rowKey;
	}

 

}
