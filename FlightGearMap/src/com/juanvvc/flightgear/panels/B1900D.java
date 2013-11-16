package com.juanvvc.flightgear.panels;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.juanvvc.flightgear.MyBitmap;
import com.juanvvc.flightgear.MyLog;
import com.juanvvc.flightgear.PlaneData;
import com.juanvvc.flightgear.instruments.CalibratableRotateSurface;
import com.juanvvc.flightgear.instruments.Instrument;
import com.juanvvc.flightgear.instruments.InstrumentType;
import com.juanvvc.flightgear.instruments.RotateSurface;
import com.juanvvc.flightgear.instruments.SlippingSurface;
import com.juanvvc.flightgear.instruments.StaticSurface;
import com.juanvvc.flightgear.instruments.Surface;
import com.juanvvc.flightgear.instruments.SwitchSurface;

/** Distributed instruments as in a B1900D */
public class B1900D {
	
	public static Instrument createInstrument(InstrumentType type, Context context, float col, float row) {
		MyBitmap hand1 = new MyBitmap("misc1.png", 380, 10, 40, 270); // used in the ASI (long hand)
		MyBitmap hand4 = new MyBitmap("misc2.png", 40, 200, 100, 24); // used in even smaller instruments
		MyBitmap headings = new MyBitmap("nav2.png", -1, -1, -1, -1);
		Typeface face = Typeface.createFromAsset(context.getAssets(), "7-Segment.ttf");//"14_LED1.ttf");

		switch (type) {
		case SPEED:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("b1900d.png", 520, 512, 504, 512), (512-504)/2, (512-512)/2),
					new RotateSurface(new MyBitmap("b1900d.png", 244, 400, 24, 220), 236, 56, PlaneData.VNE_SPEED, 1, 256, 256, 20, 0, 300, 338),
					new RotateSurface(hand1, 236, 56, PlaneData.SPEED, 1, 256, 256, 20, 0, 300, 338)
				});
		case HEADING: // Actually, it is a RMI.
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("nav3.png", 0, 190, 320, 320), 256-160, 256-160),
					new RotateSurface(new MyBitmap("nav4.png", 398, 50, 54, 324), 256-27, 256-162, PlaneData.ADF_DEFLECTION, 1, 256, 256, 0, 0, 720, 720),  // for some reason, the ADF instrument shows headings from 0 to 720
					new RotateSurface(headings, 0, 0, PlaneData.HEADING, 1, 256, 256, 0, 0, 360, -360),
					new C172HIBug(new MyBitmap("misc2.png", 242, 290, 44, 44), 256-22, 36, "/instrumentation/fgc-65/settings/hdg", true, -1, 256, 256, -180, -180, 180, 180),
					new C172HIBug(new MyBitmap("nav4.png", 248, 200, 32, 300), 236, 100, null, false, PlaneData.NAV2_HEADING, 256, 256, 0, 0, 360, 360),
					new StaticSurface(new MyBitmap("nav5.png", -1, -1, -1, -1), 0, 0),
					new StaticSurface(new MyBitmap("nav1.png", -1, -1, -1, -1), 0, 0)
				});
		case CLIMB_RATE:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("b1900d.png", 516, 1032, 508, 500), (512-508)/2, (512-500)/2),
					new B1900DVerticalSpeedSurface(hand1, 236, 56, PlaneData.CLIMB_RATE, 1, 256, 256, -4500, -240, 4500, 60)
				});
		case FUEL: // two instruments, one below the other
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("b1900d.png", 524, 12, 492, 496), (512-492)/2, (512-496)/2),
					new RotateSurface(hand1, 236, 56, PlaneData.FUEL1, 1, 256, 256, 0, -120, 2000, 120),
					new StaticSurface(new MyBitmap("b1900d.png", 524, 12, 492, 496), (512-492)/2, (512-496)/2+512),
					new RotateSurface(hand1, 236, 56+512, PlaneData.FUEL2, 1, 256, 256+512, 0, -120, 2000, 120),
				});
		case TURBINE:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("b1900d.png", 272, 264, 232, 240), (256-232) / 2, (256-240) / 2),
					new StaticSurface(new MyBitmap("b1900d.png", 272, 264, 232, 240), 256 + (256-232) / 2, (256-240) / 2),
					new RotateSurface(hand4, 128, 128-12, PlaneData.TURBINE1, 1, 128, 128,  0, -36, 110, 216),
					new RotateSurface(hand4, 128+256, 128-12, PlaneData.TURBINE2, 1, 128+256, 128, 0, -36, 110, 216),
					new B1900DTurbine(null, PlaneData.TURBINE1, 110, 50, face),
					new B1900DTurbine(null, PlaneData.TURBINE2, 110+256, 50, face)
			});
		case PROP:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("b1900d.png", 8, 260, 232, 240), (256-232) / 2, (256-240) / 2),
					new StaticSurface(new MyBitmap("b1900d.png", 8, 260, 232, 240), 256 + (256-232) / 2, (256-240) / 2),
					new B1900DProp(hand4, 128, 128-12, PlaneData.PROP_ENGINE1, 1, 128, 128,  0, -36, 110, 216),
					new B1900DProp(hand4, 128+256, 128-12, PlaneData.PROP_ENGINE2, 1, 128+256, 128, 0, -36, 110, 216),
			});
		case FUELFLOW:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("b1900d.png", 8, 520, 232, 240), (256-232) / 2, (256-240) / 2),
					new StaticSurface(new MyBitmap("b1900d.png", 8, 529, 232, 240), 256 + (256-232) / 2, (256-240) / 2),
					new RotateSurface(hand4, 128, 128-12, PlaneData.FUEL_FLOW1, 1, 128, 128, 0, 30, 800, 270),
					new RotateSurface(hand4, 128+256, 128-12, PlaneData.FUEL_FLOW2, 1, 128+256, 128, 0, 30, 800, 270),
			});
		case TORQUE:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("b1900d.png", 268, 10, 232, 240), (256-232) / 2, (256-240) / 2),
					new StaticSurface(new MyBitmap("b1900d.png", 268, 10, 232, 240), 256 + (256-232) / 2, (256-240) / 2),
					new RotateSurface(hand4, 128, 128-12, PlaneData.TORQUE1, 1, 128, 128, 0, -75, 5000, 255),
					new RotateSurface(hand4, 128+256, 128-12, PlaneData.TORQUE2, 1, 128+256, 128, 0, -75, 5000, 255)
			});
		case ITT:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("b1900d.png", 12, 8, 232, 240), (256-232) / 2, (256-240) / 2),
					new StaticSurface(new MyBitmap("b1900d.png", 12, 8, 232, 240), 256 + (256-232) / 2, (256-240) / 2),
					new RotateSurface(hand4, 128, 128-12, PlaneData.ITT1, 1, 128, 128, 300, 0, 1000, 270),
					new RotateSurface(hand4, 128+256, 128-12, PlaneData.ITT2, 1, 128+256, 128, 300, 0, 1000, 270),
			});
		case OIL_PRESS: // note: fgfs reports degf, the instrument is celsius. Oil press doesn't seem to be working
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("b1900d.png", 268, 524, 232, 240), (256-232) / 2, (256-240) / 2),
					new StaticSurface(new MyBitmap("b1900d.png", 268, 524, 232, 240), 256 + (256-232) / 2, (256-240) / 2),
					new RotateSurface(hand4, 128, 128-12, PlaneData.OIL_TEMP, 1, 128, 128, -4, -240, 284, -120),
					new RotateSurface(hand4, 128+256, 128-12, PlaneData.OIL2_TEMP, 1, 128+256, 128, -4, -240, 284, -120),
