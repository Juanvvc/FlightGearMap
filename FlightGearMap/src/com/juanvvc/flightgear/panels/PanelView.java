package com.juanvvc.flightgear.panels;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

import com.juanvvc.flightgear.BitmapProvider;
import com.juanvvc.flightgear.MyLog;
import com.juanvvc.flightgear.PlaneData;
import com.juanvvc.flightgear.R;
import com.juanvvc.flightgear.instruments.CalibratableSurfaceManager;
import com.juanvvc.flightgear.instruments.Instrument;
import com.juanvvc.flightgear.instruments.InstrumentType;
import com.juanvvc.flightgear.instruments.Surface;

/**
 * Shows a instrument panel on the screen.
 * This panel resizes controls to the available space.
 * 
 * @author juanvi
 * 
 */
public class PanelView extends SurfaceView implements OnTouchListener {
	
	/** If set, instruments are centered */
	private static final boolean CENTER_INSTRUMENTS = true;

	/** Specifies the distribution type. */
	// Note: this cannot be an enum since the XML needs an integer to refer to a distribution type.
	public static class Distribution {
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
		/** Show a SenecaII panel */
		public static final int SENECAII_PANEL = 5;
		/** Show a Liquid panel. */
		public static final int LIQUID_PANEL = 6;
		/** Show communication instruments */
		public static final int COMM_PANEL = 7;
		/** Show a Cessna 337 Skymaster panel */
		public static final int C337_INSTRUMENTS = 8;
	};

	/** Scaled to be applied to all sizes on screen. */
	private float scale = 0;
	/** The available instruments. */
	private ArrayList<Instrument> instruments;
	/** Number of columns in the panel. */
	private float cols;
	/** Number of rows in the panel. */
	private float rows;
	/** identifier of the current distribution. */
	private int distribution;
	
	private SurfaceHolder surfaceHolder;
	
	/** The surface that the user is currently moving, if any */
	private Surface movingSurface = null;
	
	/** The bitmap to draw instruments */
	private Bitmap buffer = null;
	/** THe X position of the buffer (centered on screen, or on the left side) */
	private int bufferX = 0;
	/** The Y position of the buffer (centered on screen, or on the top) */
	private int bufferY = 0;

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
	 * @param The distribution to use (see Distribution class for values)
	 */
	public void setDistribution(int distribution) {
		
		synchronized(instruments) {
			
			MyLog.v(this, "Loading distribution: " + distribution);
	
			instruments.clear();
	
			Context context = getContext();
			switch (distribution) {
			case Distribution.SIMPLE_VERTICAL_PANEL:
				cols = 2;
				rows = 3;
				instruments.add(Cessna172.createInstrument(InstrumentType.ATTITUDE, context, 0, 0));
				instruments.add(Cessna172.createInstrument(InstrumentType.TURN_RATE, context, 1, 0));
				instruments.add(Cessna172.createInstrument(InstrumentType.SPEED, context, 0, 1));
				instruments.add(Cessna172.createInstrument(InstrumentType.HSI1, context, 1, 1));
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
				instruments.add(Cessna172.createInstrument(InstrumentType.HSI1, context, 1, 1));
				instruments.add(Cessna172.createInstrument(InstrumentType.CLIMB_RATE, context, 2, 1));
	
				break;
			case Distribution.HORIZONTAL_PANEL:
				cols = 6;
				rows = 1;
				instruments.add(Cessna172.createInstrument(InstrumentType.ATTITUDE, context, 0, 0));
				instruments.add(Cessna172.createInstrument(InstrumentType.TURN_RATE, context, 1, 0));
				instruments.add(Cessna172.createInstrument(InstrumentType.SPEED, context, 2, 0));
				instruments.add(Cessna172.createInstrument(InstrumentType.HSI1, context, 3, 0));
				instruments.add(Cessna172.createInstrument(InstrumentType.ALTIMETER, context, 4, 0));
				instruments.add(Cessna172.createInstrument(InstrumentType.CLIMB_RATE, context, 5, 0));
				break;
			case Distribution.C172_INSTRUMENTS:
				cols = 5;
				rows = 3;
				instruments = Cessna172.getInstrumentPanel(context);
				break;
			case Distribution.SENECAII_PANEL:
				cols = 5;
				rows = 3.5f;
				instruments = SenecaII.getInstrumentPanel(context);
				break;
			case Distribution.C337_INSTRUMENTS:
				cols = 5;
				rows = 3;
				instruments = Cessna337.getInstrumentPanel(context);
				break;
			case Distribution.LIQUID_PANEL:
				cols = 1;
				rows = 2;
				instruments = LiquidDisplay.getInstrumentPanel(context);
				break;
//			case Distribution.COMM_PANEL:
//				cols = 3;
//				rows = 2;
//				instruments.add(Cessna172.createInstrument(InstrumentType.TURN_RATE, context, 0, 0));
//				instruments.add(Cessna172.createInstrument(InstrumentType.ATTITUDE, context, 1, 0));
//				instruments.add(Cessna172.createInstrument(InstrumentType.CLIMB_RATE, context, 2, 0));
//				instruments.add(Cessna172.createInstrument(InstrumentType.NAV1, context, 0, 1));
//				instruments.add(SenecaII.createInstrument(InstrumentType.NAV2, context, 1, 1));
//				instruments.add(Cessna172.createInstrument(InstrumentType.ADF, context, 2, 1));
//				break;
			default:
				MyLog.w(this, "No distribution configured for panel");
			}
			
			this.reloadImages();
			this.rescaleInstruments();
		
			this.distribution = distribution;
		}
	}
	
