package com.gimis.dataserver.gps;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gimis.dataserver.hbase.HBaseUtil;
import com.gimis.dataserver.redis.RedisUtil;
import com.gimis.util.DBConnection;
import com.gimis.util.GlobalCache;
import com.gimis.util.PlcContent;
import com.gimis.util.PropertiesTools;
import com.google.gson.Gson;
import com.mysql.jdbc.Connection;

import BaiduMap.AddressComponent;
import BaiduMap.BaiduMap;
import redis.clients.jedis.JedisPool;


/*
 * 解析和保存GPS数据队列
 */
public class SavePositionThread extends Thread{
	
    //GPS表名
    public static final String TABLE_GPS_NAME="tzh-gps";
	
	private static final Logger logger = LoggerFactory.getLogger(SavePositionThread.class);

	private DecimalFormat floatFormat = new DecimalFormat("#.000000"); 
	
	private SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
	//redis工具
	private RedisUtil redisUtil;
	
	//BaidDu地图的KEY
	private String baiduMapKey;
	
 
	//存放保存数据库的时间
	private Map<String, Long> saveDBList = new Hashtable<String, Long>();
	
	//存放上次保存中文位置的时间,经纬度
	private Map<String, GPSPositionChs> lnglatList = new Hashtable<String, GPSPositionChs>();
		
	//建立一个Mysql连接
	private Connection connection=null;
	
	//HBase 
	private HBaseUtil hBaseUtil=new HBaseUtil();

