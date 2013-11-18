package com.juanvvc.flightgear.maps;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;

import com.juanvvc.flightgear.R;
import com.juanvvc.flightgear.R.drawable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.view.MotionEvent;
import android.widget.Toast;

/** An overlay over a map to draw a plane. */
public class MapOverlay extends org.osmdroid.views.overlay.Overlay {
	private Bitmap bitmap = null;
	private GeoPoint location = null;
	private float heading = 0;
	private String text = null;
	private Paint txtPaint = null;
	private String description = null;

    public MapOverlay(Context ctx) {
    	super(ctx);
    	loadIcon(ctx, R.drawable.plane1);
    }
    
    public void loadIcon(Context ctx, int id) {
    	bitmap = BitmapFactory.decodeResource(ctx.getResources(),id);
    }
    
    /**
     * @param l The position of the overlay
     * @param h The heading of the overlay
     */
    public void setPosition(GeoPoint l, float h) {
   		location = l;
   		heading = h;
    }
    
    public void setText(String t) {
    	txtPaint = new Paint();
    	txtPaint.setColor(Color.WHITE);
    	txtPaint.setTextAlign(Align.CENTER);
    	txtPaint.setTextSize(16);
    	txtPaint.setShadowLayer(5, 0, 0, Color.BLACK);
    	text = t;
    }
    
    public void setDescription(String desc) {
    	description = desc;
    }
    
	@Override
	public boolean onDown(MotionEvent e, MapView mapView) {
		if (description != null) {
	        Point locPoint = new Point();
	        final Projection pj = mapView.getProjection();
	        pj.toMapPixels(location, locPoint);
	        
	        if (e.getX() > locPoint.x - bitmap.getWidth() / 2 && e.getX() < locPoint.x + bitmap.getWidth() / 2 ) {
	        	if (e.getY() > locPoint.y - bitmap.getHeight() / 2 && e.getY() < locPoint.y + bitmap.getHeight() / 2 ) {
	        		Toast.makeText(mapView.getContext(), description, Toast.LENGTH_LONG).show();
	        		return true;
	        	}
	        }
		}
		return false;
	}
	
	public static int nmToRadius(float nm, MapView map, double latitude) {
		Projection proj = map.getProjection();
		float meters = (float) (nm * Projection.METERS_PER_NAUTICAL_MILE);
		return (int) (proj.metersToEquatorPixels(meters) * (1 / Math.cos(Math.toRadians(latitude))));
	}

    @Override
    protected void draw(Canvas pC, MapView mapV, boolean shadow) {
        if (shadow || bitmap == null || location == null) {
            return;
        }
        Point locPoint = new Point();
        final Projection pj = mapV.getProjection();
        pj.toMapPixels(location, locPoint);
        
        Matrix m = new Matrix();
        m.setTranslate(locPoint.x - bitmap.getWidth() / 2, locPoint.y - bitmap.getHeight() / 2);
        m.postRotate(heading, locPoint.x, locPoint.y);
        
        pC.drawBitmap(bitmap, m, txtPaint);
        if (text != null) {
        	pC.drawText(text, locPoint.x, locPoint.y, txtPaint);
        }
        pC.save();
    }
}