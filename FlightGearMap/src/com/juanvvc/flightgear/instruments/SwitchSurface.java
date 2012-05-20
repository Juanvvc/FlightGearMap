package com.juanvvc.flightgear.instruments;

import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.juanvvc.flightgear.FGFSConnection;
import com.juanvvc.flightgear.PlaneData;
import com.juanvvc.flightgear.myLog;

public class SwitchSurface extends Surface {
	private String prop;
	private String label;
	private boolean state = true;
	private Paint textPaint;
	private static final String TAG = "Switches";
	
	public SwitchSurface(final String file, final float x, final float y, final String prop, final String label) {
		super(file, x, y);
		this.label = label;
		this.prop = prop;
		state = false;
		
		textPaint = new Paint();
		textPaint.setColor(0xffffffff);
	}
	
	public boolean getState() {
		return state;
	}
	
	public void setState(boolean s) {
		state = s;
	}
	
	@Override
	public void postPlaneData(PlaneData pd) {
		super.postPlaneData(pd);
		
		FGFSConnection conn = pd.getConnection();
		if (conn != null) {
			try {
				state = conn.getBoolean(this.prop);
			} catch (IOException e) {
				myLog.w(TAG, e.toString());
			}
		}
	}
	
	@Override
	public void onDraw(Canvas c, Bitmap b) {
		if (planeData == null || planeData.getConnection() == null) {
			return;
		}
		
		// calculate the position of the switch
		final float gridSize = parent.getGridSize();
		final float scale = parent.getScale();
		final float col = parent.getCol();
		final float row = parent.getRow();
		final int left = (int)((x / 512f + col) * gridSize * scale);
		final int top = (int)((y / 512f + row) * gridSize * scale);
		
		// draw the label
		c.drawText(label, left + b.getWidth() / 5 * scale, top + b.getHeight() / 2 * scale, textPaint);
		// draw the switch according to its state
		if (state) {
			c.drawBitmap(b,
					new Rect(0, 0, b.getWidth(), b.getHeight() / 2),
					new Rect(left, top, (int)(left + b.getWidth() * scale), (int)(top + b.getHeight() / 2 * scale)),
					null);		
		} else {
			c.drawBitmap(b,
				new Rect(0, b.getHeight() / 2, b.getWidth(), b.getHeight()),
				new Rect(left, top, (int)(left + b.getWidth() * scale), (int)(top + b.getHeight() / 2 * scale)),
				null);
		}
	}
}

