package com.gimis.dataserver.plc;

public class OffsetResolution {
	
	//偏移量
	private int offset;
	
	//分辨率
	private double resolution; 
	
	public OffsetResolution(){
		setOffset(0);
		setResolution(1);
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public double getResolution() {
		return resolution;
	}

	public void setResolution(double resolution) {
		this.resolution = resolution;
	}

}
