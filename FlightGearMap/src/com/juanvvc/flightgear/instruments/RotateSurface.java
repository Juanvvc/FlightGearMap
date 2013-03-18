package com.juanvvc.flightgear.instruments;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.juanvvc.flightgear.MyBitmap;
import com.juanvvc.flightgear.PlaneData;

/** A surface that is rotated according to some data in PlaneData. */
public class RotateSurface extends Surface {
	protected Matrix m;
	protected int pdIdx;
	protected float rcx;
	protected float rcy;
	private float rscale;
	private float min, amin;
	private float max, amax;
	
	/**
	 * @param file The file of the image (does not include directory)
	 * @param x Horizontal position of the surface inside the instrument
	 * @param y Horizontal position of the surface inside the instrument
	 * @param pdIdx Index of PlaneData that holds the data
	 * @param rscale Scale of the data (usually 1: do not modify the data)
	 * @param rcx Rotation center (inside the instrument)
	 * @param rcy Rotation center (inside the instrument)
	 * @param min Minimum value of the data
	 * @param amin Angle that corresponds to the minimum value
	 * @param max Max value of the data
	 * @param amax Angle that corresponds to the max value.
	 */
	public RotateSurface(
			MyBitmap bitmap, float x, float y,
			int pdIdx, float rscale,
			int rcx, int rcy,
			float min, float amin, float max, float amax) {
		super(bitmap, x, y);
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
	public void onDraw(Canvas c) {
		if (planeData == null || !planeData.hasData() || bitmap == null || bitmap.getScaledBitmap() == null) {
			return;
		}
		
		m.reset();
		final float realscale = parent.getScale() * parent.getGridSize();
		final float col = parent.getCol();
		final float row = parent.getRow();
		m.setTranslate(
				(col + relx ) * realscale,
				(row + rely ) * realscale);
		m.postRotate(
				getRotationAngle(this.planeData),
				(col + rcx / 512f ) * realscale,
				(row + rcy / 512f ) * realscale);
		c.drawBitmap(bitmap.getScaledBitmap(), m, null);
	}
}
