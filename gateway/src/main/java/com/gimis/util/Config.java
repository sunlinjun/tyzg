package com.gimis.util;

import java.io.IOException;
import java.util.Properties;

public class Config {
	
	private int terminalTCPPort;
	
	//读空闲超时时间
	private int readerIdleTime;

	//写空闲超时时间
	private int writeIdleTime;
	
	//kafka topic
	private String sourcecodeTopic;
	
	//kafka topic2 
	//给众能租赁  
	private String sourcecodeTopic2;
	

	//kafka 命令控制topic
	private String cmdTopic;
	
	//kafka 地址
	private String kafkaServer;
	

	//kafka 消费者线程数
	private int kafkaClientThreadCount;
	
	//kafka 消费者组号
	private String kafkaGroupID;
	
	//网关号
	private String gateWayID;
	
	//调试 1 调试 0 未调试
	private int isDebug;
	
	
	//调试DEVICE ID
	private String debugID="";
	
	public Config(){
		 Properties prop = new Properties();
		 
		 
		 try {
			prop = PropertiesTools.loadProperties("cfg.properties", System.getProperty("user.dir"));
		
			String port=prop.getProperty("terminalTCPPort").trim();
			setTerminalTCPPort(Integer.parseInt(port)); 
			setReaderIdleTime(Integer.parseInt(prop.getProperty("mina.readerIdleTime").trim())); 
			setWriteIdleTime(Integer.parseInt(prop.getProperty("mina.writeIdleTime").trim()));
			setIsDebug(Integer.parseInt(prop.getProperty("isDebug").trim()));
 
			setSourcecodeTopic(prop.getProperty("kafka.sourcecodeTopic").trim());			
			setSourcecodeTopic2(prop.getProperty("kafka.sourcecodeTopic2").trim());
						
			setCmdTopic(prop.getProperty("kafka.cmdTopic").trim());
 
			setGateWayID(prop.getProperty("gateWayID").trim());
			setKafkaServer(prop.getProperty("kafka.server").trim());
 
			setKafkaGroupID(prop.getProperty("kafka.groupID").trim());
			setKafkaClientThreadCount(Integer.parseInt(prop.getProperty("kafka.client.threadcount").trim()));

			
			setDebugID(prop.getProperty("debugID").trim());
					 
		 } catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
 
 
			 
		 
	}

	public int getTerminalTCPPort() {
		return terminalTCPPort;
	}

	public void setTerminalTCPPort(int terminalTCPPort) {
		this.terminalTCPPort = terminalTCPPort;
	}

	public int getReaderIdleTime() {
		return readerIdleTime;
	}

	public void setReaderIdleTime(int readerIdleTime) {
		this.readerIdleTime = readerIdleTime;
	}

	public int getWriteIdleTime() {
		return writeIdleTime;
	}

	public void setWriteIdleTime(int writeIdleTime) {
		this.writeIdleTime = writeIdleTime;
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

	public String getSourcecodeTopic2() {
		return sourcecodeTopic2;
	}

	public void setSourcecodeTopic2(String sourcecodeTopic2) {
		this.sourcecodeTopic2 = sourcecodeTopic2;
	}

	public String getDebugID() {
		return debugID;
	}

	public void setDebugID(String debugID) {
		this.debugID = debugID;
	}

 

}
