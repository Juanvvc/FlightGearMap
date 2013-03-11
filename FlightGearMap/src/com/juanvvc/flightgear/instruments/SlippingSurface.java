package com.juanvvc.flightgear.instruments;

import com.juanvvc.flightgear.MyBitmap;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

/** A surface to control an instrument that moves linearly
 * @author juanvi
 *
 */
public class SlippingSurface extends Surface {
	private Matrix m = new Matrix();
	private int xmin, xmax;
	private int ymin, ymax;
	private float min, max;
	private float rotation;
	private int prop;
	
	/**
	 * @param file The image file
	 * @param rotation The rotation to apply to the image file (it is constant)
	 * @param prop The index of the property to read
	 * @param min The minimum value of the property
	 * @param xmin The X component that corresponds to the minimum value (in a 512 system)
	 * @param ymin The Y component that corresponds to the minimum value
	 * @param max
	 * @param xmax
	 * @param ymax
	 */
	public SlippingSurface(
			MyBitmap bitmap,
			float rotation,
			int prop,
			float min, int xmin, int ymin,
			float max, int xmax, int ymax) {
		super(bitmap, 0, 0);
		this.min = min;
		this.xmin = xmin;
		this.xmax = xmax;
		this.max = max;
		this.ymax = ymax;
		this.ymin = ymin;
		this.rotation = rotation;
		this.prop = prop;
	}

	@Override
	public void onDraw(Canvas c) {
		if (planeData == null || !planeData.hasData() || bitmap == null || bitmap.getScaledBitmap() == null) {
			return;
		}

		float value = planeData.getFloat(this.prop);
		x = (value - min) * (xmax - xmin) / (max - min) + xmin;
		y = (value - min) * (ymax - ymin) / (max - min) + ymin;
		
		m.reset();
		final float gridSize = parent.getGridSize();
		final float scale = parent.getScale();
		final float col = parent.getCol();
		final float row = parent.getRow();
		m.setTranslate(
				(col + x / 512f ) * gridSize * scale,
				(row + y / 512f ) * gridSize * scale);
		m.postRotate(
				this.rotation,
				(col + x / 512f ) * gridSize * scale,
				(row + y / 512f ) * gridSize * scale);
		c.drawBitmap(bitmap.getScaledBitmap(), m, null);
	}
}
