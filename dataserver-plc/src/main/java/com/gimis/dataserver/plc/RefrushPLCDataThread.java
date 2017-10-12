package com.gimis.dataserver.plc;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gimis.dataserver.redis.RedisUtil;
import com.gimis.util.DBConnection;
import com.gimis.util.GlobalCache;
import com.google.gson.Gson;
import com.mysql.jdbc.Connection;

import redis.clients.jedis.JedisPool;

/*
 * 更新PLC数据线程
 */
public class RefrushPLCDataThread extends Thread{
	
	private static final Logger logger = LoggerFactory.getLogger(RefrushPLCDataThread.class);
	
	private RedisUtil redisUtil;
	
	//建立一个Mysql连接
	private Connection connection=null;
	
	//存放保存数据库的时间
	private Map<String, Long> saveDBList = new Hashtable<String, Long>();
	
	public RefrushPLCDataThread(){
		JedisPool jedisPool= GlobalCache.getInstance().getRedisPoolFactory().getJedisPool();
		redisUtil = new RedisUtil(jedisPool);
	}
	
	private void initDBConnection(){
		
		if(connection==null){
			createConnection();	
		} else{
			try {
				if(connection.isClosed()){
					createConnection();	
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
	
	private void createConnection(){
		try {
			connection=DBConnection.getConnection();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			logger.error("连接mysql出错!"+e1.toString());
		}
	}	
	
	public void run(){
		
		while(true){
			try{
			
				HashMap<String, String> hashMap = GlobalCache.getInstance().getPlcRedisQueue().take();				
				if(hashMap!=null){
					String gpsId=hashMap.get("gps_id");
					
			        String deviceUnid=hashMap.get("device_unid");
			        String serverTime=hashMap.get("server_time");
			        String gpsTime=hashMap.get("gps_time");
					
					if(gpsId!=null && deviceUnid!=null){
						//保存到redis
						redisUtil.add("CAN", gpsId, hashMap);	
																	
						HashMap<String, String> map=new HashMap<String,String>();
						map.put("serverTime",serverTime);
						redisUtil.add("GPSPosition", deviceUnid, map);
 						
						//保存到MySQL 
						refrushDB(gpsId,deviceUnid,serverTime,gpsTime,hashMap);
					}
				}				
			}catch(Exception e){
				
			}
								
		}
		
	}
	
	private void refrushDB(String gpsId,String deviceUnid,String serverTime,String gpsTime,
			HashMap<String, String> plcData){
		
		String key=gpsId+"-"+deviceUnid;
		Long lastTime=saveDBList.get(key);
		if(lastTime!=null){
			long diff=(System.currentTimeMillis()-lastTime)/1000;
			if(diff>=1800){
				saveDBList.put(key, System.currentTimeMillis());
				saveToDB(gpsId,deviceUnid,serverTime,gpsTime,plcData);
			}

		}else{
			saveDBList.put(key, System.currentTimeMillis());
			saveToDB(gpsId,deviceUnid,serverTime,gpsTime,plcData);
		}
	}
	
	private void saveToDB(String gpsId,String deviceUnid,String serverTime,String gpsTime,
			HashMap<String, String> plcData){
		initDBConnection();
		
		
		Gson gson=new Gson();
		String strCanData=gson.toJson(plcData);
		
		Statement st = null;
		StringBuilder sb=new StringBuilder();
		try {
			st = connection.createStatement();		
			
			sb.append("insert into gpscan set ");	
			sb.append(getSubSQL(gpsId, deviceUnid, serverTime, gpsTime));
			sb.append(",").append("candata='").append(strCanData).append("'");

			sb.append(" on duplicate key update ");
			sb.append(getSubSQL(gpsId, deviceUnid, serverTime, gpsTime));
			sb.append(",").append("candata='").append(strCanData).append("'");

			st.execute(sb.toString());		
 
		} catch (Exception e) {
			logger.error(e.toString()+" "+sb.toString());
			try {
				st.close();
				connection.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}				
			connection=null;
		}finally{
			try {
				st.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}
	
	private String getSubSQL(String gpsId,String deviceUnid,String serverTime,String gpsTime){
		StringBuilder sb=new StringBuilder();
		sb.append(" gps_id='").append(gpsId).append("'");		
		sb.append(",").append("device_unid='").append(deviceUnid).append("'");;	
		sb.append(",").append("gpstime='").append(gpsTime).append("'");
		sb.append(",").append("servertime='").append(serverTime).append("'");
		
		return sb.toString();		
	}

}
