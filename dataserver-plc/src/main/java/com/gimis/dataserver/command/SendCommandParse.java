package com.gimis.dataserver.command;

import com.gimis.util.MessageTools;

//发送命令解析
public class SendCommandParse {
 
	private static byte[] decode(String ID,byte commandID,int seqNo,byte[] content){		
 		
		byte[] data=null;
		if(content==null){
			data=new byte[11];
			data[1]=(byte)0x08;
			data[2]=(byte)0x00;			
		}else{		
			data=new byte[11+content.length];
	    	data[1] = (byte) (content.length+8);
	    	data[2] = (byte)((content.length+8) >> 8);	
		}		
		data[0]=(byte)commandID;
    	data[3] = (byte)seqNo;
    	data[4] = (byte)(seqNo >> 8);			
		
    	for(int i=0;i<6;i++){
    		data[5+i]=(byte)ID.charAt(i);    		
    	}  
    	
    	if(content!=null){
    		System.arraycopy(content, 0, data, 11, content.length);
    	}
    	
		return data;		
	}
 
 
	public static byte[] parse(String ID,int commandID, int seqNo,String commandContent) {

		try {
			
			//远程升级
			if (commandID == 400) {
				int len=commandContent.length();
				byte[] content=new byte[len+4];
				
				content[0]=2;
				content[1]=0;
				content[2]=(byte)(len % 0x100);
				content[3]=(byte)(len / 0x100);
								
				for(int i=0;i<commandContent.length();i++){
					content[4+i]=(byte)commandContent.charAt(i);
				}
				return decode(ID,(byte)0x09,seqNo,content); 
			}
			
			//数据上传间隔
			else if (commandID == 300 || commandID == 602) {
				int interval = Integer.parseInt(commandContent);
				
				byte[] data=new byte[8];
				
				data[0]=1;
				data[1]=0;
				data[2]=(byte)0x04;
				data[3]=(byte)0x00;
				
				
				byte[] tempdata = MessageTools.int2Byte(interval);				
				data[4] = (byte) tempdata[3];
				data[5] = (byte) tempdata[2];
				data[6] = (byte) tempdata[1];
				data[7] = (byte) tempdata[0];
				
				return decode(ID,(byte)0x07,seqNo,data); 
			}
			
			//变量属性设置 
			else if (commandID == 603) {
				
				byte[] buffer= commandContent.getBytes("UTF-8");				
				int len=buffer.length;
				byte[] content=new byte[len+4];
				
				content[0]=2;
				content[1]=0;
				content[2]=(byte)(len % 0x100);
				content[3]=(byte)(len / 0x100);
				
				System.arraycopy(buffer, 0, content, 4, len);
				
				return decode(ID,(byte)0x08,seqNo,content); 
			}

			//PLC设置
			else if (commandID == 600) {
				
				int len=commandContent.length();
				byte[] content=new byte[len+4];
				
				content[0]=2;
				content[1]=0;
				content[2]=(byte)(len % 0x100);
				content[3]=(byte)(len / 0x100);
								
				for(int i=0;i<commandContent.length();i++){
					content[4+i]=(byte)commandContent.charAt(i);
				}
				return decode(ID,(byte)0x06,seqNo,content); 
			}
			
			//设备复位
			else if (commandID == 601) {
				return decode(ID,(byte)0x05,seqNo,null); 
			}
			
			//设置地址
			else if (commandID == 100) {
				
				int len=commandContent.length();
				byte[] content=new byte[len+4];
				
				content[0]=2;
				content[1]=0;
				content[2]=(byte)(len % 0x100);
				content[3]=(byte)(len / 0x100);
								
				for(int i=0;i<commandContent.length();i++){
					content[4+i]=(byte)commandContent.charAt(i);
				}
				
				return decode(ID,(byte)0x0A,seqNo,content); 
 
			}
			
			else{
				return null;
			}
			
 

			
		} catch (Exception e) {
			return null;
		}

	}
	
	/*
	public static void main(String[] args) {
		
		SendCommandParse commandParse=new SendCommandParse();
		
		//byte[] data=commandParse.parse("C00001", 400, 257, "hello slj");		
		//String msg=MessageTools.bytesToHexString(data);

		//byte[] data=commandParse.parse("C00001", 300, 1, "20");		
		//String msg=MessageTools.bytesToHexString(data);

		//byte[] data=commandParse.parse("C00001", 603, 1, "1,INT16,发动机转矩,DB1.012,0,30000\r\n2,INT16,发动机转矩2,DB1.016,0,30000\r\n");		
		//String msg=MessageTools.bytesToHexString(data);

		//byte[] data=commandParse.parse("C00001", 600, 1, "1,88\r\n5,0\r\n");		
		//String msg=MessageTools.bytesToHexString(data);
		
		
		//byte[] data=commandParse.parse("C00001", 601, 1, null);		
		//String msg=MessageTools.bytesToHexString(data);
		
		byte[] data=commandParse.parse("C00001", 100, 1, "tz-ecloud.com:19000");		
		String msg=MessageTools.bytesToHexString(data);
		
		System.out.println(msg);
		
		
	}
 */
	
}
