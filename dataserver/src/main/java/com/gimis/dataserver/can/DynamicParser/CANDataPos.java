package com.gimis.dataserver.can.DynamicParser;

//在一个字节中
public class CANDataPos {
		
	    private int pos;  //字节位置
		
	    private int start;//字节中bit的起始位置
		
	    private int end;  //字节中bit的结束位置
				
		public CANDataPos(){
			start=-1;
			end=-1;
		}
 		
		public int getPos() {
			return pos;
		}
		
		public void setPos(int pos) {
			this.pos = pos;
		}
		
		public int getStart() {
			return start;
		}
		
		public void setStart(int start) {
			this.start = start;
		}
		
		public int getEnd() {
			return end;
		}
		
		public void setEnd(int end) {
			this.end = end;
		}
 
}

