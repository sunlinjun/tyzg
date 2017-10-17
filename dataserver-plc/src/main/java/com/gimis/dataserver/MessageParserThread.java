package com.gimis.dataserver;
 
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

import com.gimis.dataserver.command.RepeatSendCommandList;
import com.gimis.dataserver.plc.OffsetResolution;
import com.gimis.dataserver.terminal.GPSDevice;
import com.gimis.dataserver.terminal.GPSDeviceManager;
import com.gimis.util.DBConnection;
import com.gimis.util.GlobalCache;
import com.gimis.util.PlcBody;
import com.gimis.util.PlcContent;
import com.gimis.util.SourceMessage;
import com.mysql.jdbc.Connection;

public class MessageParserThread extends Thread{

	//private final String GpsVarId="759";
	private final String GpsVarId="65530";
	 
	private SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
	
	private SimpleDateFormat milsdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS");
	
	private DecimalFormat df=new DecimalFormat("0.000");
	
	private GPSDeviceManager gpsDeviceManager;
	
	private RepeatSendCommandList sendCommandList;
	
	private Hashtable<String,Long> deviceFlow=new Hashtable<String,Long>();
	

	//设备与数据字典对应关系   Key 设备 GPS_ID  value  数据字典UNID
	private Hashtable<String,String> deviceFiber=new Hashtable<String,String>();
	
	//数据字典中的偏移量与分辨率  Key   数据字典UNID+_+var_id
	private Hashtable<String,OffsetResolution> deviceOffsetResolution =new Hashtable<String,OffsetResolution>();
	
	//最后一次获取偏移量的时间
	private long lastGetPlcFiberTime=0;
 
	
	//最后一次打印流量的时间
	private long lastPrintFlowTime=0;
	
	public MessageParserThread(GPSDeviceManager gpsDeviceManager,RepeatSendCommandList sendCommandList){
		this.gpsDeviceManager=gpsDeviceManager;	
		this.sendCommandList=sendCommandList;
	}
 
	private void refrushPlcFiberDB(){
		long diff=System.currentTimeMillis()-lastGetPlcFiberTime;
		if(diff>3600000){
			lastGetPlcFiberTime=System.currentTimeMillis();

			StringBuilder sb=new StringBuilder();
 
			
			Statement st = null;
			Connection connection=null;
			ResultSet rs=null;
			try {
				
				connection=DBConnection.getConnection();
				
				//获取设备对应的数据字典		 	
				st = connection.createStatement();					
				sb.append("select GPS_ID,FIBER_UNID from device where FLAG_DEL=0 ");
				rs = st.executeQuery(sb.toString());
				
				deviceFiber.clear();
				while (rs.next() != false) {		
					try{
						if(rs.getString("GPS_ID")!=null && rs.getString("FIBER_UNID")!=null){
							deviceFiber.put(rs.getString("GPS_ID"), rs.getString("FIBER_UNID"));
						}											
					}catch(Exception e){
						
					}
				}
				
				rs.close();				
				st.close();				
				st = connection.createStatement();
				sb.setLength(0);
				sb.append("select FIBER_UNID,VAR_ID,OFFSET,RESOLUTION from fiber_plc ");
				rs = st.executeQuery(sb.toString());
				deviceOffsetResolution.clear();
				while (rs.next() != false) {
					
					try{

						String key=rs.getString("FIBER_UNID")+"_"+rs.getString("VAR_ID");
						
						OffsetResolution offset=new OffsetResolution();
						if(rs.getString("OFFSET")!=null){
							offset.setOffset(Integer.parseInt(rs.getString("OFFSET")));
						}
						
						if(rs.getString("RESOLUTION")!=null){
							offset.setResolution(Double.parseDouble(rs.getString("RESOLUTION")));
						}
						
						if(offset.getOffset()!=0 ||
								offset.getResolution()>1.001 || offset.getResolution()<0.999){
							deviceOffsetResolution.put(key, offset);	
						}
					}catch(Exception e){
						
					}												
				}
				
			} catch (Exception e) {		
				e.printStackTrace();
			}finally{
				
				try{
					rs.close();
				}catch(Exception e){
					
				}			
				try {
					st.close();					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
				
				try {			
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
		}
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
	
	//计算偏移量和分辨率后的新值
	private void updateOffsetResolution(String gpsId,HashMap<String,String> hashMap,
			HashMap<String,String> hashMap2,
			String key,String value){
		
		String fiberUnid= deviceFiber.get(gpsId);
		if(fiberUnid!=null){			
			String fKey=fiberUnid+"_"+key;
			OffsetResolution offsetResolution= deviceOffsetResolution.get(fKey);
			if(offsetResolution!=null){	
				
				if(offsetResolution.getOffset()!=0 || 
						offsetResolution.getResolution()>1.001 ||
						offsetResolution.getResolution()<0.999){
					try{
						double dTemp=Double.parseDouble(value);	
						dTemp=(dTemp-offsetResolution.getOffset())*offsetResolution.getResolution();					
						hashMap.put(key, df.format(dTemp));
						hashMap2.put(key, df.format(dTemp));
					}catch(Exception e){					
					}						
				}											
			} 
		} 						
	}
	
	public void run(){
		
		
		
		int count=0;
		while(true){ 			
			try{				
				SourceMessage sourceMessage=GlobalCache.getInstance().getSourceMessageQueue().poll();					
				if(sourceMessage!=null){	
					
					
					refrushPlcFiberDB();

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
										
										
										if(i==0){
											hashMap.put("gps_time", sdf.format(date));
										}
																		
										if(array1[0].equals(GpsVarId)){		
											
											hashMap.put(array1[0], array1[1]);
											
											PlcContent plcContent=new PlcContent();
											plcContent.setDeviceUnid(gpsDevice.getDevice_unid());
											plcContent.setPlcId(gpsDevice.getGpsID());
											plcContent.setPlcVarId(array1[0]);
											plcContent.setPlcVarValue(array1[1]);		 
											plcContent.setPlcVarTime(date);									
											list.add(plcContent);
											GlobalCache.getInstance().getGpsDataQueue().put(plcContent);
										}else{										
											updateOffsetResolution(plcBody.getPlcId(),hashMap,map,
													array1[0], array1[1]); 
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
