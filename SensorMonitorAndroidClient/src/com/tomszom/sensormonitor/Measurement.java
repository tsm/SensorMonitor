package com.tomszom.sensormonitor;

import android.R.bool;

public class Measurement {
	private String device;
	private String measurement;
	private String value;
	private boolean isSubscribing=false;
	public Measurement(String device, String measurement, String value) {
		super();
		this.device = device;
		this.measurement = measurement;
		this.value = value;
	}
	public String getDevice() {
		return device;
	}
	public void setDevice(String device) {
		this.device = device;
	}
	public String getMeasurement() {
		return measurement;
	}
	public void setMeasurement(String measurement) {
		this.measurement = measurement;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
}
