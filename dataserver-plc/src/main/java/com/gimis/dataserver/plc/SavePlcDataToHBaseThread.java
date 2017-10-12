package com.gimis.dataserver.plc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
import com.gimis.dataserver.hbase.HBaseData;
import com.gimis.dataserver.hbase.HBaseUtil;
import com.gimis.dataserver.hbase.RowKey;
import com.gimis.util.GlobalCache;
import com.google.gson.Gson;

/*
 * 将PLC数据保存到HBase中 
 */
public class SavePlcDataToHBaseThread extends Thread{
	
	private static final Logger logger = LoggerFactory.getLogger(SavePlcDataToHBaseThread.class);
 	
	private String  hbaseTable;
	
	//当前year
	private int currentYear;
	
	//HBase   
	private HBaseUtil hBaseUtil=new HBaseUtil();
 
	
	public SavePlcDataToHBaseThread(){
	}
		
	private void createHBaseTable(){
		Calendar a=Calendar.getInstance();
		int year=a.get(Calendar.YEAR); 		 		
		if(currentYear!=year){			
			hbaseTable="tzPlc-"+Integer.toString(year);
			hBaseUtil.createTable(hbaseTable);
			currentYear=year;
		}				
	}
		
	public void run(){
		int count=0;
 
		//System.out.println("query HBase begin ...");
		//TestQuery();
		//System.out.println("query HBase end ...");
		
		while(true){
			try{
				//创建HBase表
				createHBaseTable();
 
				HashMap<String,HashMap<String,String>> list=GlobalCache.getInstance().getPlcDataQueue().poll();				
				if(list!=null){
					ArrayList<HBaseData> hbaseList=new ArrayList<HBaseData>(); 
					Iterator<Entry<String, HashMap<String, String>>> iter =list.entrySet().iterator();
										
					String DT=null;	
					
					while(iter.hasNext()){
						Entry<String, HashMap<String, String>> entry = (Entry<String, HashMap<String, String>>) iter.next();
	 						
						String key = entry.getKey();
						HashMap<String,String> hashMap=(HashMap<String,String>)entry.getValue();
						
						if(DT==null){
							DT="server_time:"+hashMap.get("server_time")+" gps_time:"+hashMap.get("gps_time");													 
						}
												
					    String[] array=key.split("-");
						if(array!=null &&  array.length==2){
							
							Long timestamp=Long.parseLong(array[1]);
							
							byte[] rowKey=RowKey.makeRowKey(array[0], timestamp);
 
							HBaseData hBaseData=new HBaseData();								 
							hBaseData.setRowKey(rowKey);
							hBaseData.setTimestamp(timestamp);						
							hBaseData.setRecord(hashMap);																			
							hbaseList.add(hBaseData);	
						}				
					}
 
					if(hbaseList.size()>0){
						
						//long diff=System.currentTimeMillis();
						
						hBaseUtil.writeList(hbaseTable, hbaseList);
						
						//diff=System.currentTimeMillis()-diff;
						//System.out.println("[save HBase "+hbaseTable+" ] "
						//    +DT+" count:"+hbaseList.size()+" times:"+diff);
					}
					

									  						
				}							
			}catch(Exception e){
				e.printStackTrace();
				logger.error(e.toString());				
			}
 
			//定期休眠
			if(count>10){
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
	
	
	public void TestQuery(){
		
		
		String deviceUnid="84C680C3864C4AAAB68106EC6B33548C";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
		String dt1="2017-05-01 00:00:27";
		String dt2="2017-05-21 23:50:00";
		
		long dateFrom=0;
		try {
			dateFrom = sdf.parse(dt1).getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long dateTo=0;
		try {
			dateTo = sdf.parse(dt2).getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
					
		ArrayList<String> columnsList=new ArrayList<String>();
		 
		columnsList.add("1");
		columnsList.add("2");
		columnsList.add("3");
		
		//columnsList.add("server_time");		
		columnsList.add("gps_time");
		LinkedList<HashMap<String, String>> list=hBaseUtil.read("tzPlc-2017",
				deviceUnid, columnsList, dateFrom, dateTo);
		
		 		
		Gson gson = new Gson();
		String jsonList=gson.toJson(list);
		
		System.out.println(jsonList);		
	}

}
