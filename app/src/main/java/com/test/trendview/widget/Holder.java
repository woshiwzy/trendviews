package com.test.trendview.widget;



public class Holder {

	private String stamp;
	private float value;

	public Holder() {
	}

	public Holder(String stamp, float value) {
		this.stamp = stamp;
		this.value = value;
	}

	public String getStamp() {
		return stamp;
	}

	public void setStamp(String stamp) {
		this.stamp = stamp;
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}

	public Holder cloneSelf() {
		Holder cloneHolder = new Holder();
		cloneHolder.setStamp(stamp);
		cloneHolder.setValue(value);
		return cloneHolder;
	}
}
