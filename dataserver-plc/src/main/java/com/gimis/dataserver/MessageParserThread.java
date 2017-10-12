package com.gimis.dataserver;
 
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;


import com.gimis.dataserver.command.RepeatSendCommandList;
import com.gimis.dataserver.terminal.GPSDevice;
import com.gimis.dataserver.terminal.GPSDeviceManager;
import com.gimis.util.GlobalCache;
import com.gimis.util.PlcBody;
import com.gimis.util.PlcContent;
import com.gimis.util.SourceMessage;

public class MessageParserThread extends Thread{
	
	 	
	//private final String GpsVarId="759";
	private final String GpsVarId="65530";
	 
	private SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
	
	private SimpleDateFormat milsdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS");
	
	private GPSDeviceManager gpsDeviceManager;
	
	private RepeatSendCommandList sendCommandList;
	
	private Hashtable<String,Long> deviceFlow=new Hashtable<String,Long>();
	
	//最后一次打印流量的时间
	private long lastPrintFlowTime=0;
	
	public MessageParserThread(GPSDeviceManager gpsDeviceManager,RepeatSendCommandList sendCommandList){
		this.gpsDeviceManager=gpsDeviceManager;	
		this.sendCommandList=sendCommandList;
	}
	
	private void printFlow(){
		long diff=System.currentTimeMillis()-lastPrintFlowTime;
		if(diff>300000){
			lastPrintFlowTime=System.currentTimeMillis();
			
			StringBuilder sb=new StringBuilder();
 
			sb.append(sdf.format(new Date()));
        	sb.append(" device flows ->");
        	
        	for(java.util.Map.Entry<String, Long> entry:deviceFlow.entrySet())
        	{
        		sb.append(" ").append(entry.getKey()+":"+entry.getValue());
        	}  
        	
        	System.out.println(sb.toString());
        	 
		}		
	}
	
