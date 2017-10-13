package com.gimis;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.gimis.gateway.CommandConsumer;
import com.gimis.gateway.ProcessDataThread;
import com.gimis.gateway.SendCommandThread;
import com.gimis.gateway.network.NetController;
import com.gimis.gateway.network.SessionManager;
import com.gimis.util.Config;
import com.gimis.util.SourceMessage;

@SpringBootApplication
public class GatewayApplication {
	
	public static void main(String[] args) {
		
		System.out.println("gateway start ... ");
		
		SpringApplication.run(GatewayApplication.class, args);
		
		//配置文件
		Config config=new Config();
		
		//发送收到的数据队列
		BlockingQueue<SourceMessage> sendQueue=new LinkedBlockingQueue<SourceMessage>();
 
		//下发的命令队列
		BlockingQueue<SourceMessage> cmdQueue=new LinkedBlockingQueue<SourceMessage>();
				
		//session管理
		SessionManager sessionManager=new SessionManager();
		
		//响应应答，发送数据至KAFKA
		ProcessDataThread  processDataThread=new ProcessDataThread(config,sendQueue,sessionManager);
		processDataThread.setDaemon(true);
		processDataThread.start();	
 
		//消费者
		CommandConsumer commandConsumer=new CommandConsumer(config,cmdQueue);
		commandConsumer.run();

		//发送命令
		SendCommandThread sendCommmandThread=new SendCommandThread(sessionManager,cmdQueue);
		sendCommmandThread.setDaemon(true);
		sendCommmandThread.start();
		
		//网络接受
		NetController netController=new NetController(config,sessionManager,sendQueue);
		netController.start();
		
	}
}
