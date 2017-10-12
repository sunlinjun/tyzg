package com.gimis.dataserver.command;

import com.gimis.util.GlobalCache;
import com.gimis.util.SourceMessage;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;


import java.util.concurrent.locks.ReentrantLock;

public class RepeatSendCommandList {
	private ReentrantLock myLock;
	private ArrayList<SendCommandData> list;
 
	public RepeatSendCommandList() {
		this.myLock = new ReentrantLock();
		list = new ArrayList<SendCommandData>();
	}

	public void put(SendCommandData data) {
		this.myLock.lock();
		try {
			this.list.add(data);
		} finally {
			this.myLock.unlock();
		}
	}

	public void deleteData(String vehicleID, int seq) {
		this.myLock.lock();
		try {
			Iterator<SendCommandData> iter = this.list.iterator();
			while (iter.hasNext()) {
				SendCommandData data = (SendCommandData) iter.next();
				if ((data.getGpsID().compareTo(vehicleID) == 0) && (data.getSeqNO() == seq)) {
					ResultCommand resultCommand = new ResultCommand();
					resultCommand.setCommandLogID(data.getCommanLogID());
					resultCommand.setDt(new Date());
					resultCommand.setResultStatus(1);
					GlobalCache.getInstance().getTerminalResponseQueue().put(resultCommand);

					iter.remove();
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.myLock.unlock();
		}
	}

	public void send() {
		this.myLock.lock();
		try {
			Iterator<SendCommandData> iter = this.list.iterator();
			while (iter.hasNext()) {
				SendCommandData data = (SendCommandData) iter.next();

				if (data.getCount() > 0) {
					data.setCount(data.getCount() - 1);
					String address = (String) GlobalCache.getInstance().getGpsIpAddressMap().get(data.getGpsID());
					SourceMessage message = new SourceMessage("1", address, data.getData());

					GlobalCache.getInstance().getCmdQueue().add(message);

					if (data.getCount() == 2) {
						ResultCommand resultCommand = new ResultCommand();
						resultCommand.setCommandLogID(data.getCommanLogID());
						resultCommand.setDt(new Date());
						resultCommand.setResultStatus(2);
						try {
							GlobalCache.getInstance().getTerminalResponseQueue().put(resultCommand);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				} else {
					iter.remove();
				}
			}
		} finally {
			this.myLock.unlock();
		}
	}
}