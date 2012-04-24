package com.juanvvc.flightgear;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;


/** Shows a small panel on the screen.
 * This panel resize controls to the available space.
 * @author juanvi
 *
 */
public class SmallPanelView extends View {
	/** Scaled to be applied to all sizes on screen. */
	private float scale = 0;
	/** Constant TAG to be used duting development. */	
	private static final String TAG = "SmallPanelView";
	/** Plane data. */
	private PlaneData lastPlaneData = new PlaneData();
	/** The available instruments. */
	private ArrayList<Instrument> instruments;
	/** Number of columns in the panel. */
	private int cols;
	/** Number of rows in the panel. */
	private int rows;
	
	/* Constructors */
	public SmallPanelView(Context context) {
		super(context);
	}
	public SmallPanelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		instruments = new ArrayList<Instrument>();
		
		// check if the widget description included the "vertical" attribute
		// Vertical is a panel 2x3, and it is used in large screens.
		// if not vertical, a panel 6x1 is used. Useful for small screens.
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SmallPanelView);
		boolean vertical = true;
		for(int i = 0; i < a.getIndexCount(); i++) {
			switch (a.getIndex(i)) {
			case R.styleable.SmallPanelView_vertical:
				vertical = a.getBoolean(i, true);
			default:
			}
		}
		
		if (vertical) {
			cols = 2;
			rows = 3;
			instruments.add(new Attitude(0, 0, context));
			instruments.add(new TurnSlip(Instrument.SEMICLOCKSIZE * 2, 0, context));
			instruments.add(new Speed(0, Instrument.SEMICLOCKSIZE * 2, context));
			instruments.add(new RPM(Instrument.SEMICLOCKSIZE * 2, Instrument.SEMICLOCKSIZE * 2, context));
			instruments.add(new Altimeter(0, Instrument.SEMICLOCKSIZE * 4, context));
			instruments.add(new ClimbRate(Instrument.SEMICLOCKSIZE * 2, Instrument.SEMICLOCKSIZE * 4, context));
		} else {
			cols = 6;
			rows = 1;
			instruments.add(new Attitude(0, 0, context));
			instruments.add(new TurnSlip(Instrument.SEMICLOCKSIZE * 2, 0, context));
			instruments.add(new Speed(Instrument.SEMICLOCKSIZE * 4, 0, context));
			instruments.add(new RPM(Instrument.SEMICLOCKSIZE * 6, 0, context));
			instruments.add(new Altimeter(Instrument.SEMICLOCKSIZE * 8, 0, context));
			instruments.add(new ClimbRate(Instrument.SEMICLOCKSIZE * 10, 0, context));			
		}
		
		// load the instruments. This could be in a different thread, but IN MY DEVICES, loading does not take long
		for(Instrument i: instruments) {
			try {
				i.loadImages();
			} catch (Exception e) {
				myLog.w(TAG, "Cannot load instrument: " + myLog.stackToString(e));
			}
		}
	}


	/** Rescale bitmaps.
	 * Call after a new size is detected and at the beginning of the execution.
	 */
	private void rescaleInstruments() {
		if (getWidth() > 0) {
			// scale to match the available size. All instrumewnts should be visible.
			scale = Math.min(1.0f * getWidth() / (cols * 2 * Instrument.SEMICLOCKSIZE), 1.0f * getHeight() / (rows * 2 * Instrument.SEMICLOCKSIZE));
			for(Instrument i: instruments) {
				i.setScale(scale);
			}
		}
	}
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		rescaleInstruments();
	}
	
	/**
	 * @param pd The last received PlaneData 
	 */
	public void setPlaneData(PlaneData pd) {
		this.lastPlaneData = pd;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	
		for(Instrument i: instruments) {
			i.onDraw(canvas, lastPlaneData);
		}
	}
}
