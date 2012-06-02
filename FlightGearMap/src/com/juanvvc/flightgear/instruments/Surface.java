package com.juanvvc.flightgear.instruments;

import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.juanvvc.flightgear.FGFSConnection;
import com.juanvvc.flightgear.PlaneData;

/** Manages a layer of an instrument.
 * A layer of a instrument is usually an image.
 * It can be static or respond to some data, such as rotation or so.
 * Surfaces are stacked on an instrument.
 * @author juanvi
 *
 */
public abstract class Surface {
	/** Horizontal position inside the instrument. */
	protected float x;
	/** Vertical position inside the instrument. */
	protected float y;
	/** The name of the image file of this surface (does not include the directory) */
	protected String file;
	/** The parent instrument for this surface. */
	protected Instrument parent;
	/** The last PlaneData object. */
	protected PlaneData planeData;
	
	/**
	 * @param file The name of the file to load (does not include the directory)
	 * @param x horizontal position inside the instrument
	 * @param y vertical position inside the instrument
	 */
	public Surface(final String file, final float x, final float y) {
		this.file = file;
		this.x = x;
		this.y = y;
	}
	
	public void setParent(final Instrument ins) {
		this.parent = ins;
	}
	
	public Instrument getParent() {
		return parent;
	}
	
	public String getFile() {
		return file;
	}
	
	/** @param pd The last PlaneData object */
	public void postPlaneData(PlaneData pd) {
		this.planeData = pd;
	}
	
	/**
	 * Check if this surface controls a point to change its value.
	 * If this method returns true, the system will inform the surface abouot any movement
	 * from this one to the end of the movement. Use to calibrate surfaces on touchable screens.
	 * 
	 * @param x The x position of an event from the user inside the instrument, in 512 scale
	 * @param y The y position of an event from the user inside the instrument, in 512 scale
	 * @return True if this surface controls an event on point (x, y). Currently,
	 * this method returns always false ("we do not manage movements"). Override to do
	 * something useful. 
	 */
	public boolean youControl(float x, float y) {
		return false;
	}
	
	
	/** Gets a movement event from the user.
	 * Currently, this method does nothing. To do something useful, override this method
	 * and return true in youControl(x,y).
	 * @param x The x position of an event from the user inside the instrument, in 512 scale
	 * @param y The y position of an event from the user inside the instrument, in 512 scale
	 * @param end If true, the movement has ended. It is up to the class to detect starting movements.
	 */
	public void onMove(float x, float y, boolean end) {
		// Does nothing
	}
	
	
	/** Draws the surface.
	 * @param c The canvas where draw the surface on.
	 * @param b The bitmap of the surface. If must correspond to this.getFile().
	 * This bitmap is not managed directly in the surface to let some smart modifications, such as scaling.
	 * @param pd The current PlaneData object. */
	public abstract void onDraw(final Canvas c, final Bitmap b);
	
	/** Called when the instrument has been moved on the screen. */
	public void onMove() {
		
	}
	
	public void postCalibratableSurfaceManager(CalibratableSurfaceManager cs) {
		
	}
	
	public boolean isDirty() {
		return false;
	}
	
	public void update(FGFSConnection conn) throws IOException {
		
	}
}
