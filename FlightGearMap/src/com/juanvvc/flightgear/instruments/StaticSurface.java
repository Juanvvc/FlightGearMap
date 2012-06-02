package com.juanvvc.flightgear.instruments;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.juanvvc.flightgear.PlaneData;

/** An surfaces that draws an static image on a position. */
public class StaticSurface extends Surface {
	Matrix m;
	public StaticSurface(String file, float x, float y) {
		super(file, x, y);
		m = null;
	}
	
	@Override
	public void onMove() {
		if (m == null) {
			m = new Matrix();
			final float gridSize = parent.getGridSize();
			final float scale = parent.getScale();
			final float col = parent.getCol();
			final float row = parent.getRow();
			m.setTranslate((col + x / 512f) * gridSize * scale, (row + y / 512f) * gridSize * scale);
		}
	}

	@Override
	public void onDraw(Canvas c, Bitmap b) {
		
		if (planeData == null || !planeData.hasData()) {
			return;
		}
		
		if (m == null) {
			onMove();
		}
		c.drawBitmap(b, m, null);
	}
}
