package com.gimis.ps.msg;

import java.io.Serializable;
import java.util.Date;

public class ReportBody implements Serializable {
	private static final long serialVersionUID = -614450009984776399L;
	private String gps_id;
	private String device_unid;
	private Date reportTime;
	private Integer totalOil;
	private Integer newOil;
	private Integer totalACC;
	private Short newACC;
	private Integer totalWorkTime;
	private Short newWorkTime;
	private Integer totalDistance;
	private Integer newDistance;
	private Integer totalMonthFlow;

	public Date getReportTime() {
		return this.reportTime;
	}

	public void setReportTime(Date reportTime) {
		this.reportTime = reportTime;
	}

	public Double getTotalOil() {
		return Double.valueOf(this.totalOil.intValue() * 0.5D);
	}

	public void setTotalOil(Integer totalOil) {
		this.totalOil = totalOil;
	}

	public Double getNewOil() {
		return Double.valueOf(this.newOil.intValue() * 0.5D);
	}

	public void setNewOil(Integer newOil) {
		this.newOil = newOil;
	}

	public Double getTotalACC() {
		return Double.valueOf(this.totalACC.intValue() / 60.0D);
	}

	public void setTotalACC(Integer totalACC) {
		this.totalACC = totalACC;
	}

	public Double getNewACC() {
		return Double.valueOf(this.newACC.shortValue() / 60.0D);
	}

	public void setNewACC(Short newACC) {
		this.newACC = newACC;
	}

	public Double getTotalWorkTime() {
		return Double.valueOf(this.totalWorkTime.intValue() / 60.0D);
	}

	public void setTotalWorkTime(Integer totalWorkTime) {
		this.totalWorkTime = totalWorkTime;
	}

	public Double getNewWorkTime() {
		return Double.valueOf(this.newWorkTime.shortValue() / 60.0D);
	}

	public void setNewWorkTime(Short newWorkTime) {
		this.newWorkTime = newWorkTime;
	}

	public Double getTotalDistance() {
		return Double.valueOf(this.totalDistance.intValue() / 1000.0D);
	}

	public void setTotalDistance(Integer totalDistance) {
		this.totalDistance = totalDistance;
	}

	public Double getNewDistance() {
		return Double.valueOf(this.newDistance.intValue() / 1000.0D);
	}

	public void setNewDistance(Integer newDistance) {
		this.newDistance = newDistance;
	}

	public Integer getTotalMonthFlow() {
		return this.totalMonthFlow;
	}

	public void setTotalMonthFlow(Integer totalMonthFlow) {
		this.totalMonthFlow = totalMonthFlow;
	}

	public String getGps_id() {
		return this.gps_id;
	}

	public void setGps_id(String gps_id) {
		this.gps_id = gps_id;
	}

	public String getDevice_unid() {
		return this.device_unid;
	}

	public void setDevice_unid(String device_unid) {
		this.device_unid = device_unid;
	}
}