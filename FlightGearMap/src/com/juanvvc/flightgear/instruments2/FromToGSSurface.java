package com.juanvvc.flightgear.instruments2;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.juanvvc.flightgear.instruments.Surface;


/** A special surface to draw the flag from/to in a VOR. See an example in the C172 */
public class FromToGSSurface extends Surface {
	private int nav_to, nav_from, gs; // position of this flags in PlaneData

	public FromToGSSurface(String file, float x, float y, int nav_to, int nav_from, int gs) {
		super(file, x, y);
		this.nav_from = nav_from;
		this.nav_to = nav_to;
		this.gs = gs;
	}
	@Override
	public void onDraw(Canvas c, Bitmap b) {
		if (planeData == null) {
			return;
		}
		
		float col = parent.getCol();
		float row = parent.getRow();
		float gridSize = parent.getGridSize();
		float scale = parent.getScale();
		
		int left = (int) ((col + x / 512f) * gridSize * scale);
		int top = (int) ((row + y / 512f) * gridSize * scale);

		if (gs == -1) {
			if (planeData.getBool(nav_to)) {
				c.drawBitmap(b,
						new Rect(0, 0, b.getWidth() / 3, b.getHeight()),
						new Rect(left, top, (int)(left + b.getWidth() / 3 * scale), (int)(top + b.getHeight() * scale)),
						null);
			} else if (planeData.getBool(nav_from)) {
				c.drawBitmap(b,
						new Rect(b.getWidth() / 3, 0, 2 * b.getWidth() / 3, b.getHeight()),
						new Rect(left, top, (int)(left + b.getWidth() / 3 * scale), (int)(top + b.getHeight() * scale)),
						null);
			}
		} else {
			if (planeData.getBool(gs)) {
				c.drawBitmap(b,
						new Rect(2 * b.getWidth() / 3, 0, b.getWidth(), b.getHeight()),
						new Rect(left, top, (int)(left + b.getWidth() / 3 * scale), (int)(top + b.getHeight() * scale)),
						null);
			}
		}
	}
}

