package com.juanvvc.flightgear.instruments;

import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.juanvvc.flightgear.FGFSConnection;
import com.juanvvc.flightgear.myLog;

/** A surface that is rotated according to the telnet connection, and can be calibrated. */
public class CalibratableRotateSurface extends Surface {
	private Matrix m;
	/** A scale factor to apply to the property */
	private float rscale;
	/** The property to read from the remote fgfs */
	private String prop;
	/** If true, the value is wrapped (after max, it is min again) */
	private boolean wrapped = false;
	/** The property to read from PlaneData, if positive. */
	private int propIdx;
	/** ration center */
	private float rcx;
	private float rcy;
	/** min value, and its angle. */
	private float min, amin;
	/** max value, and its angle. */
	private float max, amax;
	
	private float value = 0;
	private boolean moving = false;
	private boolean dirtyValue = false;
//	private float angle_start = 0;
//	private float angle_moved = 0;
	private float lastx, lasty;
	
	private static final int TOUCHABLE_WIDTH = 256;
	private static final float ROTATION_SCALE = 0.3f;
	
	private boolean firstRead = true;
	
	/**
	 * @param file The file of the image (does not include directory)
	 * @param x Horizontal position of the surface inside the instrument
	 * @param y Horizontal position of the surface inside the instrument
	 * @param prop The path to the property to read from the remote FGFSConnetion
	 * @param rscale Scale of the data (usually 1: do not modify the data)
	 * @param rcx Rotation center (inside the instrument)
	 * @param rcy Rotation center (inside the instrument)
	 * @param min Minimum value of the data
	 * @param amin Angle that corresponds to the minimum value
	 * @param max Max value of the data
	 * @param amax Angle that corresponds to the max value.
	 */
	public CalibratableRotateSurface(
			String file, float x, float y,
			String prop, float rscale, boolean wrap,
			int propIdx,
			int rcx, int rcy,
			float min, float amin, float max, float amax) {
		super(file, x, y);
		m = new Matrix();
		this.rcx = rcx;
		this.rcy = rcy;
		this.min = min;
		this.amin = amin;
		this.max = max;
		this.amax = amax;
		this.wrapped = wrap;
		this.propIdx = propIdx;
		this.rscale = rscale;
		this.prop = prop;
	}
	
	/**
	 * @param v value angle
	 * @return The angle to rotate the drawable to match that value
	 */
	protected float getDrawableRotationAngle(float v) {
		if (v < min) {
			v = min;
		} else if (v > max) {
			v = max;
		}
		return (v - min) * (amax - amin) / (max - min) + amin;
	}

	@Override
	public void onDraw(Canvas c, Bitmap b) {
		
		if (!this.dirtyValue && this.propIdx > -1 && this.planeData != null && planeData.hasData()) {
			value = planeData.getFloat(this.propIdx);
		}
		
		m.reset();
		final float gridSize = parent.getGridSize();
		final float scale = parent.getScale();
		final float col = parent.getCol();
		final float row = parent.getRow();
		m.setTranslate(
				(col + x / 512f ) * gridSize * scale,
				(row + y / 512f ) * gridSize * scale);
		m.postRotate(
				getDrawableRotationAngle(value),
				(col + rcx / 512f ) * gridSize * scale,
				(row + rcy / 512f ) * gridSize * scale);
		c.drawBitmap(b, m, null);
	}
	
	@Override
	public boolean youControl(float x, float y) {
		// note that the user must rotate the movement centered on the rotation point.
		// In most instruments, this is different from the real calibration wheel, but I think
		// that this way it is much more comfortable
		return Math.abs(x - this.rcx) < TOUCHABLE_WIDTH && Math.abs(y - this.rcy) < TOUCHABLE_WIDTH;
	}
	
	@Override
	public void onMove(float x, float y, boolean end) {
		if (end && !moving) {
			return;
		}
		
		float a1, a2, da;

		
		if (!moving) {
			moving = true;
//			angle_start = (float)Math.atan2(x - rcx, y - rcy);
			lastx = x;
			lasty = y;
		} else {
			a1 = (float)Math.atan2(lastx - rcx, lasty - rcy);
			a2 = (float)Math.atan2(x - rcx, y - rcy);
			da = a2 - a1;
			float rotateAngle = 0; //value; //this.getDrawableRotationAngle(value);
			if (Math.abs(da) < 1) { // do not consider the discontinuity in 0-360. This is a small error. Noticeable? Don't think so.
				//rotateAngle += (da * 180 / Math.PI) * ROTATION_SCALE;
				rotateAngle += (da * 180 / Math.PI);
			}
			lastx = x;
			lasty = y;
			if (end) {
				moving = false;
			}
			
//			// set rotationAngle in (0, 360)
//			while (rotateAngle < 0) {
//				rotateAngle += 360;
//			}
//			while (rotateAngle > 360) {
//				rotateAngle -= 360;
//			}
//			
//			value = rotateAngle; //(rotateAngle - amin) * (max - min) / (amax - amin) + min;
//			myLog.i(this, "Setting " + rotateAngle + ": " + value);
			
			value += rotateAngle * ROTATION_SCALE * (this.max - this.min) / 360;
			
			if (value > max) {
				if (this.wrapped) {
					value = min + (value - max);
				} else {
					value = max;
				}
			} else if (value < min) {
				if (this.wrapped) {
					value = max - Math.abs(min - value);
				} else {
					value = min;
				}
			}
			
			this.dirtyValue = true;
		}
	}
	
	@Override
	public void postCalibratableSurfaceManager(CalibratableSurfaceManager cs) {
		cs.register(this);
		firstRead = true;
	}


	@Override
	public boolean isDirty() {
		return dirtyValue || firstRead;
	}

	@Override
	public void update(FGFSConnection conn) throws IOException {
		if (conn == null || conn.isClosed()) {
			return;
		}
		
		if (this.prop == null && dirtyValue) {
			// if prop is null, we cannot send/receive our value to the server. We are done.
			dirtyValue = false;
			return;
		}
		
		// if our value is not dirty, read from the remote fgfs
		if (!dirtyValue || firstRead) {
			try {
				value = conn.getFloat(prop);
				firstRead = false;
			} catch (NumberFormatException e) {
				myLog.e(this, prop + ": " + e.toString());
			}
		} else {
			// if dirty, push the value
			conn.setFloat(prop, value);
			dirtyValue = false;
		}
	}
}
