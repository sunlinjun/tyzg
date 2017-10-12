package com.gimis.dataserver.can;

public class CANDef {
	
	private String canID;
	private String fiberUNID;
	private int byteOrder;
	private int dataType;
	private String propertyName;
	private String fieldName;
	private String highBytes;
	private String lowBytes;
	private int offset;
	private double resolution;
	private int signBit;
	public int getByteOrder() {
		return byteOrder;
	}
	public void setByteOrder(int byteOrder) {
		this.byteOrder = byteOrder;
	}
	public int getDataType() {
		return dataType;
	}
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getHighBytes() {
		return highBytes;
	}
	public void setHighBytes(String highBytes) {
		this.highBytes = highBytes;
	}
	public String getLowBytes() {
		return lowBytes;
	}
	public void setLowBytes(String lowBytes) {
		this.lowBytes = lowBytes;
	}
 
	public double getResolution() {
		return resolution;
	}
	public void setResolution(double resolution) {
		this.resolution = resolution;
	}
	public int getSignBit() {
		return signBit;
	}
	public void setSignBit(int signBit) {
		this.signBit = signBit;
	}
	public String getCanID() {
		return canID;
	}
	public void setCanID(String canID) {
		this.canID = canID;
	}
	public String getFiberUNID() {
		return fiberUNID;
	}
	public void setFiberUNID(String fiberUNID) {
		this.fiberUNID = fiberUNID;
	}
	
 
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
}
