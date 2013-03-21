package com.juanvvc.flightgear.instruments;

import com.juanvvc.flightgear.MyBitmap;

import android.graphics.Canvas;
import android.graphics.Matrix;

/** An surfaces that draws an static image on a position. */
public class StaticSurface extends Surface {
	Matrix m;
	public StaticSurface(MyBitmap bitmap, float x, float y) {
		super(bitmap, x, y);
		m = null;
	}
	
	@Override
	public void onBitmapChanged() {
		if (m == null) {
			m = new Matrix();
			final float gridSize = parent.getGridSize();
			final float scale = parent.getScale();
			final float col = parent.getCol();
			final float row = parent.getRow();
			m.setTranslate((col + relx) * gridSize * scale, (row + rely) * gridSize * scale);
		}
	}

	@Override
	public void onDraw(Canvas c) {
		
		if (planeData == null || !planeData.hasData() || bitmap == null || bitmap.getScaledBitmap() == null || m == null) {
			return;
		}
		
		c.drawBitmap(bitmap.getScaledBitmap(), m, null);
	}
}
