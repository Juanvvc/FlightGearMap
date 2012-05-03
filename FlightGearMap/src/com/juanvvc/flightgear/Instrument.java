package com.juanvvc.flightgear;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

/** A generic instrument.
 * This class manages the list of resources that instruments need, and
 * specifically two generic hands, and takes care of scaling images.
 * @author juanvi
 */
public abstract class Instrument {
	/** Position of the instrument in the grid. Unscaled. */
	float col;
	/** Position of the instrument in the grid. Unscaled. */
	float row;
	/** Scale all positions with this value! */
	float scale;
	/** The context of the application. */
	Context context;
	/** Name of the images to load, inside the assets directory (different subdirectories according to quality) */
	ArrayList<String> imgFiles;
	/** The scaled bitmaps of your instruments. */
	ArrayList<Bitmap> imgsScaled;
	
	/** The grid are squares of gridSize x gridSize */
	static int gridSize = 256;
	/** If true, the instrument is ready to be drawn.
	 * An instrument is ready when its bitmaps are loaded.
	 */
	boolean ready = false;
	private static BitmapProvider bProvider = null;
	
	/** Constructor. Call this constructor always from your extended classes!
	 * @param c The column of the instrument
	 * @param r The row of the instrument instrument
	 * @param c A reference to the context of the application
	 */
	public Instrument(float c, float r, Context ctx) {
		this.col = c;
		this.row = r;
		context = ctx;
		scale = 1; // we begin unscaled
		imgFiles = new ArrayList<String>();
		imgsScaled = new ArrayList<Bitmap>();
		ready = false;
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
		// we know that size of the images checking the dir name. This is magic
		if (dir.equals(BitmapProvider.MEDIUM_QUALITY)) {
			setGridSize(256); // medium are 256x256 images
		} else if (dir.equals(BitmapProvider.HIGH_QUALITY)) {
			// TODO: these bitmaps are not included in the current version to save space
			// you can find them in assets.high.zip
			setGridSize(512); //high are 512x512 images 
		} else {
			setGridSize(128); // low are 128x128 images
		}
		
		for(String f: this.imgFiles) {
			// ensures that the manager has loaded the image
			bProvider.getBitmap(dir,  f);
		}
		
		ready = true;
	}
	
	/** Sets the scale and loads the scaled images into the inner array. */
	public void setScale(float scale) {
		this.scale = scale;
		for(String f: imgFiles) {
			 Bitmap b = bProvider.getScaledBitmap(f);
			 imgsScaled.add(b);
		}
	}
	
	/** Draw the instrument on the canvas.
	 * 
	 * @param c The current Canvas
	 * @param pd The current value of the plane information
	 */
	public abstract void onDraw(Canvas c, PlaneData pd);
	
	/** @return The size of a unit in the grid, according to the available bitmaps.
	 * The size of a clock is the final size of a full column/row in pixels.
	 */
	public static int getGridSize() {
		return gridSize;
	}
	
	/** @param s
	 * 		The size of a grid unit, according to the available bitmaps.
	 */
	private static void setGridSize(int s) {
		gridSize = s;
	}
	
	/** @return The horizontal center of the default hand. */
	public int getHandCenterX() {
		// the center of the handle is 20x200 in the bitmaps of size 512
		return 20 * gridSize / 512;
	}
	
	/** @return The vertical center of the default hand. */
	public int getHandCenterY() {
		// the center of the handle is 20x200 in the bitmaps of size 512
		return 200 * gridSize / 512;
	}
}

class Altimeter extends Instrument {
	public Altimeter(float x, float y, Context c) {
		super(x, y, c);
		// load the background of the altimeter, in addition to the hands
		// remember: the background is gong to be at position 2
		this.imgFiles.add("hand1.png");
		this.imgFiles.add("hand2.png");
		this.imgFiles.add("alt1.png");
	}

