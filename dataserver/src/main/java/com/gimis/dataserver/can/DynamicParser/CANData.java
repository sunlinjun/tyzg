package com.gimis.dataserver.can.DynamicParser;

 

public class CANData {	 
	private int byteOrder;   //大小端   0小端 1大端
	private String propertyName;     
	private String fieldName;
	private String highBytes;        //高位
	private String lowBytes;         //地位  16-31 
	private int offset;              //偏移量
	private String resolution;          //分辨率	
	
	private CANContent low_content;
	private CANContent high_content;
	
	private int signBit;   //符号位  0没有  1有符号位
	
	//计算字段
	private int _valueType;  //类型   -1：无效  0:bit 1:数组 
	
	public CANData(){		
		setLow_content(new CANContent());
		setHigh_content(new CANContent());
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
	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public String getResolution() {
		return resolution;
	}
	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public int getByteOrder() {
		return byteOrder;
	}
	public void setByteOrder(int byteOrder) {
		this.byteOrder = byteOrder;
	}

 
 
 
	public int get_valueType() {
		return _valueType;
	}

	public void set_valueType(int _valueType) {
		this._valueType = _valueType;
	}
 

	public CANContent getLow_content() {
		return low_content;
	}


	public void setLow_content(CANContent low_content) {
		this.low_content = low_content;
	}


	public CANContent getHigh_content() {
		return high_content;
	}


	public void setHigh_content(CANContent high_content) {
		this.high_content = high_content;
	}


	public int getSignBit() {
		return signBit;
	}


	public void setSignBit(int signBit) {
		this.signBit = signBit;
	}


	
 
 
	
 

}
