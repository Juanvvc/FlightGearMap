package com.juanvvc.flightgear;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;

/** An overlay over a map to draw a plane. */
public class PlaneOverlay extends org.osmdroid.views.overlay.Overlay {
	private PlaneData planeData = null;
	private Bitmap bitmap = null;
	private GeoPoint location;

    public PlaneOverlay(Context ctx) {
    	super(ctx);
    	loadPlane(ctx, R.drawable.plane1);
    }
    
    public void loadPlane(Context ctx, int id) {
    	bitmap = BitmapFactory.decodeResource(ctx.getResources(),id);
    }
    
    /**
     * @param pd The position of the plane
     * @param l The position of the plane, as a GeoPoint. It can be null, and in this case is calculated internally.
     */
    public void setPlaneData(PlaneData pd, GeoPoint l) {
    	this.planeData = pd;
    	if ( l == null ) {
    		location = new GeoPoint(
    				(int)(planeData.getFloat(PlaneData.LATITUDE) * 1E6),
    				(int)(planeData.getFloat(PlaneData.LONGITUDE) * 1E6));
    	} else {
    		location = l;
    	}
    }

    @Override
    protected void draw(Canvas pC, MapView mapV, boolean shadow) {
        if (shadow || bitmap == null || planeData == null) {
            return;
        }
        Point locPoint = new Point();
        final Projection pj = mapV.getProjection();
        pj.toMapPixels(location, locPoint);
        
        Matrix m = new Matrix();
        m.setTranslate(locPoint.x - bitmap.getWidth() / 2, locPoint.y - bitmap.getHeight() / 2);
        m.postRotate(planeData.getFloat(PlaneData.HEADING_MOV), locPoint.x, locPoint.y);
        
        pC.drawBitmap(bitmap, m, null);
        pC.save();
    }
}