	/** Sets the calibratable surface manager, and register any available calibratable surface. */
	public void postCalibratableSurfaceManager(CalibratableSurfaceManager cs) {
		for(Instrument i: instruments) {
			 // TODO: an instrument should never be null. Use only during development
			if ( i == null ) {
				MyLog.w(PanelView.class, "Instrument is null");
				continue;
			}
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
			// scale to match the available size. All instrumewnts should be visible.
			// TODO: this assumes that the first instrument is not null
			scale = Math.min(
					1.0f * getWidth() / (cols * instruments.get(0).getGridSize()),
					1.0f * getHeight()/ (rows * instruments.get(0).getGridSize()));
			MyLog.d(this, "Scale: " + scale);

			// prevent spurious scales
			// if (Math.abs(scale - 1) < 0.1) {
			// scale = 1;
			// }
			
			boolean scaled = false;
			while (!scaled) {
				try {
					Instrument.getBitmapProvider(this.getContext()).setScale(scale);
					for (Instrument i: instruments) {
						if (i != null) {
							i.setScale(scale);
						}
					}
					scaled = true;
				} catch (OutOfMemoryError e) {
					scale = scale / 2;
				}
			}
			
			// create the buffer
			if (CENTER_INSTRUMENTS) {
				if (buffer != null) {
					buffer.recycle();
				}
				buffer = Bitmap.createBitmap(
						(int)(scale * cols * instruments.get(0).getGridSize()),
						(int)(scale * rows * instruments.get(0).getGridSize()),
						Bitmap.Config.RGB_565);
				bufferX = (this.getWidth() - buffer.getWidth()) / 2;
				bufferY = (this.getHeight() - buffer.getHeight()) / 2;
			} else {
				if (buffer != null) {
					buffer.recycle();
				}
				buffer = null;
				bufferX = 0;
				bufferY = 0;
			}
		}
	}
	
