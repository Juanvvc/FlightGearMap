package com.juanvvc.flightgear.instruments;

import java.util.ArrayList;

import com.juanvvc.flightgear.BitmapProvider;
import com.juanvvc.flightgear.PlaneData;
import com.juanvvc.flightgear.myLog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

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
	/** The scaled bitmaps of your instruments. */
	ArrayList<Bitmap> imgsScaled;
	/** The grid are squares of gridSize x gridSize */
	protected int gridSize = 256;
	/** If true, the instrument is ready to be drawn.
	 * An instrument is ready when its bitmaps are loaded.
	 */
	boolean ready = false;
	/** The bitmap cache */
	private static BitmapProvider bProvider = null;
	
	private static final String TAG = "Instrumet";
	
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
		imgsScaled = new ArrayList<Bitmap>();
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
			bProvider.getBitmap(dir,  s.getFile());
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
	
	/** Sets the scale and loads the scaled images into the inner array. */
	public void setScale(float scale) {
		for (Bitmap b: imgsScaled) {
			b.recycle();
		}
		imgsScaled.clear();
		
		this.scale = scale;
		for(Surface s: surfaces) {
			String f = s.getFile();
			Bitmap b = bProvider.getScaledBitmap(f);
			if (b == null) {
				myLog.w(TAG, "Null bitmap: " + f + ". Image not found?");
			}
			// even if null, add the bitmap to respect position
			imgsScaled.add(b);
		}
	}
	
	/** @return The scale of the instrument. */
	public float getScale() {
		return scale;
	}
	
	/** Draw the instrument on the canvas.
	 * 
	 * @param c The current Canvas
	 * @param pd The current value of the plane information
	 */
	public void onDraw(Canvas c, PlaneData pd) {
		if (!ready) {
			return;
		}
		for (int i = 0; i < surfaces.length; i++) {
			Surface s = surfaces[i];
			if (s != null) {
				Bitmap b = imgsScaled.get(i);
				try {
					s.onDraw(c, b, pd);
				} catch (NullPointerException e) {
					myLog.w(TAG, myLog.stackToString(e));
				}
			}
		}
	}
}

/*
class Fuel extends Instrument {
	public Fuel(float x, float y, Context c) {
		super(x, y, c);
		this.imgFiles.add("hand3.png");
		this.imgFiles.add("fuel1.png");
	}
	
	public void onDraw(Canvas c, PlaneData pd) {
		Matrix matrix = new Matrix();
		matrix.setTranslate(col * gridSize * scale, row * gridSize * scale);
		c.drawBitmap(this.imgsScaled.get(1), matrix, null);
		
		// note: in the 256x256 bitmaps, centers are at (18, 115) and (137, 115)
		
		// left fuel
		// 0 gals = 160º, 26gals = 20º
		float ang = 160 - pd.getFloat(PlaneData.FUEL1) * 140 / 26;
		if (ang < 10 ) {
			ang = 10;
		}
		matrix.reset();
		matrix.setTranslate(((col + 18.0f/256) * gridSize - this.getHandCenterX()) * scale, ((row + 115.0f/256) * gridSize - this.getHandCenterY()) * scale);
		matrix.postRotate(ang, (col + 18.0f/256) * gridSize * scale, (row + 115.0f/256) * gridSize * scale);
		c.drawBitmap(this.imgsScaled.get(0), matrix, null);
		
		// right fuel
		// 0 gals = -160º, 26gals = -20º
		ang = -160 + pd.getFloat(PlaneData.FUEL2) * 140 / 26;
		if (ang > -10 ) {
			ang = -10;
		}
		matrix.reset();
		matrix.setTranslate(((col + 137.0f/256) * gridSize - this.getHandCenterX()) * scale, ((row + 115.0f/256) * gridSize - this.getHandCenterY()) * scale);
		matrix.postRotate(ang, (col + 137.0f/256) * gridSize * scale, (row + 115.0f/256) * gridSize * scale);
		c.drawBitmap(this.imgsScaled.get(0), matrix, null);

	}
}

class Oil extends Instrument {
	public Oil(float x, float y, Context c) {
		super(x, y, c);
		this.imgFiles.add("hand3.png");
		this.imgFiles.add("oil1.png");
	}
	
	public void onDraw(Canvas c, PlaneData pd) {
		Matrix matrix = new Matrix();
		matrix.setTranslate(col * gridSize * scale, row * gridSize * scale);
		c.drawBitmap(this.imgsScaled.get(1), matrix, null);
		
		// note: in the 256x256 bitmaps, centers are at (18, 115) and (137, 115)
		
		// oil temperature
		// 75 = 165º, 245 = 15º
		float ang = 165 - (pd.getFloat(PlaneData.OIL_TEMP) - 75) * 150 / 170;
		if (ang < 10 ) {
			ang = 10;
		}
		if (ang > 170) {
			ang =170;
		}
		matrix.reset();
		matrix.setTranslate(((col + 18.0f/256) * gridSize - this.getHandCenterX()) * scale, ((row + 115.0f/256) * gridSize - this.getHandCenterY()) * scale);
		matrix.postRotate(ang, (col + 18.0f/256) * gridSize * scale, (row + 115.0f/256) * gridSize * scale);
		c.drawBitmap(this.imgsScaled.get(0), matrix, null);
		
		// oil press
		// 0 gals = -160º, 26gals = -20º
		ang = -165 + pd.getFloat(PlaneData.OIL_PRESS) * 150 / 115;
		if (ang > -10 ) {
			ang = -10;
		}
		matrix.reset();
		matrix.setTranslate(((col + 137.0f/256) * gridSize - this.getHandCenterX()) * scale, ((row + 115.0f/256) * gridSize - this.getHandCenterY()) * scale);
		matrix.postRotate(ang, (col + 137.0f/256) * gridSize * scale, (row + 115.0f/256) * gridSize * scale);
		c.drawBitmap(this.imgsScaled.get(0), matrix, null);

	}
}

class Nav extends Instrument {
	public Nav(float x, float y, Context c) {
		super(x, y, c);
		this.imgFiles.add("nav1.png");
		this.imgFiles.add("nav2.png");
		this.imgFiles.add("nav3.png");
	}
	
	public void onDraw(Canvas c, PlaneData pd) {
		Matrix m = new Matrix();
		m.setTranslate(col * gridSize * scale, row * gridSize * scale);
		c.drawBitmap(imgsScaled.get(0), m, null);
		c.drawBitmap(imgsScaled.get(1), m, null);
		c.drawBitmap(imgsScaled.get(2), m, null);
		
	}
}
*/