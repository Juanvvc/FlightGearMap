package com.juanvvc.flightgear;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

/** Manages Bitmaps, to prevent loading same images twice.
 * @author juanvi
 *
 */
public class BitmapProvider {
	/** key: image names, value: original bitmaps. */
	private Hashtable<String, Bitmap> bitmaps;
	/** key: image names, value: scaled bitmaps. */
	private Hashtable<String, Bitmap> scaledBitmaps;
	/** The context of the provider. */
	private Context context;
	/** Directory inside assets for low quality bitmaps. */
	public static final String LOW_QUALITY = "low";
	/** Directory inside assets for medium quality bitmaps. */
	public static final String MEDIUM_QUALITY = "medium";
	/** Directory inside assets for high quality bitmaps. */
	public static final String HIGH_QUALITY = "high";
	/** The scale of the bitmaps */
	private float scale = -1;
	
	/** Constructor.
	 * @param ctx The context of the provider.
	 */
	public BitmapProvider(final Context ctx) {
		bitmaps = new Hashtable<String, Bitmap>();
		scaledBitmaps = new Hashtable<String, Bitmap>();
		context = ctx;
	}
	
	/** Scale the currently available bitmaps.
	 * 
	 * @param scale scale=1 original size
	 */
	public void setScale(float s) {
		Matrix matrix = new Matrix();
		this.scale = s;
		matrix.setScale(scale,  scale);
		// remove from memory previous versions of the bitmaps
		for(Bitmap b: this.scaledBitmaps.values()) {
			b.recycle();
		}
		scaledBitmaps.clear();
		
		for (String imgName: bitmaps.keySet()) {
			Bitmap b = bitmaps.get(imgName);
			scaledBitmaps.put(imgName, Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true));
		}
	}
	
	public float getScale() {
		return scale;
	}
	
	/**
	 * Load a Bitmap and cache it.
	 * In memory constraint devices, high quality bitmaps cannot be loaded and OutOfMemoryError is triggered here.
	 * 
	 * @param dir The directory where search the Bitmap. One of LOW_QUALITY/MEDIUM_QUALITY/HIGH_QUALITY
	 * @param imgName The name of the image to load
	 * @return The Bitmap with that name.
	 */	
	public Bitmap getBitmap(String dir, String imgName) {
		if (!bitmaps.containsKey(imgName)) {
			AssetManager mng = context.getAssets();
			try {
				bitmaps.put(imgName, BitmapFactory.decodeStream(mng.open(dir + File.separator + imgName)));
			} catch (IOException e) {
				return null;
			}
		}
		return bitmaps.get(imgName);
	}
	
	/** Returns a scaled image. Call to setScale before this method.
	 * @param imgName The name of the image to return.
	 * @return The scaled version of the image.
	 */
	public Bitmap getScaledBitmap(String imgName) {
		if (scaledBitmaps.containsKey(imgName)) {
			return scaledBitmaps.get(imgName);
		}
		return null;
	}
	
	/** Recycle the Bitmaps and clear the inner arrays. */
	public void recycle() {
		for (Bitmap b: bitmaps.values()) {
			b.recycle();
		}
		for (Bitmap b: scaledBitmaps.values()) {
			b.recycle();
		}
		bitmaps.clear();
		scaledBitmaps.clear();
	}
}