	/** Reload and re-scale images inside the instruments.
	 * Call this method when the view is created or its size changes.
	 * TODO: This method could be run on a different thread, but
	 * on my devices loading does not takes long and can be
	 * in the main thread.
	 */
	private void reloadImages() {
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
						MyLog.e(this, "Cannot load instruments: " + MyLog.stackToString(e2));
					}
				} catch (Exception e) {
					MyLog.e(this, "Cannot load instrument: " + MyLog.stackToString(e));
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
			for(int j = 0; j < instruments.size(); j++) {
				Instrument ins = instruments.get(j);
				if (ins != null) {
					ins.postPlaneData(pd);
				}
			}
		}
	}

	protected void redraw() {
		synchronized(instruments) {
			if (!surfaceHolder.getSurface().isValid()) {
				return;
			}
			
			// prepare the canvas. If CENTER_INSTRUMENTS is set, use the buffer.
			// if not set, draw directly on the screen
			Canvas c = null;
			if (CENTER_INSTRUMENTS) {
				if (buffer == null) {
					return;
				}
				c = new Canvas(buffer);
			} else {
				c = surfaceHolder.lockCanvas();
				if (c == null) {
					return;
				}
			}
			c.drawColor(Color.DKGRAY);
	
			try {
				for(int j = 0; j < instruments.size(); j++) {
					Instrument ins = instruments.get(j);
					if (ins != null) {
						ins.onDraw(c); // this is a "suspicious call" according to Eclipse: switch the warning off!!
					}
				}
			} catch(IndexOutOfBoundsException e) {
				// TODO: this exception is thrown if redrawing() while the
				// instruments are not ready. Prevent this.
				MyLog.w(this, MyLog.stackToString(e));
			} catch(NullPointerException e) {
				// This usually means that an image is not found
				MyLog.w(this, MyLog.stackToString(e));
			} catch(NumberFormatException e) {
				// Most probable cause: wrong XML in FlightGear
				MyLog.w(this, MyLog.stackToString(e));
			} catch(RuntimeException e) {
				// this exception is thrown when using a recycled bitmap on Canvas, for example
				// TODO: I'm ignoring this exception, but I'm sure that it means an error in the program
			}
			
			// If CENTER_INSTRUMENTS is set, draw the buffer on the canvas
			// if not set, assume that we were drawing directly on the screen
			if (CENTER_INSTRUMENTS) {
				if (buffer != null) {
					c = surfaceHolder.lockCanvas();
					c.drawBitmap(buffer, bufferX, bufferY, null);
					surfaceHolder.unlockCanvasAndPost(c);
				}
			} else {
				surfaceHolder.unlockCanvasAndPost(c);
			}
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch(event.getAction()) {
		// remember: instruments may be drawing on a buffer, with an offset of (bufferX, bufferY)
		// if no buffer is used, then it is mandatory that bufferX=bufferY=0
		case MotionEvent.ACTION_DOWN:
			if (this.movingSurface != null) {
				this.movingSurface.onMove(-1, -1, true);
			}
			for(Instrument i: this.instruments) {
				movingSurface = i.getControlingSurface(event.getX() - bufferX, event.getY() - bufferY);
				if (movingSurface != null) {
					break;
				}
			}
			if (movingSurface != null) {
				movingSurface.onMove(
						movingSurface.getParent().getXtoInnerX(event.getX() - bufferX),
						movingSurface.getParent().getYtoInnerY(event.getY() - bufferY),
						false);
			} else {
				MyLog.d(this, "Event down and no surface controls the movement");
			}
			break;
		case MotionEvent.ACTION_UP:
			if (this.movingSurface != null) {
				movingSurface.onMove(
						movingSurface.getParent().getXtoInnerX(event.getX() - bufferX),
						movingSurface.getParent().getYtoInnerY(event.getY() - bufferY),
						true);
				movingSurface = null;
			} else {
				MyLog.d(this, "Event up and no surface controls the movement");
			}
			break;
		case MotionEvent.ACTION_MOVE:
		default:
			if (this.movingSurface != null) {
				movingSurface.onMove(
						movingSurface.getParent().getXtoInnerX(event.getX() - bufferX),
						movingSurface.getParent().getYtoInnerY(event.getY() - bufferY),
						false);
			} else {
				MyLog.d(this, "Event move and no surface controls the movement");
			}
		}
		
		return true;
	}
}
