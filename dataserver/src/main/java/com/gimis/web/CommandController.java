package com.gimis.web;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gimis.ps.msg.DefaultHeader;
import com.gimis.ps.msg.DefaultMessage;
import com.gimis.ps.parser.SendCommandParse;
import com.gimis.util.GlobalCache;
import com.gimis.util.SourceMessage;
import com.google.gson.Gson;

 /*
  * 接受外部发送命令请求，向终端发送命令,并且返回命令应答给外部
  */
@RestController
public class CommandController {
	
	@Autowired
	private String appKey;
	
	//发送命令，获取应答
	private  int sendCommand(String gpsId, String address,short commandId, byte[] sendData) {
		try {
			DefaultHeader defaultHeader = new DefaultHeader();
			defaultHeader.setGpsId(gpsId);
			
			//自增序列号
			GlobalCache.seqNumber++;
			if(GlobalCache.seqNumber>65535){
				GlobalCache.seqNumber=1;
			}
			short seqNo=GlobalCache.seqNumber;
			defaultHeader.setSequenceId((short) seqNo);

			DefaultMessage sendMessage = new DefaultMessage();

			//GPS消息体
			if (SendCommandParse.commandType(commandId)==0){
				sendMessage.setSendGPSData(sendData);		
				defaultHeader.setGpsCommandId(commandId);
				
			}else{  //CAN消息体				
				sendMessage.setSendCANData(sendData);	
				defaultHeader.setAttachmentId(commandId);
			}
			sendMessage.setHeader(defaultHeader);			 	
			// 发送命令
			SourceMessage sourceMessage = new SourceMessage("1", address, sendMessage.getData());
			GlobalCache.getInstance().getCmdQueue().put(sourceMessage);
			
			//获取命令应答的序列号;
			//12秒接收
			for(int i=0;i<4;i++){
				Short value=GlobalCache.getInstance().getResponseSEQ().get(gpsId);
			    if(value!=null){ 
			    	if( value== seqNo){
			    		return 0; 
			    	}
			    }
				Thread.sleep(3000);
			}			
			return 1;
			
		} catch (Exception e) {
		}
		return 2;

	}
	
	private String commandResponse(String result,String message){
		Map<String, String> resultMap = new Hashtable<String, String>();
		resultMap.put("result", result);
		resultMap.put("message", message);

		Gson gson = new Gson();
		return gson.toJson(resultMap);
	}

	/*
	 * 接受外部发送命令请求，向终端发送命令,并且返回命令应答给外部
	 * myAppKey  授权KEY
	 * unid  设备UNID
	 * commandid 命令ID   
	 * content 命令内容
	 * http://127.0.0.1:9019/sendCommand/40f70f06-cdd5-4d86-82fb-b347f6c6f4fa/F0CD8B2291A549909692F02AF7B801D5/100/news.sina.com%3A19000
	 */
	@RequestMapping("/sendCommand/{myAppKey}/{unid}/{commandid}/{content}")
	public String index(@PathVariable String myAppKey, @PathVariable String unid, @PathVariable int commandid,
			@PathVariable String content) throws UnsupportedEncodingException {
	
		if (myAppKey.equals(appKey)==false) {
			return commandResponse("100","key不正确");
		}
		
		
		Map<String, String> hashMap = GlobalCache.getInstance().getDeviceUnidMap();
		String gpsId = hashMap.get(unid);
		if (gpsId == null) {
			return commandResponse("100","设备未注册或不在线");
		}
		
 
		String address = GlobalCache.getInstance().getGpsIpAddressMap().get(gpsId);
		Date dt = GlobalCache.getInstance().getGpsIpAddressTimeMap().get(gpsId);
		if(address==null || dt==null){
			return commandResponse("101","设备不在线");
		}
		

		long diff = (System.currentTimeMillis() - dt.getTime()) / 1000;
		if (diff >= 300) {
			return commandResponse("102","设备不在线");
		}
		
		//通过URL传过来时,   将 ,  转化成  .
		content=content.replace(',', '.');
		// 解析命令
		byte[] sendData = SendCommandParse.parse(commandid, content);
		if(sendData==null){
			return commandResponse("103","命令内容有错");
		}
		
		int ret=sendCommand(gpsId,address,SendCommandParse.commandConvert(commandid), sendData);

		if (ret==1){
			return commandResponse("105","命令未响应");
			 
		}else if (ret==2){
			return commandResponse("106","发送命令出错");
		}
 
		return commandResponse("0","命令发送成功");
		
 
	}

}