	@Override
	public void onDraw(Canvas c, PlaneData pd) {
		if (! ready) {
			return;
		}
		Matrix matrix = new Matrix();
		matrix.setTranslate(col * gridSize * scale, row * gridSize * scale);
		// remember: the background is gong to be at position 2, since hands are at 0 and 1
		c.drawBitmap(imgsScaled.get(2), matrix, null);

		float altitude = pd.getFloat(PlaneData.ALTITUDE);
		double alt2Angle = ((altitude / 1000) * 360 / 10);
		matrix.reset();
		matrix.postTranslate(((0.5f + col) * gridSize - getHandCenterX()) * scale, ((0.5f + row) * gridSize - getHandCenterY()) * scale);
		matrix.postRotate((float) alt2Angle, ( (0.5f + col) * gridSize ) * scale, ((0.5f + row) * gridSize) * scale);
		c.drawBitmap(imgsScaled.get(1), matrix, null);
		double alt1Angle = ((altitude % 1000) * 360 / 1000);
		matrix.reset();
		matrix.postTranslate(( (0.5f + col) * gridSize - getHandCenterX()) * scale, ((0.5f + row) * gridSize - getHandCenterY()) * scale);
		matrix.postRotate((float) alt1Angle, ((0.5f + col) * gridSize ) * scale, ((0.5f + row) * gridSize) * scale);
		c.drawBitmap(imgsScaled.get(0), matrix, null);			
	}
}

class Attitude extends Instrument {
	public Attitude(float x, float y, Context c) {
		super(x, y, c);
		this.imgFiles.add("ati0.png");
		this.imgFiles.add("ati1.png");
		this.imgFiles.add("ati2.png");
		this.imgFiles.add("ati3.png");
	}
	@Override
	public void onDraw(Canvas c, PlaneData pd) {
		if (! ready) {
			return;
		}
		Matrix matrix = new Matrix();
		matrix.setTranslate(col * gridSize * scale, row * gridSize * scale);
		c.drawBitmap(imgsScaled.get(0), matrix, null);
		
		Bitmap ati1Scaled = imgsScaled.get(1);
		Bitmap ati2Scaled = imgsScaled.get(2);
		
		
		// draw pitch
		matrix.reset();
		// translate 23 /  pixels each 5 degrees
		float roll = pd.getFloat(PlaneData.ROLL);
		matrix.postTranslate(((0.5f + col) * gridSize) * scale - ati1Scaled.getWidth() / 2, ((0.5f + row) * gridSize + pd.getFloat(PlaneData.PITCH) * (23 * gridSize/ 512) / 5) * scale - ati1Scaled.getHeight() / 2);
		matrix.postRotate(-roll, ((0.5f + col) * gridSize) * scale, ((0.5f + row) * gridSize) * scale);
		c.drawBitmap(ati1Scaled, matrix, null);
		// draw roll
		matrix.reset();
		matrix.postTranslate(((0.5f + col) * gridSize) * scale - ati2Scaled.getWidth() / 2, ((0.5f + row) * gridSize) * scale  - ati2Scaled.getHeight() / 2);
		matrix.postRotate(-roll, ((0.5f + col) * gridSize) * scale, ((0.5f + row) * gridSize) * scale);
		c.drawBitmap(ati2Scaled, matrix, null);
		
		matrix.reset();
		matrix.setTranslate(col * gridSize * scale, row * gridSize * scale);
		c.drawBitmap(imgsScaled.get(3), matrix, null);
	}
}

