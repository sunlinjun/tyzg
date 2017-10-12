package com.gimis.dataserver.command;

import java.util.Date;

public class ResultCommand {
	
	//COMMAND_LOG表的ID
	private int commandLogID;
	
	//命令结果 0 等待 1 成功  2未应答 3超时
	private int resultStatus;
	
	//时间
	private Date dt;

	public int getCommandLogID() {
		return commandLogID;
	}

	public void setCommandLogID(int commandLogID) {
		this.commandLogID = commandLogID;
	}

	public int getResultStatus() {
		return resultStatus;
	}

	public void setResultStatus(int resultStatus) {
		this.resultStatus = resultStatus;
	}

	public Date getDt() {
		return dt;
	}

	public void setDt(Date dt) {
		this.dt = dt;
	}

}
