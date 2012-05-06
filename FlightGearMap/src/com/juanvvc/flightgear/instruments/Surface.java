package com.juanvvc.flightgear.instruments;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.juanvvc.flightgear.PlaneData;

public abstract class Surface {
	protected float x;
	protected float y;
	protected String file;
	protected Instrument parent;
	
	public Surface(String file, float x, float y) {
		this.file = file;
		this.x = x;
		this.y = y;
	}
	
	public void setParent(Instrument ins) {
		this.parent = ins;
	}
	
	public Instrument getParent() {
		return parent;
	}
	
	public String getFile() {
		return file;
	}
	
	public abstract void onDraw(Canvas c, Bitmap b, PlaneData pd);
}
