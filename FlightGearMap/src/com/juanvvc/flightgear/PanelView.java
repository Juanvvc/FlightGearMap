package com.juanvvc.flightgear;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

import com.juanvvc.flightgear.instruments.CalibratableSurfaceManager;
import com.juanvvc.flightgear.instruments.Instrument;
import com.juanvvc.flightgear.instruments.InstrumentType;
import com.juanvvc.flightgear.instruments.Surface;

/**
 * Shows a small panel on the screen. This panel resize controls to the
 * available space.
 * 
 * @author juanvi
 * 
 */
public class PanelView extends SurfaceView implements OnTouchListener {

	/** Specifies the distribution type. */
	// TODO: change this to a enum
	public class Distribution {
		/** A 2x3 panel with simple instruments. */
		public static final int SIMPLE_VERTICAL_PANEL = 0;
		/** 6x1 panel with simple instruments. */
		public static final int HORIZONTAL_PANEL = 1;
		/** A 3x2 panel with simple instruments. */
		public static final int SIMPLE_HORIZONTAL_PANEL = 2;
		/** Show only the map. PanelView is not used. */
		public static final int ONLY_MAP = 3;
		/** Show a complete Cessna-172 instrument panel. */
		public static final int C172_INSTRUMENTS = 4;
		/** Show a complete Cessna-172 comm panel. */
		public static final int C172_COMM = 5;
	};

	/** Scaled to be applied to all sizes on screen. */
	private float scale = 0;
	/** The available instruments. */
	private ArrayList<Instrument> instruments;
	/** Number of columns in the panel. */
	private int cols;
	/** Number of rows in the panel. */
	private int rows;
	/** identifier of the current distribution. */
	private int distribution;
	
	private SurfaceHolder surfaceHolder;
	
	/** The surface that the user is currently moving, if any */
	private Surface movingSurface = null;

	/* Constructors */
	public PanelView(Context context) {
		super(context);
		this.setOnTouchListener(this);
		this.surfaceHolder = this.getHolder();
	}

	public PanelView(Context context, AttributeSet attrs) {
		super(context, attrs);

		instruments = new ArrayList<Instrument>();

		// Check the instrument distribution that was declared in the XML
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PanelView);
		int distribution = 0;
		for (int i = 0; i < a.getIndexCount(); i++) {
			switch (a.getIndex(i)) {
			case R.styleable.PanelView_distribution:
				distribution = a.getInt(i, 0);
				break;
			default:
			}
		}

