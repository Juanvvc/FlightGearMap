package com.juanvvc.flightgear.instruments;

import java.io.IOException;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.juanvvc.flightgear.FGFSConnection;
import com.juanvvc.flightgear.MyBitmap;
import com.juanvvc.flightgear.MyLog;

/** A surface like a key for the magnetos and started, as the one in may aircrafts. Based on SwitchSurface */
public class MagnetosStarterSurface extends Surface {
	private String propMagnetos;
	private String propStarter;
	private static final int SWITCH_SIZE = 120;
	int value; // 0=none, 1=right magnet, 2=left magnet, 3=both, 4=start
	private String label;
	private float textX, textY;
	private Paint textPaint;
	private boolean firstRead;
	/** ration center */
	protected float rcx;
	protected float rcy;
	// The final position of the surface, scale and gridsize considered (calculated in onBitmapChanged())
	private float finalx, finaly;
	// The final position of the rotation center, scale and gridsize considered (calculated in onBitmapChanged())
	private float finalrx, finalry;
	private Matrix m;
	
	/** If true, the switch needs to be post to the remote fgfs (do not read) */
	private boolean dirty = false;

	
	public MagnetosStarterSurface(MyBitmap bitmap, final float x, final float y, final float rcX, final float rcY, final String label, final String propMagnetos, final String propStarter) {
		super(bitmap, x, y);
		this.rcx = rcX;
		this.rcy = rcY;
		this.label = label;
		this.propMagnetos = propMagnetos;
		this.propStarter = propStarter;
		m = new Matrix();
		
		if (label != null) {
			textPaint = new Paint();
			textPaint.setColor(0xffffffff);
		}
		
		firstRead = true;
		dirty = false;
	}
	
	@Override
	public boolean youControl(final float x, final float y) {
		return x > rcx-SWITCH_SIZE && x < rcx+SWITCH_SIZE && y > rcy-SWITCH_SIZE && y < rcy+SWITCH_SIZE;
	}
	
	@Override
	public void onMove(final float x, final float y, final boolean end) {
		if (end) {
			if ( x < DEFAULT_SURFACE_SIZE / 2 && value > 0) {
				value -= 1;
				dirty = true;
			} else if ( x > DEFAULT_SURFACE_SIZE / 2 && value < 4) {
				value += 1;
				dirty = true;
			}
			if (value == 4) {
				value = 3;
				dirty = true;
			}
		} else {
			if (value == 4) {
				dirty = true;
			} else if ( x > DEFAULT_SURFACE_SIZE / 2 && value == 3) {
				value = 4;
				dirty = true;
			}
		}
	}
	
	@Override
	public void onBitmapChanged() {
		final float realscale = parent.getScale() * parent.getGridSize();
		final float col = parent.getCol();
		final float row = parent.getRow();
		finalx = (col + relx ) * realscale;
		finaly = (row + rely ) * realscale;
		finalrx = (col + rcx / 512f ) * realscale;
		finalry = (row + rcy / 512f ) * realscale;
	}

	@Override
	public void onDraw(Canvas c) {
		if (planeData == null || !planeData.hasData() || bitmap == null || bitmap.getScaledBitmap() == null) {
			return;
		}
		
		// draw the label
		if (label != null) {
			c.drawText(label, textX, textY, textPaint);
		}
		
		float rotation = 0;
		switch (value) {
		case 0: rotation = -20; break;
		case 1: rotation = 10; break;
		case 2: rotation = 45; break;
		case 3: rotation = 80; break;
		case 4: rotation = 120; break;
		}
		
		m.reset();
		m.setTranslate(finalx, finaly);
		m.postRotate(rotation, finalrx, finalry);
		c.drawBitmap(bitmap.getScaledBitmap(), m, null);
	}
	
	@Override
	public void postCalibratableSurfaceManager(CalibratableSurfaceManager cs) {
		cs.register(this);
	}

	@Override
	public boolean isDirty() {
		return dirty || firstRead;
	}

	@Override
	public void update(FGFSConnection conn) throws IOException {
		if (conn == null || conn.isClosed()) {
			return;
		}

		if (!dirty || firstRead) {
			if (conn.getBoolean(this.propStarter)) {
				value = 4;
			} else {
				value = conn.getInt(this.propMagnetos, 0);
			}
			// TODO: reading the state from the remote fgfs is SLOW.
			// We only read once after creating the switch.
			// So, if the user changes the state in the remote fgfs, we will never find out.
			firstRead = false;
		} else {
			// if dirty, post the state of the switch
			if (value < 4) {
				conn.setInt(this.propMagnetos, value);
				conn.setBoolean(this.propStarter, false);
			} else {
				conn.setInt(this.propMagnetos, 3);
				conn.setBoolean(this.propStarter, true);
				MyLog.d(this, "Starter");
			}
			dirty = false;
		}
	}
}
