package com.gimis.gateway.kafka;

import java.util.concurrent.BlockingQueue;

import com.gimis.gateway.network.SessionManager;
import com.gimis.util.SourceMessage;

public class SendCommandThread extends Thread{
	
	private SessionManager sessionManager;
	
	private BlockingQueue<SourceMessage> cmdQueue;
	
	public SendCommandThread(SessionManager sessionManager,BlockingQueue<SourceMessage> cmdQueue){
		this.sessionManager=sessionManager;
		this.cmdQueue=cmdQueue;
	}
	
	public void run() {			
		while (true) {			
			try {
				SourceMessage message=cmdQueue.take();		
				sessionManager.writeSession(message.getAddress(),message.getData());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

 

}

