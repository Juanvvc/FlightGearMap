package com.juanvvc.flightgear.instruments;

import android.content.Context;
import android.graphics.Canvas;

import com.juanvvc.flightgear.BitmapProvider;
import com.juanvvc.flightgear.MyBitmap;
import com.juanvvc.flightgear.PlaneData;

/** A generic instrument.
 * This class manages the list of resources that instruments need, and
 * specifically two generic hands, and takes care of scaling images.
 * @author juanvi
 */
public class Instrument {
	/** Position of the instrument in the grid. Unscaled. */
	float col;
	/** Position of the instrument in the grid. Unscaled. */
	float row;
	/** Scale all positions with this value! */
	float scale;
	/** The context of the application. */
	Context context;
	/** Surfaces of this instrument */
	Surface[] surfaces;
	/** The grid are squares of gridSize x gridSize */
	protected int gridSize = 256;
	/** If true, the instrument is ready to be drawn.
	 * An instrument is ready when its bitmaps are loaded.
	 */
	boolean ready = false;
	/** The bitmap cache */
	private static BitmapProvider bProvider = null;
	
	/** Constructor. Call this constructor always from your extended classes!
	 * @param c The column of the instrument
	 * @param r The row of the instrument instrument
	 * @param c A reference to the context of the application
	 */
	public Instrument(float c, float r, Context ctx, Surface... surfaces) {
		this.col = c;
		this.row = r;
		context = ctx;
		scale = 1; // we begin unscaled
		setSurfaces(surfaces);
		ready = false;
	}
	
	public void setSurfaces(Surface[] ss) {
		for (Surface s: ss) {
			s.setParent(this);
		}
		surfaces = ss;
	}
	
	public float getCol() {
		return col;
	}
	
	public float getRow() {
		return row;
	}
	
	public static BitmapProvider getBitmapProvider(Context ctx) {
		if (bProvider == null) {
			bProvider = new BitmapProvider(ctx);
		}
		return bProvider;
	}
	
	/** Loads the images in imgFiles.
	 * @throws Exception If the images cannot be loaded.
	 */
	public void loadImages(String dir) throws Exception {
		for(Surface s: this.surfaces) {
			// ensures that the manager has loaded the image
			MyBitmap b = s.getBitmap();
			if (b != null) {
				bProvider.getBitmap(dir,  b.getFile());
			}
		}
		
		if (dir.equals(BitmapProvider.HIGH_QUALITY)) {
			this.gridSize = 512;
		} else if (dir.equals(BitmapProvider.MEDIUM_QUALITY)) {
			this.gridSize = 256;
		} else {
			this.gridSize = 128;
		}
		
		ready = true;
	}
	
	public float getGridSize() {
		return this.gridSize;
	}
	
	/**
	 * Get the surface of this instrument that controls a screen position.
	 * In no surface controls this position, returns null.
	 * 
	 * @param x The screen pixel position of the event
	 * @param y The screen pixel position of the event
	 * @return The surfaces that controls that position, or null.
	 */
	public Surface getControlingSurface(float x, float y) {
		// transform screen pixels into inner instrument position, in 512 scale
		float inX = getXtoInnerX(x);
		float inY = getYtoInnerY(y);
		
		for(Surface s: this.surfaces) {
			if (s.youControl(inX, inY)) {
				return s;
			}
		}
		return null;
	}
	
	
	public Surface[] getSurfaces() {
		return this.surfaces;
	}
	
	/**
	 * @param x The x position of a pixel on the screen
	 * @return The X position of this x, as an inner, 512 scale point
	 */
	public float getXtoInnerX(float x) {
		return 512f * (x / (this.gridSize * this.scale) - this.col);
	}

	/**
	 * @param x The y position of a pixel on the screen
	 * @return The y position of this x, as an inner, 512 scale point
	 */
	public float getYtoInnerY(float y) {
		return 512f * (y / (this.gridSize * this.scale) - this.row);
	}
	
	/** Sets the scale and loads the scaled images into the inner array. */
	public void setScale(float scale) {
		for (Surface s: surfaces) {
			MyBitmap b = s.getBitmap();
			if ( b!=null && b.getScaledBitmap() != null) {
				b.getScaledBitmap().recycle();
			}
		}
		
		this.scale = scale;
		// Update all bitmaps
		for(Surface s: surfaces) {
			MyBitmap b = s.getBitmap();
			if (b != null) {
				b.updateBitmap(bProvider, this.gridSize);
			}
			// Inform the surfaces that the bitmap has changed
			s.onBitmapChanged();
		}
	}
	
	/** @return The scale of the instrument. */
	public float getScale() {
		return scale;
	}
	
	/** Get a new PlaneData object.
	 * Some surfaces may call pd.getConnection().get(), that uses the
	 * network and then it shouldn't be on the main thread.
	 * @param pd The last PlaneData object */
	public void postPlaneData(PlaneData pd) {
		for (Surface s: surfaces) {
			s.postPlaneData(pd);
		}
	}
	
	/** Draw the instrument on the canvas.
	 * 
	 * @param c The current Canvas
	 * @param pd The current value of the plane information
	 */
	public void onDraw(Canvas c) {
		for (int i = 0; i < surfaces.length; i++) {
			Surface s = surfaces[i];
			if (s != null) {
				// we call onDraw() even if b==null. Maybe the surface is creating its own bitmap
				s.onDraw(c);
			}
		}
	}
}