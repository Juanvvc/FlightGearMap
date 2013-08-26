package com.juanvvc.flightgear;

import android.graphics.Bitmap;

/** A wrap over a Bitmap to manage images before loading them */
public class MyBitmap {
	private int width;
	private int height;
	private int xo;
	private int yo;
	private Bitmap b;
	private String file;
	public MyBitmap(String file, int xo, int yo, int w, int h) {
		this.xo = xo;
		this.yo = yo;
		this.height = h;
		this.width = w;
		b = null;
		this.file = file;
	}
	public void updateBitmap(BitmapProvider bitmapProvider, int gridSize) {
		if (file != null) {
			try {
				Bitmap original = bitmapProvider.getScaledBitmap(this.file);
				if ( xo > -1) {
					float s = bitmapProvider.getScale();
					b = Bitmap.createBitmap(
							original,
							(int)(this.xo * s * gridSize / 512), (int)(this.yo * s * gridSize / 512),
							(int)(this.width * s * gridSize / 512), (int)(this.height * s * gridSize / 512));
				} else {
					b = bitmapProvider.getScaledBitmap(this.file);
				}
			} catch (NullPointerException e) {
				MyLog.w(MyBitmap.class, "Null bitmap: " + this.file);
				b = null;
			} catch (IllegalArgumentException e) {
				MyLog.w(MyBitmap.class, "Bitmap out of bounds: " + this.file);
				b = null;
			}
		}
	}
	public int getHeight() {
		return height;
	}
	public int getWidth() {
		return width;
	}
	public Bitmap getScaledBitmap() {
		return b;
	}
	public String getFile() {
		return this.file;
	}
}
