package com.gimis.dataserver.command;

public class SendCommandThread extends Thread{
	  
private RepeatSendCommandList sendCommandList;

public SendCommandThread(RepeatSendCommandList sendCommandList){
	this.sendCommandList = sendCommandList;
}
	
public void run() {			 
	while (true) {			
		sendCommandList.send();					
		try {
			Thread.sleep(9000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}		
}
}

