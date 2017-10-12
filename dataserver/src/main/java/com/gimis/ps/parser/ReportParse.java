package com.gimis.ps.parser;

import com.gimis.ps.msg.ReportBody;
import com.gimis.util.MessageTools;
import java.nio.ByteBuffer;
import java.util.Calendar;

public class ReportParse {
	private ReportBody reportBody;

	public ReportParse() {
		this.reportBody = new ReportBody();
	}

	public ReportBody getReportBody() {
		return this.reportBody;
	}

	public void deCode(String gpsId, ByteBuffer buf) {
		this.reportBody.setGps_id(gpsId);

		int year = MessageTools.getByteValue(buf.get()) + 2000;
		int month = MessageTools.getByteValue(buf.get()) - 1;
		int day = MessageTools.getByteValue(buf.get());

		Calendar c = Calendar.getInstance();
		c.set(year, month, day, 0, 0, 0);
		this.reportBody.setReportTime(c.getTime());

		this.reportBody.setTotalOil(Integer.valueOf(buf.getInt()));
		this.reportBody.setNewOil(Integer.valueOf(buf.getInt()));

		this.reportBody.setTotalACC(Integer.valueOf(buf.getInt()));
		this.reportBody.setNewACC(Short.valueOf(buf.getShort()));

		this.reportBody.setTotalWorkTime(Integer.valueOf(buf.getInt()));
		this.reportBody.setNewWorkTime(Short.valueOf(buf.getShort()));

		this.reportBody.setTotalDistance(Integer.valueOf(buf.getInt()));

		this.reportBody.setNewDistance(Integer.valueOf(buf.getInt()));

		this.reportBody.setTotalMonthFlow(Integer.valueOf(buf.getInt()));
	}
}