		setDistribution(distribution);
		this.setOnTouchListener(this);
		this.surfaceHolder = this.getHolder();
	}
	
	/**
	 * @return The name of the most suitable image set.
	 */
	private String selectImageSet() {
		float minSize = Math.min(getWidth() * 1.0f / cols , getHeight() * 1.0f / rows);
		if (minSize > 400) {
			//return "high"; // not available to save space
			return "medium";
		} else if (minSize > 200) {
			return "medium";
		} else {
			return "low";
		}
	}

	/**
	 * Sets the distribution of this panel.
	 * 
	 * @param The
	 *            distribution to use
	 */
	public void setDistribution(int distribution) {
		
		synchronized(instruments) {
			
			myLog.v(this, "Loading distribution: " + distribution);
	
			instruments.clear();
	
			Context context = getContext();
			switch (distribution) {
			case Distribution.SIMPLE_VERTICAL_PANEL:
				cols = 2;
				rows = 3;
				instruments.add(Cessna172.createInstrument(InstrumentType.ATTITUDE, context, 0, 0));
				instruments.add(Cessna172.createInstrument(InstrumentType.TURN_RATE, context, 1, 0));
				instruments.add(Cessna172.createInstrument(InstrumentType.SPEED, context, 0, 1));
				instruments.add(Cessna172.createInstrument(InstrumentType.RPM, context, 1, 1));
				instruments.add(Cessna172.createInstrument(InstrumentType.ALTIMETER, context, 0, 2));
				instruments.add(Cessna172.createInstrument(InstrumentType.CLIMB_RATE, context, 1, 2));
	
				break;
			case Distribution.SIMPLE_HORIZONTAL_PANEL:
				cols = 3;
				rows = 2;
				instruments.add(Cessna172.createInstrument(InstrumentType.SPEED, context, 0, 0));
				instruments.add(Cessna172.createInstrument(InstrumentType.ATTITUDE, context, 1, 0));
				instruments.add(Cessna172.createInstrument(InstrumentType.ALTIMETER, context, 2, 0));
				instruments.add(Cessna172.createInstrument(InstrumentType.TURN_RATE, context, 0, 1));
				instruments.add(Cessna172.createInstrument(InstrumentType.RPM, context, 1, 1));
				instruments.add(Cessna172.createInstrument(InstrumentType.CLIMB_RATE, context, 2, 1));
	
				break;
			case Distribution.HORIZONTAL_PANEL:
				cols = 6;
				rows = 1;
				instruments.add(Cessna172.createInstrument(InstrumentType.ATTITUDE, context, 0, 0));
				instruments.add(Cessna172.createInstrument(InstrumentType.TURN_RATE, context, 1, 0));
				instruments.add(Cessna172.createInstrument(InstrumentType.SPEED, context, 2, 0));
				instruments.add(Cessna172.createInstrument(InstrumentType.RPM, context, 3, 0));
				instruments.add(Cessna172.createInstrument(InstrumentType.ALTIMETER, context, 4, 0));
				instruments.add(Cessna172.createInstrument(InstrumentType.CLIMB_RATE, context, 5, 0));
				break;
			case Distribution.C172_INSTRUMENTS:
				cols = 5;
				rows = 3;
				instruments = Cessna172.getInstrumentPanel(context);
	
			default: // this includes Distribution.NO_MAP
			}
			
			this.reloadImages();
			this.rescaleInstruments();
		
			this.distribution = distribution;
		}
	}
	
	/** Sets the calibratable surface manager, and register any available calibratable surface. */
	public void postCalibratableSurfaceManager(CalibratableSurfaceManager cs) {
		for(Instrument i: instruments) {
			Surface[] ss = i.getSurfaces();
			for (int j = 0; j < ss.length; j++) {
				ss[j].postCalibratableSurfaceManager(cs);
			}
		}
	}
	
	/** @return The currently displayed distribution. */
	public int getDistribution() {
		return this.distribution;
	}

	/**
	 * Rescale bitmaps. Call after a new size is detected and at the beginning
	 * of the execution.
	 */
	private void rescaleInstruments() {
		if (getWidth() > 0 && instruments != null && instruments.size() > 0) {
			// scale to match the available size. All instrumewnts should be
			// visible.
			scale = Math.min(
					1.0f * getWidth() / (cols * instruments.get(0).getGridSize()),
					1.0f * getHeight()/ (rows * instruments.get(0).getGridSize()));
			myLog.d(this, "Scale: " + scale);

			// prevent spurious scales
			// if (Math.abs(scale - 1) < 0.1) {
			// scale = 1;
			// }

			Instrument.getBitmapProvider(this.getContext()).setScale(scale);
			for (Instrument i: instruments) {
				if (i != null) {
					i.setScale(scale);
				}
			}
		}
	}
	
	private void reloadImages() {
		// reload and rescale images
		// load the instruments. This could be in a different thread, but IN MY
		// DEVICES, loading does not take long
		
		Instrument.getBitmapProvider(this.getContext()).recycle();
		
		for (Instrument i : instruments) {
			if ( i != null) {
				try {
					i.loadImages(this.selectImageSet());
				} catch (OutOfMemoryError e) {
					// if out of memory, try forcing the low quality version
					try {
						i.loadImages(BitmapProvider.LOW_QUALITY);
					} catch (Exception e2) {
						myLog.e(this, "Cannot load instruments: " + myLog.stackToString(e2));
					}
				} catch (Exception e) {
					myLog.e(this, "Cannot load instrument: " + myLog.stackToString(e));
				}
			}
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		synchronized(instruments) {
			this.reloadImages();
			this.rescaleInstruments();
		}
	}

	/**
	 * @param pd
	 *            The last received PlaneData
	 */
	public void postPlaneData(PlaneData pd) {
		synchronized(instruments) {
			// TODO: if we do not synchronize this method, flickering appears
			for(int j = 0; j < instruments.size(); j++) {
				instruments.get(j).postPlaneData(pd);
			}
		}
	}

	protected void redraw() {
		synchronized(instruments) {
			if (!surfaceHolder.getSurface().isValid()) {
				return;
			}
			
			Canvas c = surfaceHolder.lockCanvas();
			c.drawColor(Color.DKGRAY);
	
			try {
				for(int j = 0; j < instruments.size(); j++) {
					instruments.get(j).onDraw(c);
				}
			} catch(IndexOutOfBoundsException e) {
				// TODO: this exception is thrown if redrawing() while the
				// instruments are not ready. Prevent this.
			}
			
			surfaceHolder.unlockCanvasAndPost(c);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (this.movingSurface != null) {
				this.movingSurface.onMove(-1, -1, true);
			}
			for(Instrument i: this.instruments) {
				movingSurface = i.getControlingSurface(event.getX(), event.getY());
				if (movingSurface != null) {
					break;
				}
			}
			if (movingSurface != null) {
				movingSurface.onMove(
						movingSurface.getParent().getXtoInnerX(event.getX()),
						movingSurface.getParent().getYtoInnerY(event.getY()),
						false);
			} else {
				myLog.d(this, "Event down and no surface controls the movement");
			}
			break;
		case MotionEvent.ACTION_UP:
			if (this.movingSurface != null) {
				movingSurface.onMove(
						movingSurface.getParent().getXtoInnerX(event.getX()),
						movingSurface.getParent().getYtoInnerY(event.getY()),
						true);
				movingSurface = null;
			} else {
				myLog.d(this, "Event up and no surface controls the movement");
			}
			break;
		case MotionEvent.ACTION_MOVE:
		default:
			if (this.movingSurface != null) {
				movingSurface.onMove(
						movingSurface.getParent().getXtoInnerX(event.getX()),
						movingSurface.getParent().getYtoInnerY(event.getY()),
						false);
			} else {
				myLog.d(this, "Event move and no surface controls the movement");
			}
		}
		
		return true;
	}
}
