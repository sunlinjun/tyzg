package com.gimis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.gimis.dataserver.MessageProcessThread;
import com.gimis.dataserver.can.SaveCANThread;
import com.gimis.dataserver.command.CommandThread;
import com.gimis.dataserver.command.RepeatSendCommandList;
import com.gimis.dataserver.gps.SaveChsThread;
import com.gimis.dataserver.gps.SavePositionThread;
import com.gimis.dataserver.kafka.CommandConsumer;
import com.gimis.dataserver.kafka.SendCommandToKafkaThread;
import com.gimis.dataserver.kafka.SendDefaultMessageToKafkaThread;
import com.gimis.dataserver.terminal.GPSDeviceManager;
import com.gimis.util.GlobalCache;

@SpringBootApplication
public class DataserverApplication {

	public static void main(String[] args) {

		SpringApplication.run(DataserverApplication.class, args);
		
		//重复发送命令列表
		RepeatSendCommandList sendCommandList=new RepeatSendCommandList();

		//设备列表
		GPSDeviceManager gpsDeviceManager = new GPSDeviceManager();

		// 消费者 解析终端协议，发送命令应答 给终端
		CommandConsumer commandConsumer = new CommandConsumer(GlobalCache.getInstance().getCmdQueue(),
				gpsDeviceManager,sendCommandList);
		commandConsumer.run();

		// 下发命令
		SendCommandToKafkaThread sendData = new SendCommandToKafkaThread(GlobalCache.getInstance().getCmdQueue());
		sendData.setDaemon(true);
		sendData.start();
		
		//发送命令到 分析服务器		
		SendDefaultMessageToKafkaThread sendGpsCanData = new SendDefaultMessageToKafkaThread(
				GlobalCache.getInstance().getSendAnalysisServerQueue());
		sendGpsCanData.setDaemon(true);
		sendGpsCanData.start();		
		
		//保存中文GPS位置				
		SaveChsThread saveChsThread = new SaveChsThread();
		saveChsThread.setDaemon(true);
		saveChsThread.start();		
		
		// 保存GPS位置
		SavePositionThread savePositionThread = new SavePositionThread();
		savePositionThread.setDaemon(true);
		savePositionThread.start();

		// 保存CAN数据
		SaveCANThread SaveCANThread = new SaveCANThread();
		SaveCANThread.setDaemon(true);
		SaveCANThread.start();
		
		//保存  报警信息
		//SaveFaultThread saveFaultThread = new SaveFaultThread();
		//saveFaultThread.setDaemon(true);
		//saveFaultThread.start();
		
		//保存自定义报警
		//SaveFaultSelfDefThread saveFaultSelfDefThread = new SaveFaultSelfDefThread();
		//saveFaultSelfDefThread.setDaemon(true);
		//saveFaultSelfDefThread.start();
		 
		// 命令分发
		MessageProcessThread messageProcessThread = new MessageProcessThread(gpsDeviceManager);
		messageProcessThread.setDaemon(true);
		messageProcessThread.start();
						
		//从数据库获取主动下发命令线程
		CommandThread commandThread=new CommandThread(sendCommandList);
		commandThread.setDaemon(true);
		commandThread.start();		 
	}

}
