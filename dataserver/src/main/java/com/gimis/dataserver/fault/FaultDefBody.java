package com.gimis.dataserver.fault;

/*
 * 报警自定义
 */
public class FaultDefBody {
	private String key;
	private int faultID;
	private int faultCondition;
	private double faultConditionValue;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public int getFaultID() {
		return faultID;
	}
	public void setFaultID(int faultID) {
		this.faultID = faultID;
	}
	public int getFaultCondition() {
		return faultCondition;
	}
	public void setFaultCondition(int faultCondition) {
		this.faultCondition = faultCondition;
	}
	public double getFaultConditionValue() {
		return faultConditionValue;
	}
	public void setFaultConditionValue(double faultConditionValue) {
		this.faultConditionValue = faultConditionValue;
	}
	
 
}
