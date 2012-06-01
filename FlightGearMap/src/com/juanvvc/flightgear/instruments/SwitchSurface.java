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
	private static final String TAG = "SwitchSurface";
	
	private static final int SWITCH_HEIGHT = 152;
	private static final int SWITCH_WIDTH = 126;
	
	/** If true, the switch needs to be post to the remote fgfs (do not read) */
	private boolean moving = false;
	
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
	public boolean youControl(float x, float y) {
		return x > this.x && x < (this.x + SWITCH_WIDTH) && x > this.y && y < this.y + (SWITCH_HEIGHT);
	}
	
	@Override
	public void onMove(float x, float y, boolean end) {
		if (end) {
			myLog.d(TAG, "Switching " + this.label);
			moving = true;
			this.state = !this.state;
		}
	}
	
	@Override
	public void postPlaneData(PlaneData pd) {
		FGFSConnection conn = pd.getConnection();
		
		// if not moving, just read from the remote connection
		if (!moving) {
			super.postPlaneData(pd);
			if (conn != null && !conn.isClosed()) {
				try {
					state = conn.getBoolean(this.prop);
				} catch (IOException e) {
					myLog.w(TAG, e.toString());
				}
			}
		} else {
			// if moving, post the state of the switch
			if (conn != null && !conn.isClosed()) {
				try {
					conn.setBoolean(this.prop, this.state);
					moving = false;
				} catch (IOException e) {
					myLog.w(TAG, e.toString());
				}
			}
		}
	}
	
	@Override
	public void onDraw(Canvas c, Bitmap b) {
		if (planeData == null) {
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