	public SavePositionThread(){
    	JedisPool jedisPool= GlobalCache.getInstance().getRedisPoolFactory().getJedisPool();
    	redisUtil = new RedisUtil(jedisPool);
    	
		Properties cfgPro = new Properties();
		try {
			cfgPro = PropertiesTools.loadProperties("cfg.properties", System.getProperty("user.dir"));
		
			baiduMapKey = cfgPro.getProperty("baiduMap.key");	
		
		} catch (IOException e) {
			e.printStackTrace();
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
	
	private GPSBody parserGPSBody(PlcContent plcContent){
		
		try{
			// 1;112.3327;E;37.4820;N;778.3;30.20;90.28
			String[] array=plcContent.getPlcVarValue().split(";");		
			if(array!=null && array.length==8){
				
				Date dt=new Date();
			
				GPSBody gpsBody=new GPSBody();
				
				gpsBody.setGps_id(plcContent.getPlcId());
				gpsBody.setAccStatus((byte)1);
				gpsBody.setDevice_unid(plcContent.getDeviceUnid());
	
			    
				short direction=(short)Double.parseDouble(array[7]);				
				gpsBody.setDirection(direction);
				
				gpsBody.setDistance((int)0);
				gpsBody.setGpsModelStatus((byte)0);
				
				gpsBody.setGpsTime(dt);
				gpsBody.setHardwareVersion((short)0);
				
				short height=(short)Double.parseDouble(array[5]);	
				gpsBody.setHeight(height);
				
				gpsBody.setIsCAN((short)1);
 
				gpsBody.setLocationStatus(Byte.parseByte(array[0]));
 
				int lng=(int)(Double.parseDouble(array[1])*1000000);				
				gpsBody.setLongitude(lng);

				int lat=(int)(Double.parseDouble(array[3])*1000000);				
				gpsBody.setLatitude(lat); 
				
				gpsBody.setPowerBatteryStatus((byte)0);
				
				gpsBody.setSatelliteCount((byte)0);
				
				gpsBody.setServerTime(dateformat.format(dt));
				
				gpsBody.setSoftVersion("");
				
				short speed=(short)Double.parseDouble(array[6]);	
				gpsBody.setSpeed(speed);
				
				gpsBody.setUploadType((int)0);
				
				return gpsBody;								
			} 
			
		}catch(Exception e){			
		}		
		return null;
	}
	
	public void run() {	
		hBaseUtil.createTable(TABLE_GPS_NAME) ;		
		//System.out.println("query HBase begin ...");
		//TestQuery();
		//System.out.println("query HBase end ...");
		
		while(true){
			try {
 
               PlcContent plcContent = GlobalCache.getInstance().getGpsDataQueue().take();               
               GPSBody gpsBody= parserGPSBody(plcContent);
				if( gpsBody!=null){
 
					//1: 解析中文位置 
					refrushChinseLocation(gpsBody);
					
					//2: 保存到redis
					Map<String,String> map=gpsBody.ToMap(floatFormat, dateformat);				
					redisUtil.add("GPSPosition", gpsBody.getDevice_unid(), map);
					
 
						
									
					//3: 保存到数据库
					refrushDB(gpsBody);
					
					//4: 保存到HBase
					saveToHBase(gpsBody);
				}
 
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}
	
	private void saveToHBase(GPSBody gpsBody){
		hBaseUtil.write(TABLE_GPS_NAME, gpsBody.getDevice_unid(), 
				gpsBody.getGpsTime().getTime(), gpsBody.ToMap(floatFormat, dateformat));
	}
	
	public void TestQuery(){
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
		String dt1="2017-04-20 00:00:27";
		String dt2="2017-05-01 23:50:00";
		
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
		
		LinkedList<HashMap<String, String>> list=hBaseUtil.read(TABLE_GPS_NAME,
			//	"AB63F1A637D34193916F2FCBCC97715F",
				"B0BD9C987AA54C009FA453A9BB232A61",
				columnsList, dateFrom, dateTo);
		
		Gson gson = new Gson();
		String jsonList=gson.toJson(list);
		
		System.out.println(jsonList);		
	}
	
	private void refrushDB(GPSBody gpsBody){
		Long lastTime=saveDBList.get(gpsBody.getGps_id());
		if(lastTime!=null){
			long diff=(System.currentTimeMillis()-lastTime)/1000;
			if(diff>=300){
				saveDBList.put(gpsBody.getGps_id(), System.currentTimeMillis());
				SaveToDB(gpsBody);
			}

		}else{
			saveDBList.put(gpsBody.getGps_id(), System.currentTimeMillis());
			SaveToDB(gpsBody);
		}
	}
	
	private void getPostionFromRedis(GPSBody gpsBody,GPSPositionChs chs){
		String strlat=redisUtil.getField("GPSPosition", gpsBody.getDevice_unid(), "latitude");
		String strlng=redisUtil.getField("GPSPosition", gpsBody.getDevice_unid(), "longitude");
		
		String province=redisUtil.getField("GPSPosition", gpsBody.getDevice_unid(), "province");
		String city=redisUtil.getField("GPSPosition", gpsBody.getDevice_unid(), "city");
		String area=redisUtil.getField("GPSPosition", gpsBody.getDevice_unid(), "area");
        if(strlat!=null && strlng!=null){
        	try{
            	Double lat=Double.parseDouble(strlat);
            	Double lng=Double.parseDouble(strlng);
            	
            	chs.setLongitude(lng);
            	chs.setLatitude(lat);
            	if(province!=null){
            		chs.setProvince(province);	
            	}
            	
            	if(city!=null){
            	    chs.setCity(city);
            	}
            	
            	if(area!=null){
            		chs.setArea(area);	
            	}
            	
            	
            	Double latitude=Math.floor(lat*1000000);
            	Double longitude=Math.floor(lng*1000000);
            	
           
            	gpsBody.setLatitude(latitude.intValue());
            	gpsBody.setLongitude(longitude.intValue());
            	
        	}catch(Exception e){
        	}	            	
        }
	}
	
	//解析中文位置，如果不定位的话，将最后一次定位的数据更新
	private void refrushChinseLocation(GPSBody gpsBody) throws InterruptedException{
	 
		GPSPositionChs chs=lnglatList.get(gpsBody.getGps_id());
 
		//如果是首次，并且不定位，那么要判断 是否redis有经度纬度，有就将redis的经纬度赋给对象。
		if(chs==null){
			chs=new GPSPositionChs();
			chs.setGpsId(gpsBody.getGps_id());
			chs.setUnid(gpsBody.getDevice_unid());
			chs.setLocationtime(new Date());
			if (gpsBody.getLocationStatus()==1){				
				chs.setLongitude(gpsBody.getLongitude());
				chs.setLatitude(gpsBody.getLatitude());	
				
				saveBaiduMapLocation(chs);	
			}else{				
				//不定位 ，从redis获取
				getPostionFromRedis(gpsBody,chs);
			}
			lnglatList.put(gpsBody.getGps_id(),chs);
		}else{
			long diff=(System.currentTimeMillis()-chs.getLocationtime().getTime())/1000;			
			//10分钟判断一次 并且是定位的
			if(diff>=60 && gpsBody.getLocationStatus()==1){
				chs.setLocationtime(new Date());
										
				double distance=BaiduMap.calPosDistance(chs.getLongitude(), chs.getLatitude(),
						gpsBody.getLongitude(), gpsBody.getLatitude() );
				
				// >10公里进行重新获取位置    或者超过24小时    或者 0.5小时都没有没有获取过中文位置				
				if(distance>=10000 || diff>86400  
				||  ( (chs.getProvince()==null || chs.getProvince().equals("")) && diff>1800  ) 
					){
					chs.setLatitude(gpsBody.getLatitude());
					chs.setLongitude(gpsBody.getLongitude());					
					saveBaiduMapLocation(chs);
 
				}
				lnglatList.put(gpsBody.getGps_id(),chs);
			}
		}
		
		//如果不定位，将lnglatList的经度纬度赋值给它
		if(gpsBody.getLocationStatus()!=1 && 
				chs.getLatitude()!=null && chs.getLongitude()!=null){            	
        	Double latitude=Math.floor(chs.getLatitude() *1000000);
        	Double longitude=Math.floor(chs.getLongitude() *1000000);
        	gpsBody.setLatitude(latitude.intValue());
        	gpsBody.setLongitude(longitude.intValue());			
		}
 
	}
	
 
	private void saveBaiduMapLocation(GPSPositionChs chs){
		
		chs.setProvince("");
		chs.setCity("");
		chs.setArea("");
		
		try{
			
			//中国经纬度范围
			if(chs.getLongitude()>=73.66 &&  chs.getLongitude()<=135.05
					  && chs.getLatitude() >=16.736 &&  chs.getLatitude()<=53.729 ){
				AddressComponent address=BaiduMap.getChinePosition(baiduMapKey
						, chs.getLatitude().toString()
						, chs.getLongitude().toString());	
				
				if(address!=null){
					chs.setProvince(address.getProvince());
					chs.setCity(address.getCity());
					chs.setArea(address.getDistrict());	
					
					//保存到redis
					redisUtil.add("GPSPosition", chs.getUnid() , chs.ToMap(dateformat));
					
					//保存到数据库
					SaveChineseLocationToDB(chs);
					return;
				}
			}else{
				
			}
						
			//保存到redis
			redisUtil.add("GPSPosition", chs.getUnid() , chs.ToMap(dateformat));
			
			//保存到数据库
			SaveChineseLocationToDB(chs);				
			
		}catch(Exception e){			
		}



	}
	
	//保存GPS信息  到表gpsposition
	private void SaveToDB(GPSBody gpsBody){
		
		initDBConnection();
		
		Statement st = null;
		StringBuilder sb=new StringBuilder();
		try {
			st = connection.createStatement();		
			
			sb.append("insert into gpsposition set ");	
			sb.append(getSubSQL(gpsBody));
			sb.append(" on duplicate key update ");
			sb.append(getSubSQL(gpsBody));

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
	
	
	//保存中文位置 到表gpsposition
	private void SaveChineseLocationToDB(GPSPositionChs chs){
		
		initDBConnection();
		
		Statement st = null;
		StringBuilder sb=new StringBuilder();
		try {
			st = connection.createStatement();		
			
			sb.append("insert into gpsposition set ");	
			sb.append(getSub2SQL(chs));
			sb.append(" on duplicate key update ");
			sb.append(getSub2SQL(chs)); 
			st.execute(sb.toString());		
		} catch (Exception e) {
			logger.error(e.toString()+" "+sb.toString());
			try {
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
	
	
	private String getSubSQL(GPSBody gpsBody){
		StringBuilder sb=new StringBuilder();
 
		sb.append(" gps_id='").append(gpsBody.getGps_id()).append("'");
		sb.append(",").append("latitude=").append(gpsBody.getLatitude());
		sb.append(",").append("longitude=").append(gpsBody.getLongitude());
		sb.append(",").append("speed=").append(gpsBody.getSpeed());
		sb.append(",").append("distance=").append(gpsBody.getDistance());
		
		sb.append(",").append("direction=").append(gpsBody.getDirection() );
		sb.append(",").append("height=").append(gpsBody.getHeight() );
 
		sb.append(",").append("softversion='").append(gpsBody.getSoftVersion() ).append("'");
		sb.append(",").append("hardwareversion='").append(gpsBody.getHardwareVersion() ).append("'");
		sb.append(",").append("accstatus=").append(gpsBody.getAccStatus() );
		
		sb.append(",").append("locationstatus=").append(gpsBody.getLocationStatus() );
		sb.append(",").append("gpsmodelstatus=").append(gpsBody.getGpsModelStatus() );
		sb.append(",").append("powerbatterystatus=").append(gpsBody.getPowerBatteryStatus() );
		
		sb.append(",").append("satellitecount=").append(gpsBody.getSatelliteCount() );
 
 
		sb.append(",").append("gpstime='").append(dateformat.format(gpsBody.getGpsTime())).append("'");
		sb.append(",").append("servertime='").append(gpsBody.getServerTime()).append("'");
		
		return sb.toString();
		
	}
	
	
	private String getSub2SQL(GPSPositionChs chs){
		StringBuilder sb=new StringBuilder();
		sb.append(" gps_id='").append(chs.getGpsId()).append("'");
		sb.append(",").append("province='").append(chs.getProvince()).append("'");
		sb.append(",").append("city='").append(chs.getCity()).append("'");
		sb.append(",").append("area='").append(chs.getArea()).append("'");
		sb.append(",").append("locationtime='").append(dateformat.format(chs.getLocationtime())).append("'");	
		return sb.toString();		
	}
		
}