//					new RotateSurface(hand4, 128, 128-12, PlaneData.OIL_PRESS, 1, 128, 128, 0, 50, 200, -50),
//					new RotateSurface(hand4, 128+256, 128-12, PlaneData.OIL2_PRESS, 1, 128+256, 128, 0, 50, 200, -50),
			});
		default:
			MyLog.w(B1900D.class.getSimpleName(), "Instrument not available: " + type);
			return null;
		}
	}
	
	public static ArrayList<Instrument> getInstrumentPanel(Context context) {
		final ArrayList<Instrument> instruments = new ArrayList<Instrument>();
		instruments.add(B1900D.createInstrument(InstrumentType.SPEED, context, 1, 0));
		instruments.add(Cessna172.createInstrument(InstrumentType.ATTITUDE, context, 2, 0));
		instruments.add(Cessna172.createInstrument(InstrumentType.ALTIMETER, context, 3, 0));
		instruments.add(B1900D.createInstrument(InstrumentType.HEADING, context, 1, 1));
		instruments.add(B1900D.createInstrument(InstrumentType.CLIMB_RATE, context, 3, 1));
		instruments.add(Cessna172.createInstrument(InstrumentType.HSI1, context, 2, 1));

		instruments.add(Cessna172.createInstrument(InstrumentType.CLOCK, context, 0, 0));
		instruments.add(Cessna172.createInstrument(InstrumentType.DME, context, 0, 0.5f));
		instruments.add(B1900D.createInstrument(InstrumentType.FUEL, context, 0, 1));
		
		instruments.add(B1900D.createInstrument(InstrumentType.ITT, context, 4, 0));
		instruments.add(B1900D.createInstrument(InstrumentType.TORQUE, context, 4, 0.5f));
		instruments.add(B1900D.createInstrument(InstrumentType.PROP, context, 4, 1));
		instruments.add(B1900D.createInstrument(InstrumentType.TURBINE, context, 4, 1.5f));
		instruments.add(B1900D.createInstrument(InstrumentType.FUELFLOW, context, 4, 2));
		instruments.add(B1900D.createInstrument(InstrumentType.OIL_PRESS, context, 4, 2.5f));
		
		return instruments;
	}
}


