package com.juanvvc.flightgear.instruments2;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.juanvvc.flightgear.PlaneData;
import com.juanvvc.flightgear.instruments.Surface;

/** A special surface to draw the attitude. See usage example in the C172 */
public class AtiSurface extends Surface {
	private Matrix matrix;

	public AtiSurface(String file, float x, float y) {
		super(file, x, y);
		matrix = new Matrix();
	}
	@Override
	public void onDraw(Canvas c, Bitmap b) {
		if (planeData == null) {
			return;
		}
		
		// draw pitch
		matrix.reset();
		float col = parent.getCol();
		float row = parent.getRow();
		float gridSize = parent.getGridSize();
		float scale = parent.getScale();
		// translate 23 /  pixels each 5 degrees
		float roll = planeData.getFloat(PlaneData.ROLL);
		if (roll > 60) {
			roll = 60;
		}
		float pitch = planeData.getFloat(PlaneData.PITCH);
		if (pitch > 45) {
			pitch = 45;
		}
		
		matrix.postTranslate(((0.5f + col) * gridSize) * scale - b.getWidth() / 2, ((0.5f + row) * gridSize + pitch * (23 * gridSize/ 512) / 5) * scale - b.getHeight() / 2);
		matrix.postRotate(-roll, ((0.5f + col) * gridSize) * scale, ((0.5f + row) * gridSize) * scale);
		c.drawBitmap(b, matrix, null);
	}
}
