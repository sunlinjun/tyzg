package com.gimis.dataserver.command;
 
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

import com.gimis.util.GlobalCache;
import com.gimis.util.SourceMessage;

public class RepeatSendCommandList {
	private ReentrantLock myLock;
	private ArrayList<SendCommandData> list;
 
	public RepeatSendCommandList() {
		myLock = new ReentrantLock();
		list = new ArrayList<SendCommandData>();
	}

	public void put(SendCommandData data) {
		myLock.lock();
		try {
			list.add(data);
		} finally {
			myLock.unlock();
		}
	}

	public void deleteData(String vehicleID, int seq) {
		
	 
		myLock.lock();
		try {
			try {
				Iterator<SendCommandData> iter = list.iterator();
				while (iter.hasNext()) {
 
					SendCommandData data = iter.next();					
					if (data.getGpsID().compareTo(vehicleID) == 0) {
						if (data.getSeqNO() == seq) {
 
							ResultCommand resultCommand = new ResultCommand();
							resultCommand.setCommandLogID(data.getCommanLogID());
							resultCommand.setDt(new Date());
							resultCommand.setResultStatus(1);							
							GlobalCache.getInstance().getTerminalResponseQueue().put(resultCommand);
							
							iter.remove();
							break;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} finally {
			myLock.unlock();
		}
	}

	public void send() {
		myLock.lock();
		try {
			Iterator<SendCommandData> iter = list.iterator();
			while (iter.hasNext()) {
				SendCommandData data = iter.next();

				if (data.getCount() > 0) {
					data.setCount(data.getCount()-1);		
					String address = GlobalCache.getInstance().getGpsIpAddressMap().get(data.getGpsID());
					SourceMessage message=new SourceMessage("1",address,data.getData());
									
					GlobalCache.getInstance().getCmdQueue().add(message);	
 
					if(data.getCount()==2){
						ResultCommand resultCommand = new ResultCommand();
						resultCommand.setCommandLogID(data.getCommanLogID());
						resultCommand.setDt(new Date());
						resultCommand.setResultStatus(2);							
						try {
							GlobalCache.getInstance().getTerminalResponseQueue().put(resultCommand);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}						
					}

				}else{
					iter.remove();
				}
	 
			}
		} finally {
			myLock.unlock();
		}
	}
}