/** The vertical speed instrument that I chose for B1900D is not linear.
 */
class B1900DVerticalSpeedSurface extends RotateSurface {

	public B1900DVerticalSpeedSurface(MyBitmap bitmap,
			float x, float y,
			int pdIdx, float rscale,
			int rcx, int rcy,
			float min, float amin, float max, float amax) {
		super(bitmap, x, y, pdIdx, rscale, rcx, rcy, min, amin, max, amax);
	}

	@Override
	protected float getRotationAngle(PlaneData pd) {
		float v = pd.getFloat(pdIdx);
		if ( v > 4500) {
			v = 4500;
		} else if (v < -4500){
			v = -4500;
		}
		float angle;
		if (Math.abs(v) < 1000) { // from 0 to 1000: 75º (linear)
			angle = -90 + (Math.abs(v) * 75 / 1000);
		} else { // from 1000 to 4500: 75º (linear)
			angle = (Math.abs(v) / 1000) * 30 - 45;
		}
		if (v < 0) {
			angle = 180 - angle;
		}
		return angle;
	}
}

/** The N1 instrument that I chose for the B1900D is not linear.
 * This class uses a polynom calculated by octave to output the rotation angle
 */
class B1900DProp extends RotateSurface {

	public B1900DProp(MyBitmap bitmap,
			float x, float y,
			int pdIdx, float rscale,
			int rcx, int rcy,
			float min, float amin, float max, float amax) {
		super(bitmap, x, y, pdIdx, rscale, rcx, rcy, min, amin, max, amax);
	}

	@Override
	protected float getRotationAngle(PlaneData pd) {
		float v = pd.getFloat(pdIdx) / 100;
		// curve adjustment using octave:
		// x=[0,5,10,13,14,15,16,17,18,18,20]; 
		// y=[-90,-41,15,46,73,96,124,148,172,200,223]; (angles calculated with gimp)
		// p=polyfit(x,y,2);
		// ans = 0.65302    2.57879  -83.42426
		return 0.65302f * v * v + 2.57879f * v - 83.42426f;
	}
}

class B1900DTurbine extends StaticSurface {
	private Typeface face;
	private Paint font;
	private StringBuffer sb = null;
	private int idx;
	
	// Bitmap is not currently used
	public B1900DTurbine(MyBitmap bitmap, int idx, float x, float y, Typeface face) {
		super(bitmap, x, y);
		this.face = face;
		this.idx = idx;
	}
	
	@Override
	public void onBitmapChanged() {
		font = new Paint();
		font.setColor(Color.RED);
		font.setTypeface(this.face);
		font.setTextSize((parent.getScale() * parent.getGridSize()) / 32);
		
		sb = new StringBuffer();
	}
	
	public void onDraw(Canvas c) {
		if (planeData == null) { // || bitmap == null || bitmap.getScaledBitmap() == null) {
			return;
		}
		
		// draw always first decimal and units
		if (sb != null) {
			int turbine = (int) planeData.getFloat(this.idx);
			
			final float realscale = parent.getScale() * parent.getGridSize();
			final int left = (int) ((this.getParent().getCol() + relx) * realscale);
			final int top = (int) ((this.getParent().getRow() + rely) * realscale);
			
			sb.setLength(0); // clear the stringbuffer
			sb.append(turbine);
			c.drawText(sb.toString(), left, top, font);
		}
		
	}
}

