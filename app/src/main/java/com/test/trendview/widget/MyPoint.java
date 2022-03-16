package com.test.trendview.widget;

import android.graphics.Point;

public class MyPoint extends Point {
	private Holder holder;
    public int x;
    public int y;
	public MyPoint(int x,int y,Holder holder) {
		set(x, y);
		this.x=x;
		this.y=y;
		
		this.holder=holder;
	}

	public Holder getHolder() {
		return holder;
	}

	public void setHolder(Holder holder) {
		this.holder = holder;
	}
}