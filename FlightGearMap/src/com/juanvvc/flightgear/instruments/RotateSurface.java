package com.juanvvc.flightgear.instruments;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.juanvvc.flightgear.PlaneData;

public class RotateSurface extends Surface {
	private Matrix m;
	protected int pdIdx;
	private float rcx;
	private float rcy;
	private float rscale;
	private float min, amin;
	private float max, amax;
	public RotateSurface(
			String file, float x, float y,
			int pdIdx, float rscale,
			int rcx, int rcy,
			float min, float amin, float max, float amax) {
		super(file, x, y);
		m = new Matrix();
		this.pdIdx = pdIdx;
		this.rcx = rcx;
		this.rcy = rcy;
		this.min = min;
		this.amin = amin;
		this.max = max;
		this.amax = amax;
		this.rscale = rscale;
	}
	
	protected float getRotationAngle(PlaneData pd) {
		float v = pd.getFloat(pdIdx) * rscale;
		if (v < min) {
			v = min;
		} else if (v > max) {
			v = max;
		}
		return (v - min) * (amax - amin) / (max - min) + amin;
	}

	@Override
	public void onDraw(Canvas c, Bitmap b, PlaneData pd) {
		m.reset();
		final float gridSize = parent.getGridSize();
		final float scale = parent.getScale();
		final float col = parent.getCol();
		final float row = parent.getRow();
		m.setTranslate(
				(col + x / 512f ) * gridSize * scale,
				(row + y / 512f ) * gridSize * scale);
		m.postRotate(
				getRotationAngle(pd),
				(col + rcx / 512f ) * gridSize * scale,
				(row + rcy / 512f ) * gridSize * scale);
		c.drawBitmap(b, m, null);
	}
}
