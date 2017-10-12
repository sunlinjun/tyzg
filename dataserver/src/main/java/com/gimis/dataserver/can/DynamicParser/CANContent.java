package com.gimis.dataserver.can.DynamicParser;

public class CANContent {

	//0 无效    	
	//1 完整的字节  startPos,endPos
	//2 在一个字节中 看head 
	//3 组合       2个字节  head+tail   3个字节以及以上  head+ (startPo-endPos) + tail   
	private int valueType;
	private int startPos;   //开始位置   ==-1无效 
	private int endpos;     //end位置   ==-1无效
	
	//头部
	private CANDataPos head;  //start==-1无效
	//尾部
	private CANDataPos tail;  //start==-1无效
	
	public CANContent() {
		// TODO Auto-generated constructor stub
		
		valueType=0;		
		setHead(new CANDataPos());
		setTail(new CANDataPos());		 		
		setStartPos(-1);
		setEndpos(-1);		 		
	}

	
	//分为head+content+tail 三个部分  当为bit类型时候， head有效，其余都无效	
	public int getValueType() {
		return valueType;
	}
	public void setValueType(int valueType) {
		this.valueType = valueType;
	}


	public int getStartPos() {
		return startPos;
	}


	public void setStartPos(int startPos) {
		this.startPos = startPos;
	}

	public int getEndpos() {
		return endpos;
	}


	public void setEndpos(int endpos) {
		this.endpos = endpos;
	}

	public CANDataPos getHead() {
		return head;
	}


	public void setHead(CANDataPos head) {
		this.head = head;
	}


	public CANDataPos getTail() {
		return tail;
	}


	public void setTail(CANDataPos tail) {
		this.tail = tail;
	}



}
