package com.gimis;

import java.io.IOException;
import java.util.Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.gimis.dataserver.MessageParserThread;
import com.gimis.dataserver.command.CommandThread;
import com.gimis.dataserver.command.RepeatSendCommandList;
import com.gimis.dataserver.gps.SavePositionThread;
import com.gimis.dataserver.kafka.PlcDataConsumer;
import com.gimis.dataserver.kafka.SendCommandToKafkaThread;
import com.gimis.dataserver.kafka.SendDataToAnalysisThread;
import com.gimis.dataserver.plc.RefrushPLCDataThread;
import com.gimis.dataserver.plc.SavePlcDataToHBaseThread;
import com.gimis.dataserver.terminal.GPSDeviceManager;
import com.gimis.util.PropertiesTools;

@SpringBootApplication
public class DataserverPlcApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataserverPlcApplication.class, args);
		
	 
 
		//重复发送命令列表
		RepeatSendCommandList sendCommandList=new RepeatSendCommandList();
		
		int parserThreadCount=1;
		int savePlcThreadCount=1;
		Properties cfgPro = new Properties();
		try {
			cfgPro = PropertiesTools.loadProperties("cfg.properties", System.getProperty("user.dir"));
		
			parserThreadCount = Integer.parseInt(cfgPro.getProperty("parserThreadCount"));	
			
			savePlcThreadCount = Integer.parseInt(cfgPro.getProperty("savePlcThreadCount"));
		
		} catch (IOException e) {
			e.printStackTrace();
		}				
	 
		
		//设备列表
		GPSDeviceManager gpsDeviceManager = new GPSDeviceManager();
		
		//数据解析		
		for(int i=0;i<parserThreadCount;i++){
			MessageParserThread messageParserThread = new MessageParserThread(gpsDeviceManager,sendCommandList);
			messageParserThread.setDaemon(true);
			messageParserThread.start();
		}
		
		//保存最新的PLC数据到Redis和MySQL
		RefrushPLCDataThread refrushPLCDataThread = new RefrushPLCDataThread();
		refrushPLCDataThread.setDaemon(true);
		refrushPLCDataThread.start();
		
		//保存PLC数据到HBase
		for(int i=0;i<savePlcThreadCount;i++){
			SavePlcDataToHBaseThread savePlcDataToHBaseThread = new SavePlcDataToHBaseThread();
			savePlcDataToHBaseThread.setDaemon(true);
			savePlcDataToHBaseThread.start();			
		}
		
		//保存GPS位置数据		
		SavePositionThread savePositionThread = new SavePositionThread();
		savePositionThread.setDaemon(true);
		savePositionThread.start();		
		
		//从数据库获取主动下发命令线程
		CommandThread commandThread=new CommandThread(sendCommandList);
		commandThread.setDaemon(true);
		commandThread.start();	
		
		// 下发命令
		SendCommandToKafkaThread sendData = new SendCommandToKafkaThread();
		sendData.setDaemon(true);
		sendData.start();
		
		//下发数据到KAFKA		
		SendDataToAnalysisThread sendDataToAnalysisThread = new SendDataToAnalysisThread();
		sendDataToAnalysisThread.setDaemon(true);
		sendDataToAnalysisThread.start();
 
		// 接收消息
		PlcDataConsumer plcDataConsumer = new PlcDataConsumer();
		plcDataConsumer.run();
		
		
		

	}
	
 
}
