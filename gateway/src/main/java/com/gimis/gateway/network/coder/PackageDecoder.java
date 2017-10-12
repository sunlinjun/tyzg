package com.gimis.gateway.network.coder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
 

public class PackageDecoder extends CumulativeProtocolDecoder {

	//包头标志
	private static byte headTag= (byte)0x7E;
	
	//包尾标志
	private static byte tailTag= (byte)0x7E;
	
	//包最小长度 23
	private static int packMinSize=23;
	
	//包最大长度2K
	private static int packMaxSize=2048;
 

	//return false 表示继续等待接受数据 ，true表示退出 doDecode,给 下一个流程处理
	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		// TODO Auto-generated method stub

		// 简单协议解析
		/*
		int len = in.limit();
		byte[] tempdata = new byte[len];
		in.get(tempdata, 0, len);
		if (len >= 23) {			
			if (tempdata[0] == MessageConstants.MESSAGE_FLAG && tempdata[len - 1] == MessageConstants.MESSAGE_FLAG) {
				out.write(tempdata);
				return true;
			}
		}
		return false;
		*/
				
		//复杂的协议读取 ,组包
		//没有接收完成，继续等待
		if(in.remaining()<packMinSize){
			return false;
		}
 
		int start = in.position();
		
		int len=in.remaining();		
		
		//最多接收packMaxSize
		if(len>packMaxSize){
			len=packMaxSize;
		}
		
		byte[] array = new byte[len];			
		in.get(array,0,len);
		
		//包头不对 返回 
		if(array[0]!=headTag){
			session.closeOnFlush();
			return false;
		}
		
		//查找包尾
		boolean bFind=false;
		int pos=0;
		for(int index=1;index<len;index++){
			if(array[index]==(byte)tailTag){
				bFind=true;
				pos=index;
				break;					
			}
		}
		
		//包尾没有找到,
		if(bFind==false){
			// 等待完整的包
			if(len<packMaxSize){
				in.position(start);
				return false;	
			}else{
				//超过最大长度，退出。
				session.closeOnFlush();
				return false;
			}			
		}
		
		//包尾找到 但是 长度不对
		if(pos+1<packMinSize){
			session.closeOnFlush();
			return false;
		}
 
		byte[] outArray =new byte[pos+1];
		System.arraycopy(array, 0, outArray, 0, pos+1);
		
		//如果有多包，将位置指向下一包
		//if(pos+1<len){
		//	in.position(pos+1);
		//}
		
		out.write(outArray);
		return true;			
	}



}
