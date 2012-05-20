package com.juanvvc.flightgear.instruments;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.juanvvc.flightgear.PlaneData;

/** Manages a layer of an instrument.
 * A layer of a instrument is usually an image.
 * It can be static or respond to some data, such as rotation or so.
 * Surfaces are stacked on an instrument.
 * @author juanvi
 *
 */
public abstract class Surface {
	/** Horizontal posiition inside the instrument. */
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
	 * @param x horizontal position insidet the in instrument
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
	
	/** Draws the surface.
	 * @param c The canvas where draw the surface on.
	 * @param b The bitmap of the surface. If must correspond to this.getFile().
	 * This bitmap is not managed directly in the surface to let some smart modifications, such as scaling.
	 * @param pd The current PlaneData object. */
	public abstract void onDraw(final Canvas c, final Bitmap b);
}
