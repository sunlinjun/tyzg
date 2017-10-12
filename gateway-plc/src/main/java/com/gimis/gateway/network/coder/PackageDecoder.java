package com.gimis.gateway.network.coder;
 
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.gimis.util.MessageTools;
 

public class PackageDecoder extends CumulativeProtocolDecoder {

 
	//包最小长度 3
	private static int packMinSize=3;

	private String getAllInBuffer(IoBuffer in){
		in.position(0);
		byte[] printBuffer=new byte[in.remaining()];			

		in.get(printBuffer, 0, in.remaining());		
		return MessageTools.bytesToString(printBuffer);
	}
 
	//return false 表示继续等待接受数据 ，true表示退出 doDecode,给 下一个流程处理
	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		// TODO Auto-generated method stub
		
		
		try{
			
			int len=in.remaining();		
			
			//复杂的协议读取 ,组包
			//没有接收完成，继续等待
			if(len<packMinSize){
				return false;
			}		
	 
			int start = in.position();
			
			byte[] tempdata=new byte[3];
			in.get(tempdata,0,3);
	 		
			int datalen=MessageTools.getUnsignedByteValue(tempdata[2])*256+
					MessageTools.getUnsignedByteValue(tempdata[1]);
			
			//数据包长度错误
			if(datalen<8){
				 
				//SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");			
				//System.out.println(sdf.format(new Date())+" 命令长度不对! "+getAllInBuffer(in)); 
				
				System.out.println(" 命令长度不对! "+getAllInBuffer(in));
				session.closeOnFlush();
				return false;
	 
			}
			
			//ID不对
			if(tempdata[0]>9){
				SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");			
				System.out.println(sdf.format(new Date())+" 命令ID不对! "+getAllInBuffer(in));
	 
				session.closeOnFlush();
				return false;
			}
				
			
			//如果数据接收完全，
			if(len>=datalen+3){		
				
				byte[] array=null;
				
				if(datalen>8){
					//查看接下来的数据包长度
					
					if(datalen>=12){
						byte[] pkgTempdata=new byte[12];
						in.get(pkgTempdata,0,12);
						
						
						int pkgContentlen=MessageTools.getUnsignedByteValue(pkgTempdata[11])*256+
								MessageTools.getUnsignedByteValue(pkgTempdata[10]);
						
						if (pkgContentlen!=(datalen-12) ){
							SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");			
							System.out.println(sdf.format(new Date())+" 数据包长度 不对 ! "+getAllInBuffer(in));
							
							session.closeOnFlush();
							return false;
						}
						
						array=new byte[3+datalen];
						System.arraycopy(tempdata, 0, array, 0, 3);
						System.arraycopy(pkgTempdata, 0, array, 3, 12);
						in.get(array,15,datalen-12);
						
					}else{
						
						SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");			
						System.out.println(sdf.format(new Date())+" 数据包长度 不对  [9 ,11]! " +getAllInBuffer(in));
						
						session.closeOnFlush();
						return false;
					}
	 
				}else{
					array=new byte[3+datalen];
					System.arraycopy(tempdata, 0, array, 0, 3);
					in.get(array,3,datalen);
				}
				
	 
	 	
				//如果有多包，将位置指向下一包
				//if(3+datalen+1<len){				
				//	System.out.println("positoin:"+in.position());
				//	in.position(3+datalen);
				//}		
				out.write(array);
				return true;	
				
			}else{
				in.mark();
				in.position(start);
				return false;	
			}
			
		}catch(Exception e){
			session.closeOnFlush();
			return false;
		}
 
	}



}
