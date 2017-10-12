package com.gimis.dataserver.command;

import java.util.Date;

public class ResultCommand {
	private int commandLogID;
	private int resultStatus;
	private Date dt;

	public int getCommandLogID() {
		return this.commandLogID;
	}

	public void setCommandLogID(int commandLogID) {
		this.commandLogID = commandLogID;
	}

	public int getResultStatus() {
		return this.resultStatus;
	}

	public void setResultStatus(int resultStatus) {
		this.resultStatus = resultStatus;
	}

	public Date getDt() {
		return this.dt;
	}

	public void setDt(Date dt) {
		this.dt = dt;
	}
}