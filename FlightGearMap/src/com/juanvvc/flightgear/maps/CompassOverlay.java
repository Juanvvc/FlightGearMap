package com.juanvvc.flightgear.maps;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;

/** An overlay over a map to draw a compass. */
public class CompassOverlay extends org.osmdroid.views.overlay.Overlay {
	private GeoPoint location = null;
	private float heading = 0;
	private float groundspeed = 0;
	private Paint txtPaint = null;
	private Paint txt2Paint = null;
	private Paint hdgPaint = null;

    public CompassOverlay(Context ctx) {
    	super(ctx);
    	
    	txtPaint = new Paint();
    	txtPaint.setColor(Color.WHITE);
    	txtPaint.setTextAlign(Align.CENTER);
    	txtPaint.setTextSize(16);
    	txtPaint.setStyle(Paint.Style.STROKE);
    	txtPaint.setStrokeWidth(2);
    	txtPaint.setShadowLayer(10, 0, 0, Color.BLACK);
    	
    	txt2Paint = new Paint();
    	txt2Paint.setColor(Color.GRAY);
    	txt2Paint.setTextAlign(Align.CENTER);
    	txt2Paint.setStyle(Paint.Style.STROKE);
    	txt2Paint.setStrokeWidth(6);
    	
    	hdgPaint = new Paint();
    	hdgPaint.setColor(Color.YELLOW);
    	hdgPaint.setTextAlign(Align.CENTER);
    	hdgPaint.setTextSize(16);
    	hdgPaint.setStyle(Paint.Style.STROKE);
    	hdgPaint.setStrokeWidth(2);
    	hdgPaint.setShadowLayer(10, 0, 0, Color.BLACK);
    }
        
    /**
     * @param l The position of the overlay
     * @param h The heading of the overlay
     */
    public void setPosition(GeoPoint l, float h, float groundspeed) {
   		location = l;
   		heading = h;
   		this.groundspeed = groundspeed;
    }
	
	private static int nmToRadius(float nm, MapView map, double latitude) {
		Projection proj = map.getProjection();
		float meters = (float) (nm * Projection.METERS_PER_NAUTICAL_MILE);
		return (int) (proj.metersToEquatorPixels(meters) * (1 / Math.cos(Math.toRadians(latitude))));
	}

    @Override
    protected void draw(Canvas pC, MapView mapV, boolean shadow) {
        if (shadow || location == null) {
            return;
        }
        Point locPoint = new Point();
        final Projection pj = mapV.getProjection();
        pj.toMapPixels(location, locPoint);
        
        // draw circle according to the zoom
        int maxradius = 1000; //mapV.getScrollX();
        int minradius = 100;
        for(int nm: new Integer[]{100, 50, 20, 10, 5, 2, 1}) {
        	int nmradius = nmToRadius(nm, mapV, location.getLatitude());
        	if (nmradius < maxradius && nmradius > minradius) {
	        	pC.drawCircle(locPoint.x, locPoint.y, nmradius, txt2Paint);
	        	//pC.drawText(nm +"nm", locPoint.x, locPoint.y - nmradius, txt2Paint);
	        	pC.drawCircle(locPoint.x, locPoint.y, nmradius, txtPaint);
	        	pC.drawText(nm + "nm", locPoint.x, locPoint.y - nmradius, txtPaint);
        	}
        }
        
        Matrix m = new Matrix();
        float[] points = {
        		0, nmToRadius(-this.groundspeed * 2 / 60, mapV, location.getLatitude()),
        		0, nmToRadius(-this.groundspeed * 5 / 60, mapV, location.getLatitude()),
        		0, nmToRadius(-100, mapV, location.getLatitude())};
        //m.setTranslate(locPoint.x, locPoint.y);
        m.postRotate(this.heading);
        m.mapPoints(points);
        pC.drawLine(locPoint.x, locPoint.y, locPoint.x + points[4], locPoint.y + points[5], txt2Paint);
        pC.drawLine(locPoint.x, locPoint.y, locPoint.x + points[4], locPoint.y + points[5], hdgPaint);
        if (this.groundspeed > 40) {
        	pC.drawText("2", locPoint.x + points[0], locPoint.y + points[1], hdgPaint);
        	pC.drawText("5", locPoint.x + points[2], locPoint.y + points[3], hdgPaint);
        }
        
        
        pC.save();
    }
}