package com.gimis.ps.parser;

import com.gimis.util.MessageConstants;
import com.gimis.util.MessageTools;

//发送命令解析
public class SendCommandParse {

	public static short commandConvert(int commandID) {
		if (commandID == 100) {
			return MessageConstants.MESSAGE_PARAM_SET;
		} else if (commandID == 200) {
			return MessageConstants.MESSAGE_PARAM_SET;
		} else if (commandID == 300) {
			return MessageConstants.MESSAGE_CAN_SET;
		} else if (commandID == 400) {
			return MessageConstants.MESSAGE_REMOTE_UPLOAD;
		} else if (commandID == 500) {
			return MessageConstants.MESSAGE_LOCK;
		}else if (commandID == 501) {
			return MessageConstants.MESSAGE_LOCK;
		}else if (commandID == 600) {
			return MessageConstants.MESSAGE_PLC_SET;
		}
		
		
		return 0;
	}
	
	public static int commandType(short commandID) {
		
		//CAN消息体
		if (commandID == MessageConstants.MESSAGE_CAN_SET
				|| commandID == MessageConstants.MESSAGE_PLC_SET) {
			return 1;
		} else { //GPS消息体
			return 0;
		}  
	}	

	public static byte[] parse(int commandID, String commandContent) {

		try {
			if (commandID == 100) {
				String[] array = commandContent.split(":");
				int len = commandContent.length();
				int port = Integer.parseInt(array[1]);
				if (array.length == 2 && port > 0) {
					byte[] data = new byte[len + 2];
					data[0] = 0x06;
					data[1] = (byte) (len);
					for (int i = 0; i < len; i++) {
						data[i + 2] = (byte) commandContent.charAt(i);
					}
					return data;
				}

			} else if (commandID == 200) {
				/*
				 * int interval = Integer.parseInt(commandContent); byte[]
				 * data=new byte[6]; data[0]=4; data[1]=4; byte[] tempdata=
				 * MessageTools.int2Byte(interval);
				 * System.arraycopy(tempdata,0,data,2,4);
				 * 
				 * return data;
				 * 
				 */
				return null;
			} else if (commandID == 300) {
				int interval = Integer.parseInt(commandContent);
				byte[] data = new byte[9];
				data[0] = 1;
				data[1] = (byte) 0xFF;
				data[2] = (byte) 0xFF;
				data[3] = (byte) 0xFF;
				data[4] = (byte) 0xFF;

				byte[] tempdata = MessageTools.int2Byte(interval*10);
				
				data[5] = (byte) tempdata[3];
				data[6] = (byte) tempdata[2];
				data[7] = (byte) tempdata[1];
				data[8] = (byte) tempdata[0];
 
				return data;
			} else if (commandID == 400) {

				//GIMIS地址
				String ip = "61.155.236.77";
				String port = "9035";

				//天昊升级地址
				//String ip = "219.134.251.176";
				//String port = "7929";
					
				int len = ip.length() + port.length() + 11;
				byte[] data = new byte[len];
				data[0] = 1; // TCP;

				String[] array = commandContent.split("\\.");
				int v1 = Integer.parseInt(array[0]);
				int v2 = Integer.parseInt(array[1]);
				int v3 = Integer.parseInt(array[2]);
				int v4 = Integer.parseInt(array[3]);
				data[1] = (byte) v1;
				data[2] = (byte) v2;
				data[3] = (byte) v3;
				data[4] = (byte) v4;

				data[5] = (byte) ip.length();

				int pos = 5 + ip.length();
				for (int i = 0; i < ip.length(); i++) {
					data[i + 6] = (byte) ip.charAt(i);
				}

				data[pos + 1] = (byte) port.length();

				for (int i = 0; i < port.length(); i++) {
					data[i + pos + 2] = (byte) port.charAt(i);
				}

 
				return data;

			}else if (commandID == 500) {
				
				byte[] data = new byte[1];
				data[0]=1; 
				return data;
			}else if (commandID == 501) {
				byte[] data = new byte[1]; 
				data[0]=0;
				return data;
			}else if (commandID == 600) {
				String[] arrstr=commandContent.split("\\.");
				
				int a1=Integer.parseInt(arrstr[0]);
				int a2=Integer.parseInt(arrstr[1]);
				int a3=Integer.parseInt(arrstr[2]);

				byte[] data = new byte[7];
				data[0] = (byte)a1;				
				
				byte[] buf1=MessageTools.int2Byte(a2);
				data[1]=buf1[3];
				data[2]=buf1[2];
 
				
				byte[] buf2=MessageTools.int2Byte(a3);
				data[3]=buf2[3];
				data[4]=buf2[2];
				data[5]=buf2[1];
				data[6]=buf2[0];
 
				return data;
			}

			return null;
		} catch (Exception e) {
			return null;
		}

	}

}
