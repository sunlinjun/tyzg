package com.gimis.dataserver.gps;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.gimis.HBase.HBaseUtil;
import com.gimis.ps.msg.GPSBody;
import com.gimis.util.GlobalCache;
import com.gimis.util.MessageConstants;
import com.google.gson.Gson;
 
/*
 * 保存GPS位置线程 
 * 保存到 mysql数据库，保存到redis 
 */
public class SavePositionThread extends Thread{
 
	private DecimalFormat floatFormat = new DecimalFormat("#.000000"); 
	
	private SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
	//HBase 
	private HBaseUtil hBaseUtil=new HBaseUtil();

	public SavePositionThread(){
 
 
	}
	
 
	
	public void run() {	
		
		hBaseUtil.createTable(MessageConstants.TABLE_GPS_NAME) ;
		
		//System.out.println("query HBase begin ...");
		//TestQuery();
		//System.out.println("query HBase end ...");
		
		while(true){
			try {
				GPSBody gpsBody=GlobalCache.getInstance().getGpsBodyQueue().take();
				

				//保存到HBase
				saveToHBase(gpsBody);
 
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}
	
	private void saveToHBase(GPSBody gpsBody){
		hBaseUtil.write(MessageConstants.TABLE_GPS_NAME, gpsBody.getDevice_unid(), 
				gpsBody.getGpsTime().getTime(), gpsBody.ToMap(floatFormat, dateformat));
	}
	
	public void TestQuery(){
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
		String dt1="2016-11-10 00:00:27";
		String dt2="2016-11-11 23:50:00";
		
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
		columnsList.add("gps_id");
		columnsList.add("uploadType");
		
		columnsList.add("longitude");
		columnsList.add("latitude");
		columnsList.add("distance");
		columnsList.add("direction");
		columnsList.add("height");
		columnsList.add("softVersion");
		
		columnsList.add("softVersion");
		columnsList.add("hardwareVersion");
		columnsList.add("gpsTime");
		columnsList.add("serverTime");
		
		columnsList.add("accStatus");
		columnsList.add("locationStatus");
		columnsList.add("gpsModelStatus");
		columnsList.add("serverTime");		
		
		columnsList.add("powerBatteryStatus");	
		columnsList.add("satelliteCount");
		
		LinkedList<HashMap<String, String>> list=hBaseUtil.read(MessageConstants.TABLE_GPS_NAME,
			//	"AB63F1A637D34193916F2FCBCC97715F",
				"B0BD9C987AA54C009FA453A9BB232A61",
				columnsList, dateFrom, dateTo);
		
		Gson gson = new Gson();
		String jsonList=gson.toJson(list);
		
		System.out.println(jsonList);		
	}
	
	 
	
	 
	 
	
	 
 
	
 
}