	public void run(){
		int count=0;
		while(true){ 			
			try{				
				SourceMessage sourceMessage=GlobalCache.getInstance().getSourceMessageQueue().poll();					
				if(sourceMessage!=null){	

					PlcBody plcBody=parser(sourceMessage.getData());	
					
					Long flow=deviceFlow.get(plcBody.getPlcId());
					if(flow==null){
						deviceFlow.put(plcBody.getPlcId(), new Long(0));
					}					
					flow=flow+sourceMessage.getData().length;
					deviceFlow.put(plcBody.getPlcId(), flow);
		
					printFlow();

					GPSDevice gpsDevice=gpsDeviceManager.getGPSDevice(plcBody.getPlcId());
					try{
										 
					if(gpsDevice!=null && gpsDevice.getDevice_unid()!=null){
						
						//将地址放入GPS地址对应表中
					    GlobalCache.getInstance().getGpsIpAddressMap().put(plcBody.getPlcId(), 
					    		sourceMessage.getAddress());
 
						//主动上传数据
					    if(plcBody.getCmdNo()==0x85
					    || plcBody.getCmdNo()==0x86
					    || plcBody.getCmdNo()==0x87
					    || plcBody.getCmdNo()==0x88
					    || plcBody.getCmdNo()==0x89
					    || plcBody.getCmdNo()==0x8A){					    						    	
					    	   sendCommandList.deleteData(plcBody.getPlcId(), plcBody.getSeqNo());
					    }
					    else if(plcBody.getCmdNo()==0x04){
							
							HashMap<String,HashMap<String,String>> dataList=new HashMap<String,HashMap<String,String>>();

							ArrayList<PlcContent> list=new ArrayList<PlcContent>();
																				
							HashMap<String, String> hashMap = new HashMap<String, String>();							 
							hashMap.put("gps_id", gpsDevice.getGpsID());
							hashMap.put("device_unid", gpsDevice.getDevice_unid());
							hashMap.put("server_time", sdf.format(new Date()));
							
							//17,0,1493510370312
							String content=new String(plcBody.getData(), "ISO8859-1");
							
							String analysisStr=plcBody.getPlcId()+"&&"+content;
							GlobalCache.getInstance().getAnalysisQueue().put(analysisStr);
 
							String[] array=content.split("\r\n");
							
							try{
								
								for(int i=0;i<array.length;i++){
									String[] array1=array[i].split(",");	
									
									if(array1!=null && array1.length>=3){
										
										long time = Long.parseLong(array1[2]);
										Date date = new Date(time);
										
										String key=gpsDevice.getDevice_unid()+"-"+array1[2];
										
										
										HashMap<String,String> map =dataList.get(key);
										if(map==null){
											map=new HashMap<String,String>();
											map.put("gps_time", milsdf.format(date));																
											map.put("server_time",milsdf.format(new Date()) );
											dataList.put(key, map);
										}								
										map.put(array1[0], array1[1]);							
										
										hashMap.put(array1[0], array1[1]);
										if(i==0){
											hashMap.put("gps_time", sdf.format(date));
										}
																		
										if(array1[0].equals(GpsVarId)){									
											PlcContent plcContent=new PlcContent();
											plcContent.setDeviceUnid(gpsDevice.getDevice_unid());
											plcContent.setPlcId(gpsDevice.getGpsID());
											plcContent.setPlcVarId(array1[0]);
											plcContent.setPlcVarValue(array1[1]);		 
											plcContent.setPlcVarTime(date);									
											list.add(plcContent);
											GlobalCache.getInstance().getGpsDataQueue().put(plcContent);
										}
										
									}else{
										System.out.println(sdf.format(new Date())+"******####### "+
												plcBody.getPlcId()+" error code !!!!!! "+
												array[i] +"  content: "+content);

									}
								}
								
								
								//更新最新的数据到redis								
								if(array!=null && array.length>0){
									GlobalCache.getInstance().getPlcRedisQueue().put(hashMap);									
									GlobalCache.getInstance().getPlcDataQueue().add(dataList);
								}
								
							}catch(Exception e){
							//	logger.error("22 GpsID:"+plcBody.getPlcId()+" "+ e.toString()+" msg:  "+content);
							}
								

						}

					} 
					
					}catch(Exception e){	
						//logger.error("11 GpsID:"+plcBody.getPlcId()+" "+ e.toString());
					}
				}
					
				
			}catch(Exception e){
				//e.printStackTrace();
			//	logger.error(e.toString());			
			}
			
			//定期休眠
			if(count>100){
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
				count=0;
			}else{
				count++;
			}
			
			
		}
		
	}
	
 
	
	
	private PlcBody parser(byte[] data){
		
		try{
			PlcBody plcBody=new PlcBody();
			plcBody.setCmdNo(data[0]);
			
			int len=(data[2] & 0xFF)*256+(data[1] & 0xFF)+3;
			plcBody.setSize(len);
			
			int seqNo=(data[4] & 0xFF)*256+(data[3] & 0xFF);
			
			plcBody.setSeqNo(seqNo);
			
			String id=bytesToAscii(data,5,6);
			plcBody.setPlcId(id);
			
			
			if(len>=15){
				int dataType=(data[12] & 0xFF)*256+(data[11] & 0xFF);					
				plcBody.setDataType(dataType);
				
				int dataLen=(data[14] & 0xFF)*256+(data[13] & 0xFF);
	 
				if(dataLen>0){
					byte[] sourceData=new byte[dataLen];
					System.arraycopy(data, 15, sourceData, 0, dataLen);
					plcBody.setData(sourceData);
				}				
			}
			
 
			return plcBody;			
		}catch(Exception e){			
			System.out.println(sdf.format(new Date())+ " protocol parser error !");
			return null;
		}

	}
	
	public static String bytesToAscii(byte[] bytes, int offset, int dateLen) {  
        if ((bytes == null) || (bytes.length == 0) || (offset < 0) || (dateLen <= 0)) {  
            return null;  
        }  
        if ((offset >= bytes.length) || (bytes.length - offset < dateLen)) {  
            return null;  
        }  
  
        String asciiStr = null;  
        byte[] data = new byte[dateLen];  
        System.arraycopy(bytes, offset, data, 0, dateLen);  
        try {  
            asciiStr = new String(data, "ISO8859-1");  
        } catch (Exception e) {  
        }  
        return asciiStr;  
    } 

}
