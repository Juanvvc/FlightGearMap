package com.juanvvc.flightgear;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

/** For some reason, you need one of this if you put markers on a map.
 * TODO: try to understand really what this class does
 * @author juanvi
 *
 */
public class PlaneMovementOverlay extends ItemizedOverlay<OverlayItem> {
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context context;

	public PlaneMovementOverlay(Drawable plane, Context c) {
		super(boundCenterBottom(plane));
		this.context = c;
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}
	
	public void addOverlay(OverlayItem overlay, float bearing) {
		// remember: if you do not call to boundCenter, the drawable is not shown.
		// afaik, this is not documented!
		overlay.setMarker(boundCenter(this.rotateDrawable(bearing)));
	    mOverlays.add(overlay);
	    this.populate();
	}
	
	public void clear() {
		mOverlays.clear();
		populate();
	}
	
	/** param angle The angle of rotation
	 * @return A rotated version of R.drawable.plane3
	 */
	public Drawable rotateDrawable(final float angle)	{
		Bitmap b = BitmapFactory.decodeResource(context.getResources(), R.drawable.plane3);
		Matrix mat = new Matrix();
		mat.postRotate(angle);
		Bitmap bRotated = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), mat, true);
		return new BitmapDrawable(context.getResources(), bRotated);
	}
}
