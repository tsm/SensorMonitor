package com.tomszom.sensormonitor;

public class Measurement {
	private String stock;
	private String measurement;
	private String value;
	private boolean isSubscribing=false;
	public Measurement(String stock, String measurement, String value) {
		super();
		this.stock = stock;
		this.measurement = measurement;
		this.value = value;
	}
	public String getStock() {
		return stock;
	}
	public void setStock(String stock) {
		this.stock = stock;
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
	public boolean isSubscribing() {
		return isSubscribing;
	}
	public void setSubscribing(boolean isSubscribing) {
		this.isSubscribing = isSubscribing;
	}
	
	
}