class TurnSlip extends Instrument {
	public TurnSlip(float x, float y, Context c) {
		super(x, y, c);
		this.imgFiles.add("trn0.png");
		this.imgFiles.add("trn1.png");
		this.imgFiles.add("slip.png");
		this.imgFiles.add("turn.png");
	}
	@Override
	public void onDraw(Canvas c, PlaneData pd) {
		if (! ready) {
			return;
		}
		Matrix matrix = new Matrix();
		matrix.setTranslate(col * gridSize * scale, row * gridSize * scale);
		c.drawBitmap(imgsScaled.get(0), matrix, null);
		
		Bitmap slipScaled = imgsScaled.get(2);
		
		// draw slip skid
		matrix.reset();
		// (0.7*SEMICLOSEWIDTH is calibrated with on screen instruments with FG sim)
		matrix.setTranslate(((0.5f + col - 0.7f * pd.getFloat(PlaneData.SLIP)) * gridSize) * scale - slipScaled.getWidth() / 2 , ((row + 0.65f) * gridSize) * scale  - slipScaled.getHeight() / 2);
		c.drawBitmap(slipScaled, matrix, null);
		
		matrix.reset();
		matrix.setTranslate(col * gridSize * scale, row * gridSize * scale);
		c.drawBitmap(imgsScaled.get(1), matrix, null);
		
		Bitmap turnScaled = imgsScaled.get(3);
		
		// turn rate
		// 20º means a turn rate = 1 turn each 2 minuts
		double turnAngle = (pd.getFloat(PlaneData.TURN_RATE) * 20);
		matrix.reset();
		matrix.postTranslate(((0.5f + col) * gridSize) * scale  - turnScaled.getWidth() / 2, ((0.5f + row) * gridSize) * scale  - turnScaled.getHeight() / 2);
		matrix.postRotate((float) turnAngle, ((0.5f + col) * gridSize) * scale, ((0.5f + row) * gridSize) * scale);
		c.drawBitmap(turnScaled, matrix, null);
	}
}

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

class OneHandInstrument extends Instrument {
	private int handle;
	private int handleID;
	private int handleX;
	private int handleY;
	private int handleCX;
	private int handleCY;
	private float min;
	private float minAngle;
	private float max;
	private float maxAngle;
	private int rotate;
	private int rotateID;
	public OneHandInstrument(
			float x, float y, Context c,
			String[] files,
			int handle, int handleID,
			int handleX, int handleY,
			int handleCX, int handleCY,
			float min, float minAngle, float max, float maxAngle,
			int rotate, int rotateID) {
		super(x, y, c);
		this.handle = handle;
		this.handleID = handleID;
		this.handleCX = handleCX;
		this.handleCY = handleCY;
		this.handleX = handleX;
		this.handleY = handleY;
		this.min = min;
		this.minAngle = minAngle;
		this.max = max;
		this.maxAngle = maxAngle;
		this.rotate = rotate;
		this.rotateID = rotateID;
		for (int i=0; i<files.length; i++) {
			this.imgFiles.add(files[i]);
		}
	}
	
	@Override
	public void onDraw(Canvas c, PlaneData pd) {
		Matrix m = new Matrix();
		for(int i = 0; i < this.imgsScaled.size(); i++) {
			Bitmap b = this.imgsScaled.get(i);
			m.reset();
			if (i == handle) {
				float v = pd.getFloat(handleID);
				if (v < min) {
					v = min;
				} else if (v > max) {
					v = max;
				}
				float angle = (v - min) * (maxAngle - minAngle) / (max - min) + minAngle;
				m.setTranslate((col * gridSize + (handleCX - handleX) * gridSize / 512) * scale, (row * gridSize + (handleCY- handleY) * gridSize / 512) * scale);
				m.postRotate(angle, (col * gridSize + handleCX * gridSize / 512) * scale, (row * gridSize + handleCY * gridSize / 512) * scale);
				c.drawBitmap(b, m, null);
			} else if (i == rotate) {
				m.setTranslate((col + 0.5f) * gridSize * scale - b.getWidth() / 2, (row + 0.5f) * gridSize * scale - b.getHeight() / 2);
				m.postRotate(pd.getFloat(rotateID), (col + 0.5f) * gridSize * scale, (row + 0.5f) * gridSize * scale);
				c.drawBitmap(b, m, null);
			} else {
				m.setTranslate(col * gridSize * scale, row * gridSize * scale);
				c.drawBitmap(b, m, null);
			}
		}
	}
}