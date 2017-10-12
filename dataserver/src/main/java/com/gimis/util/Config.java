package com.gimis.util;

import java.io.IOException;
import java.util.Properties;

public class Config {
		
	//kafka 源码topic
	private String sourcecodeTopic;

	//kafka 命令控制topic
	private String cmdTopic;
	
	//数据分析 topic
	private String analysisTopic;
	
	//kafka 地址
	private String kafkaServer;
		
	//kafka 消费者线程数
	private int kafkaClientThreadCount;
	
	//kafka 消费者组号
	private String kafkaGroupID;
	
	//网关号
	private String gateWayID; 
	
	//APPKEY
	private String appKey;
	
	//调试 1 调试 0 未调试
	private int isDebug;
	
	//从redis获取GPS和Device对应关系 的时间间隔
	private int gpsDeviceInterval;
	
	//保存到GPS,CAN表时间隔间 单位 秒
	private int saveMysqlInterval=600;
	
	//读取报警自定义的时间间隔 单位 秒
	private int faultDefIntervalFromMySQL=3600;
	
	//DEBUG GPSID
	private String debugGPSID="";
	
	//百度地图Key
	private String baiduMapKey;
	
	//mysql
	private String dataSourceDriveName;
	
	private String dataSourceURL;
	
	private String dataSourceUserName;
	
	private String dataSourcePassword;
	
	public Config(){
		 Properties prop = new Properties();
		 try {
			prop = PropertiesTools.loadProperties("cfg.properties", System.getProperty("user.dir"));
			
			setIsDebug(Integer.parseInt(prop.getProperty("isDebug").trim()));
			 
			setSourcecodeTopic(prop.getProperty("kafka.sourcecodeTopic").trim());
			setCmdTopic(prop.getProperty("kafka.cmdTopic").trim());
			setAnalysisTopic(prop.getProperty("kafka.analysisTopic").trim());
 
			setGateWayID(prop.getProperty("gateWayID").trim());
			setAppKey(prop.getProperty("appKey").trim());
			
			setKafkaServer(prop.getProperty("kafka.server").trim());
			setKafkaGroupID(prop.getProperty("kafka.groupID").trim());
			setKafkaClientThreadCount(Integer.parseInt(prop.getProperty("kafka.client.threadcount").trim()));
			setGpsDeviceInterval(Integer.parseInt(prop.getProperty("redis.gpsDeviceInterval").trim()));

			//最小值 10分钟
			int gpsInterval= Integer.parseInt(prop.getProperty("mysql.gpsDeviceInterval").trim());
			if(gpsInterval<600){
				gpsInterval=600;
			}
			setSaveMysqlInterval(gpsInterval);
			
			int faultInterval=Integer.parseInt(prop.getProperty("mysql.faultDefInterval").trim());
			if(faultInterval<3600){
				faultInterval=3600;
			}
			setFaultDefIntervalFromMySQL(faultInterval);
 
 
			//mysql
			setDataSourceDriveName(prop.getProperty("datasource.driver-class-name").trim());
			setDataSourceURL(prop.getProperty("datasource.url").trim());
			setDataSourceUserName(prop.getProperty("datasource.username").trim());
			setDataSourcePassword(prop.getProperty("datasource.password").trim());
			
			setBaiduMapKey(prop.getProperty("baiduMap.key").trim());

			setDebugGPSID(prop.getProperty("debugGPSID").trim());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

 
	 
		 
 
						
 
		 
			 
	}

 

 

 

	public String getGateWayID() {
		return gateWayID;
	}

	public void setGateWayID(String gateWayID) {
		this.gateWayID = gateWayID;
	}

	public String getKafkaServer() {
		return kafkaServer;
	}

	public void setKafkaServer(String kafkaServer) {
		this.kafkaServer = kafkaServer;
	}

	public int getIsDebug() {
		return isDebug;
	}

	public void setIsDebug(int isDebug) {
		this.isDebug = isDebug;
	}
 
	public String getKafkaGroupID() {
		return kafkaGroupID;
	}

	public void setKafkaGroupID(String kafkaGroupID) {
		this.kafkaGroupID = kafkaGroupID;
	}

 

	public String getCmdTopic() {
		return cmdTopic;
	}

	public void setCmdTopic(String cmdTopic) {
		this.cmdTopic = cmdTopic;
	}

	public int getKafkaClientThreadCount() {
		return kafkaClientThreadCount;
	}

	public void setKafkaClientThreadCount(int kafkaClientThreadCount) {
		this.kafkaClientThreadCount = kafkaClientThreadCount;
	}

	public String getSourcecodeTopic() {
		return sourcecodeTopic;
	}

	public void setSourcecodeTopic(String sourcecodeTopic) {
		this.sourcecodeTopic = sourcecodeTopic;
	}







	public String getAppKey() {
		return appKey;
	}







	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}







	public int getGpsDeviceInterval() {
		return gpsDeviceInterval;
	}







	public void setGpsDeviceInterval(int gpsDeviceInterval) {
		this.gpsDeviceInterval = gpsDeviceInterval;
	}







	public int getSaveMysqlInterval() {
		return saveMysqlInterval;
	}







	public void setSaveMysqlInterval(int saveMysqlInterval) {
		this.saveMysqlInterval = saveMysqlInterval;
	}







	public String getDataSourceDriveName() {
		return dataSourceDriveName;
	}







	public void setDataSourceDriveName(String dataSourceDriveName) {
		this.dataSourceDriveName = dataSourceDriveName;
	}







	public String getDataSourceURL() {
		return dataSourceURL;
	}







	public void setDataSourceURL(String dataSourceURL) {
		this.dataSourceURL = dataSourceURL;
	}







	public String getDataSourceUserName() {
		return dataSourceUserName;
	}







	public void setDataSourceUserName(String dataSourceUserName) {
		this.dataSourceUserName = dataSourceUserName;
	}







	public String getDataSourcePassword() {
		return dataSourcePassword;
	}







	public void setDataSourcePassword(String dataSourcePassword) {
		this.dataSourcePassword = dataSourcePassword;
	}







	public String getBaiduMapKey() {
		return baiduMapKey;
	}







	public void setBaiduMapKey(String baiduMapKey) {
		this.baiduMapKey = baiduMapKey;
	}
 

	public int getFaultDefIntervalFromMySQL() {
		return faultDefIntervalFromMySQL;
	}
 

	public void setFaultDefIntervalFromMySQL(int faultDefIntervalFromMySQL) {
		this.faultDefIntervalFromMySQL = faultDefIntervalFromMySQL;
	}







	public String getDebugGPSID() {
		return debugGPSID;
	}







	public void setDebugGPSID(String debugGPSID) {
		this.debugGPSID = debugGPSID;
	}







	public String getAnalysisTopic() {
		return analysisTopic;
	}







	public void setAnalysisTopic(String analysisTopic) {
		this.analysisTopic = analysisTopic;
	}







 
 
 
 

 

}
