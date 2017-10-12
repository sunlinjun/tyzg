package com.gimis.dataserver.command;

public class SendCommandThread extends Thread {
	private RepeatSendCommandList sendCommandList;

	public SendCommandThread(RepeatSendCommandList sendCommandList) {
		this.sendCommandList = sendCommandList;
	}

	public void run() {
		while (true) {
			this.sendCommandList.send();
			try {
				Thread.sleep(9000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}