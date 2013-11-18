package com.juanvvc.flightgear.panels;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.preference.PreferenceManager;

import com.juanvvc.flightgear.MyBitmap;
import com.juanvvc.flightgear.MyLog;
import com.juanvvc.flightgear.PlaneData;
import com.juanvvc.flightgear.instruments.CalibratableRotateSurface;
import com.juanvvc.flightgear.instruments.Instrument;
import com.juanvvc.flightgear.instruments.InstrumentType;
import com.juanvvc.flightgear.instruments.MagnetosStarterSurface;
import com.juanvvc.flightgear.instruments.RotateSurface;
import com.juanvvc.flightgear.instruments.SlippingSurface;
import com.juanvvc.flightgear.instruments.StaticSurface;
import com.juanvvc.flightgear.instruments.Surface;
import com.juanvvc.flightgear.instruments.SwitchSurface;

/** Distribute instruments as in a Cessna 172 */
public class Cessna172 {
	
	public static Instrument createInstrument(InstrumentType type, Context context, float col, float row) {
		MyBitmap hand1 = new MyBitmap("misc1.png", 380, 10, 40, 270); // used in the ASI (long hand)
		MyBitmap hand2 = new MyBitmap("misc3.png", 393, 297, 40, 208); // used in the ALTIMETER (short hand)
		MyBitmap hand3 = new MyBitmap("misc2.png", 4, 200, 140, 24); // used in small instruments
		MyBitmap hand5 = new MyBitmap("nav4.png", 496, 258, 16, 244); // NAV1 and NAV2
		MyBitmap headings = new MyBitmap("nav2.png", -1, -1, -1, -1);
		MyBitmap fromto = new MyBitmap("nav4.png", 0, 58, 183, 64);
		MyBitmap switches1 = new MyBitmap("switches.png", 0, 0, 128, 368);
		MyBitmap switches2 = new MyBitmap("switches.png", 128, 0, 128, 368);
		MyBitmap switches3 = new MyBitmap("switches.png", 258, 0, 122, 306);
		Typeface face = Typeface.createFromAsset(context.getAssets(), "7-Segment.ttf");//"14_LED1.ttf");

		
		switch (type) {

		case SPEED:
			return new Instrument(col, row, context, new Surface[] {
				new StaticSurface(new MyBitmap("asi2.png", -1, -1, -1, -1), 0, 0),
				new C172AirSpeedSurface(hand1, 236, 56, PlaneData.SPEED, 1, 256, 256, 0, 0, 200, 320)
			});
		case ATTITUDE:
			return new Instrument(col, row, context, new Surface[] {
					new C172AtiSurface(new MyBitmap("misc1.png", 0, 268, 376, 236), 70, 138),
					new RotateSurface(new MyBitmap("ati2.png", -1, -1, -1, -1), 23, 23, PlaneData.ROLL, 1, 256, 256, -180, 180, 180, -180),
					new StaticSurface(new MyBitmap("ati4.png", -1, -1, -1, -1), 0, 0)
				});
		case ALTIMETER:
			return new Instrument(col, row, context, new Surface[] {
					new CalibratableRotateSurface(new MyBitmap("alt3.png", -1, -1, -1, -1), 0, 0, "/instrumentation/altimeter/setting-inhg", true, -1, 256, 256, 27.9f, 210, 31.5f, -150),
					new StaticSurface(new MyBitmap("alt1.png", -1, -1, -1, -1), 0, 0),
					new RotateSurface(hand2, 236, 100, PlaneData.ALTITUDE, 0.001f, 256, 256, 0, 0, 30, 3 * 360),
					new C172AltimeterLongHandSurface(hand1, 236, 56, PlaneData.ALTITUDE, 1, 256, 256, 0, 0, 10, 360)
				});
		case NAV1:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("nav6.png", -1, -1, -1, -1), 0, 0),
					new CalibratableRotateSurface(headings, 0, 0, "/instrumentation/nav/radials/selected-deg", true, -1, 256, 256, 0, 0, 360, -360),
					new C172FromToGSSurface(fromto, 310, 210, PlaneData.NAV1_TO, PlaneData.NAV1_FROM, -1),
					new C172FromToGSSurface(fromto, 185, 210, -1, -1, PlaneData.GS1_INRANGE),
					new RotateSurface(hand5, 245, 120, PlaneData.NAV1_DEFLECTION, 1, 256, 120, -10, 25, 10, -25),
					new RotateSurface(hand5, 125, 266, PlaneData.GS1_DEFLECTION, 1, 125, 266, -1, -65, 1, -115),
					new StaticSurface(new MyBitmap("nav1.png", -1, -1, -1, -1), 0, 0)
				});	
		case NAV2:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("nav3.png", 0, 190, 320, 320), 256-160, 256-160),
					new StaticSurface(new MyBitmap("nav4.png", 0, 122, 244, 148), 256-122, 256-30),
					new CalibratableRotateSurface(new MyBitmap("nav2.png", -1, -1, -1, -1), 0, 0, "/instrumentation/nav[1]/radials/selected-deg", true, -1, 256, 256, 0, 0, 360, -360),
					new C172FromToGSSurface(fromto, 304, 220, PlaneData.NAV2_TO, PlaneData.NAV2_FROM, -1),
					new RotateSurface(hand5, 245, 120, PlaneData.NAV2_DEFLECTION, 1, 256, 120, -10, 25, 10, -25),
					new StaticSurface(new MyBitmap("nav1.png", -1, -1, -1, -1), 0, 0)
				});
		case ADF:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("nav3.png", 0, 190, 320, 320), 256-160, 256-160),
					new CalibratableRotateSurface(headings, 0, 0, "/instrumentation/adf/rotation-deg", true, -1, 256, 256, 0, 0, 360, -360),
					new RotateSurface(new MyBitmap("nav4.png", 248, 200, 32, 300), 236, 100, PlaneData.ADF_DEFLECTION, 1, 256, 256, 0, 0, 720, 720), // for some reason, the ADF instrument shows headings from 0 to 720
					new StaticSurface(new MyBitmap("nav1.png", -1, -1, -1, -1), 0, 0)
				});
		case HEADING:
			return new Instrument(col, row, context, new Surface[] {
					new RotateSurface(new MyBitmap("hdg1.png", -1, -1, -1, -1), 0, 0, PlaneData.HEADING, 1, 256, 256, 0, 0, 360, -360),
					new C172HIBug(new MyBitmap("misc2.png", 242, 290, 44, 44), 256-22, 36, "/autopilot/settings/heading-bug-deg", true, -1, 256, 256, -180, -180, 180, 180),
					new StaticSurface(new MyBitmap("hdg2.png", -1, -1, -1, -1), 0, 0)
				});
		case TURN_RATE:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("misc1.png", 0, 0, 300, 78), 100, 300),
					new RotateSurface(new MyBitmap("misc1.png", 312,9,48,62), 230, 300, PlaneData.SLIP, 1, 256, 0, -1, -25, 1, 25),
					new StaticSurface(new MyBitmap("trn1.png", -1, -1, -1, -1), 0, 0),
					new RotateSurface(new MyBitmap("misc1.png", 438, 0, 74, 320), 256-37, 256-160, PlaneData.TURN_RATE, 1, 256, 256, -4, -130, 4, -50),
				});
		case CLIMB_RATE:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("vsi1.png", -1, -1, -1, -1), 0, 0),
					new RotateSurface(hand1, 236, 56, PlaneData.CLIMB_RATE, 1, 256, 256, -2000, -265, 2000, 85)
				});
		case RPM:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("rpm1.png", -1, -1, -1, -1), 0, 0),
					new RotateSurface(hand1, 236, 56, PlaneData.RPM, 1, 256, 256, 0, -125, 3500, 125)
				});
		case RPM2:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("rpm1.png", -1, -1, -1, -1), 0, 0),
					new RotateSurface(hand1, 236, 56, PlaneData.RPM2, 1, 256, 256, 0, -125, 3500, 125)
				});
		case FUEL:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("fuel1.png", -1, -1, -1, -1), 0, 0),
					new RotateSurface(hand3, 0, 218, PlaneData.FUEL1, 1, 0, 230, 0, 60, 26, -60),
					new RotateSurface(hand3, 288, 218, PlaneData.FUEL2, 1, 288, 230, 0, -240, 26, -120)
				});
		case OIL_TEMP:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("oil1.png", -1, -1, -1, -1), 0, 0),
					new RotateSurface(hand3, 0, 218, PlaneData.OIL_TEMP, 1, 0, 230, 75, 60, 250, -60),
					new RotateSurface(hand3, 288, 218, PlaneData.OIL_PRESS, 1, 288, 230, 0, -240, 115, -120)
				});
		case BATT:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("battery-c172p.png", -1, -1, -1, -1), 0, 0),
					new RotateSurface(hand3, 0, 218, PlaneData.AMP, 1, 0, 230, -40, 55, 40, -55),
					new RotateSurface(hand3, 288, 218, PlaneData.VOLT, 1, 288, 230, 0, -235, 40, -125)
				});
//		case EGT:
//			return new Instrument(col, row, context, new Surface[] {
//					new StaticSurface(new MyBitmap("egt1.png", -1, -1, -1, -1), 0, 0),
//					new RotateSurface(hand3, 0, 218, PlaneData.AMP, 1, 0, 230, -40, 55, 40, -55),
//					new RotateSurface(hand3, 288, 218, PlaneData.VOLT, 1, 288, 230, 0, -235, 40, -125)
//				});
		case MANIFOLD:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("manifold1.png", -1, -1, -1, -1), 0, 0),
					new RotateSurface(hand1, 236, 56, PlaneData.MANIFOLD, 1, 256, 256, 10, -110, 50, 110)
				});
		case TRIMFLAPS:
			return new Instrument(col, row, context, new Surface[] {
					new SlippingSurface(hand3, 180, PlaneData.ELEV_TRIM, -1, 248, 218, 1, 248, 42),
					new SlippingSurface(hand3, 0, PlaneData.FLAPS, 0, 258, 18, 1, 258, 203),
					new StaticSurface(new MyBitmap("trimflaps.png", -1, -1, -1, -1), 93, 13),
				});
		case SWITCHES:
			return new Instrument(col, row, context, new Surface[] {
//					new SwitchSurface(switches2, 0, 0, "/controls/engines/engine[0]/master-bat", "BATT."),
//					new SwitchSurface(switches2, 128, 0, "/controls/engines/engine[0]/master-alt", "ALT."),
//					new SwitchSurface(switches1, 256, 0, "/controls/switches/master-avionics", "AVION."),
					new SwitchSurface(switches3, 256, 0, "/controls/anti-ice/pitot-heat", "P. HEAT"),
					new SwitchSurface(switches3, 384, 0, "/controls/lighting/strobe", "STRO."),
					new SwitchSurface(switches3, 512+0, 0, "/controls/lighting/nav-lights", "NAV"),
					new SwitchSurface(switches3, 512+128, 0, "/controls/lighting/beacon", "BCN"),
					new SwitchSurface(switches3, 512+256, 0, "/controls/lighting/taxi-light", "TAX"),
					new SwitchSurface(switches3, 512+384, 0, "/controls/lighting/landing-lights", "LNG"),
				});
		case DME:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("dme.png", 0, 0, 512, 216), 0, 0),
					new DMENumber(null, 10, 120, PlaneData.DME, face),
					new DMENumber(null, 240, 120, PlaneData.DME_SPEED, face)
			});
		case MAGNETS_STARTER:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("magnetos.png", -1, -1, -1, -1), 256-128, 128-128),
					new MagnetosStarterSurface(new MyBitmap("magnetos.png", 92, 96, 82, 84), 256-128+92, 128-128+96, 259, 136, null, "/controls/engines/engine/magnetos", starter),
					
					new SwitchSurface(switches2, 0, 256, "/controls/engines/engine[0]/master-bat", "BATT."),
					new SwitchSurface(switches2, 128, 256, "/controls/engines/engine[0]/master-alt", "ALT."),
					new SwitchSurface(switches1, 256, 256, "/controls/switches/master-avionics", "AVION."),
					new SwitchSurface(switches1, 384, 256, "/controls/switches/fuel-pump", "F.PUMP"),
			});
		case HSI1:
			// The center of the instrument is (256, 274)
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("nav3.png", 0, 190, 320, 320), 256-160, 256-160),
					new RotateSurface(new MyBitmap("hsi3.png", 0, 0, 328, 328), 256-164, 274-164, PlaneData.HEADING, 1, 256, 274, 0, 0, 360, -360),
					new C172HIBug(null, 256-22, 100, "/instrumentation/nav/radials/selected-deg", true, -1, 256, 274, 0, 0, 360, 360), // I'm using this to select the radial. Notice the null bitmap
					new StaticSurface(new MyBitmap("hsi2.png", 0, 0, 408, 416), 256-204, 256-208),
					new C172GS1(new MyBitmap("hsi2.png", 412, 124, 32, 32), 0, PlaneData.GS1_DEFLECTION, -1, 50, 256+85, 1, 50, 256-85),
					new C172GS1(new MyBitmap("hsi2.png", 452, 124, 32, 32), 0, PlaneData.GS1_DEFLECTION, -1, 430, 256+85, 1, 430, 256-85),
					new RelativeToHeadingRotateSurface(new MyBitmap("hsi2.png", 444, 164, 32, 64), 256-16, 340, PlaneData.NAV1_SEL_RADIAL, 1, 256, 274, 0, 0, 360, 360, 180), // CDI, head
					new RelativeToHeadingRotateSurface(new MyBitmap("hsi2.png", 484, 172, 20, 68), 256-10, 130, PlaneData.NAV1_SEL_RADIAL, 1, 256, 274, 0, 0, 360, 360, 180), // CDI, tail
					new RelativeToHeadingRotateSurface(new MyBitmap("hsi2.png", 178, 456, 184, 52), 256-92, 274-26, PlaneData.NAV1_SEL_RADIAL, 1, 256, 274, 0, 0, 360, 360, 0), // CDI, scale
					new HSINeedleDeflection(new MyBitmap("hsi2.png", 418, 172, 16, 148), 256-8, 274-74, PlaneData.NAV1_SEL_RADIAL, PlaneData.NAV1_DEFLECTION, 0), // CDI, deflection
					new HSIInRange(new MyBitmap("hsi2.png", 408, 64, 100, 52), 100, 128, PlaneData.NAV1_FROM, PlaneData.NAV1_TO),
					new StaticSurface(new MyBitmap("hsi1.png", -1, -1, -1, -1), 0, 0)
				});
		case CLOCK:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("clock.png", -1, -1, -1, -1), 0, 20),
					new ClockNumber(null, PlaneData.SECONDS, 256, 120, face)
			});
		default:
			MyLog.w(Cessna172.class.getSimpleName(), "Instrument not available: " + type);
			return null;
		}
	}
	
	private static String starter = "/controls/switches/starter";
	
	public static ArrayList<Instrument> getInstrumentPanel(Context context) {
		
    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    	String hiType = sp.getString("hi_type", "HI");
    	String alternateIns = sp.getString("additional_instrument", "manifold");
    	String asiType = sp.getString("asi_type", "asi160");
    	
    	// select the property for the starter, depending on the options
    	starter = (sp.getBoolean("starter_property", true)?"/controls/switches/starter":"/controls/engines/engine/starter");
		
		final ArrayList<Instrument> instruments = new ArrayList<Instrument>();
		if (asiType.equals("asi160")) {
			instruments.add(Cessna172.createInstrument(InstrumentType.SPEED, context, 1, 0));
		} else {
			instruments.add(Cessna337.createInstrument(InstrumentType.SPEED, context, 1, 0));
		}
		instruments.add(Cessna172.createInstrument(InstrumentType.ATTITUDE, context, 2, 0));
		instruments.add(Cessna172.createInstrument(InstrumentType.ALTIMETER, context, 3, 0));
		if ( hiType.equals("HSI")) {
			if (alternateIns.equals("radar")) {
				instruments.add(Cessna337.createInstrument(InstrumentType.RADAR, context, 4, 0));
			} else if (alternateIns.equals("manifold")) {
				instruments.add(Cessna172.createInstrument(InstrumentType.MANIFOLD, context, 4, 0));
			}
		} else {
			instruments.add(Cessna172.createInstrument(InstrumentType.NAV1, context, 4, 0));
		}
		instruments.add(Cessna172.createInstrument(InstrumentType.TURN_RATE, context, 1, 1));
		if (hiType.equals("HSI")) {
			instruments.add(Cessna172.createInstrument(InstrumentType.HSI1, context, 2, 1));
		} else if (hiType.equals("RMI")) {
			instruments.add(Cessna337.createInstrument(InstrumentType.HEADING, context, 2, 1));
		} else {
			instruments.add(Cessna172.createInstrument(InstrumentType.HEADING, context, 2, 1));
		}
		instruments.add(Cessna172.createInstrument(InstrumentType.CLIMB_RATE, context, 3, 1));
		instruments.add(Cessna172.createInstrument(InstrumentType.NAV2, context, 4, 1));

		instruments.add(Cessna172.createInstrument(InstrumentType.RPM, context, 1, 2));
		if ( hiType.equals("RMI")) {
			if (alternateIns.equals("radar")) {
				instruments.add(Cessna337.createInstrument(InstrumentType.RADAR, context, 4, 2));
			} else if (alternateIns.equals("manifold")) {
				instruments.add(Cessna172.createInstrument(InstrumentType.MANIFOLD, context, 4, 2));
			}
		} else {
			instruments.add(Cessna172.createInstrument(InstrumentType.ADF, context, 4, 2));
		}
		
		instruments.add(Cessna172.createInstrument(InstrumentType.OIL_TEMP, context, 0.2f, 0));
		instruments.add(Cessna172.createInstrument(InstrumentType.FUEL, context, 0.2f, 1));
//		instruments.add(Cessna172.createInstrument(InstrumentType.EGT, context, 0.5f, 0));
//		instruments.add(Cessna172.createInstrument(InstrumentType.BATT, context, 0.2f, 0));

		
		instruments.add(Cessna172.createInstrument(InstrumentType.SWITCHES, context, 2, 2));
		instruments.add(Cessna172.createInstrument(InstrumentType.TRIMFLAPS, context, 3, 2.5f));
		instruments.add(Cessna172.createInstrument(InstrumentType.DME, context, 2, 2.5f));
		
		instruments.add(Cessna172.createInstrument(InstrumentType.MAGNETS_STARTER, context, 0, 2));

		return instruments;
	}
}

/** The airspeed instrument that I chose for C172 is not linear.
 * This surface rotates the handle in a not linear scale using a polynomial to adjust the curve
 * that was calculated using octave.
 */
class C172AirSpeedSurface extends RotateSurface {

	public C172AirSpeedSurface(MyBitmap bitmap,
			float x, float y,
			int pdIdx, float rscale,
			int rcx, int rcy,
			float min, float amin, float max, float amax) {
		super(bitmap, x, y, pdIdx, rscale, rcx, rcy, min, amin, max, amax);
	}

	@Override
	protected float getRotationAngle(PlaneData pd) {
		float v = pd.getFloat(pdIdx);
		if (v < 40) {
			// from 0 to 40: approximate to a linear behavior: 0=0º, 40=20º
			// this simplifies the polynomial and in any case, speeds under 40knots are uncommon
			return v/2;
		} else {
			// curve adjustment using octave:
			// x=[40:20:200]; 
			// y=[20,70,120,160,205,240,270,290,310]; (angles calculated with gimp on speed1.png)
			// p=polyfit(x,y,2);
			// ans = -6.1147e-03   3.3009e+00  -1.0452e+02
			return -0.0061147f * v * v + 3.3f * v - 104.5f;
		}
	}
}

// a rotate surface that rotates in addition to the angle that it needs, the current heading and a offset
// Used in the HSI of the NAV heading
class RelativeToHeadingRotateSurface extends RotateSurface {
	private float roffset;
	
	public RelativeToHeadingRotateSurface(MyBitmap bitmap, float x, float y,
			int pdIdx, float rscale, int rcx, int rcy, float min, float amin,
			float max, float amax, float roffset) {
		super(bitmap, x, y, pdIdx, rscale, rcx, rcy, min, amin, max, amax);
		this.roffset = roffset;
	}
	
	@Override
	protected float getRotationAngle(PlaneData pd) {
		float vparent = super.getRotationAngle(pd);
		return vparent - planeData.getFloat(PlaneData.HEADING) + roffset;
	}	
}


class C172HIBug extends CalibratableRotateSurface {

	public C172HIBug(MyBitmap bitmap, float x, float y, String prop,
			boolean wrap, int propIdx, int rcx, int rcy, float min, float amin,
			float max, float amax) {
		super(bitmap, x, y, prop, wrap, propIdx, rcx, rcy, min, amin, max, amax);
	}

	/**
	 * @param v value angle
	 * @return The angle to rotate the drawable to match that value
	 */
	protected float getRotationAngle(float v) {
		float vparent = super.getRotationAngle(v);
		return vparent - planeData.getFloat(PlaneData.HEADING);
	}
}


//class HSINeedle extends RotateSurface {
//	private int pdIdx2;
//	private float roffset;
//	
//	public HSINeedle(MyBitmap bitmap, float x, float y, int pdIdx, int pdIdx2, float roffset) {
//		super(bitmap, x, y, pdIdx, 1, 256, 274, 0, 0, 360, 360);
//		this.pdIdx2 = pdIdx2;
//		this.roffset = roffset;
//	}
//	
//	protected float getRotationAngle(PlaneData pd) {
//		float v = super.getRotationAngle(pd);
//		return v - pd.getFloat(this.pdIdx2)+ roffset;
//	}
//}
class HSINeedleDeflection extends RelativeToHeadingRotateSurface {
	private int pdIdx2;
	
	public HSINeedleDeflection(MyBitmap bitmap, float x, float y, int pdIdx, int pdIdx2, float roffset) {
		super(bitmap, x, y, pdIdx, 1, 256, 274, 0, 0, 360, 360, roffset);
		this.pdIdx2 = pdIdx2;
	}
	
	@Override
	public void onDraw(Canvas c) {
		if (planeData == null || !planeData.hasData() || bitmap == null || bitmap.getScaledBitmap() == null) {
			return;
		}
		
		m.reset();
		final float realscale = parent.getScale() * parent.getGridSize();
		final float col = parent.getCol();
		final float row = parent.getRow();
		// max deflexion is 10, and it is a translation of 80 pixels
		m.setTranslate(
				(col + relx + planeData.getFloat(this.pdIdx2) * 8 / DEFAULT_SURFACE_SIZE) * realscale,
				(row + rely ) * realscale);
		m.postRotate(
				getRotationAngle(this.planeData),
				(col + rcx / DEFAULT_SURFACE_SIZE ) * realscale,
				(row + rcy / DEFAULT_SURFACE_SIZE ) * realscale);
		c.drawBitmap(bitmap.getScaledBitmap(), m, null);
	}
}
class HSIInRange extends StaticSurface {
	private int indexTo, indexFrom;

	public HSIInRange(MyBitmap bitmap, float x, float y, final int indexTo, final int indexFrom) {
		super(bitmap, x, y);
		this.indexFrom = indexFrom;
		this.indexTo = indexTo;
	}
	
	@Override
	public void onDraw(Canvas c) {
		// draw only if the to/flag are not set
		if (planeData != null && !(planeData.getBool(this.indexTo) || planeData.getBool(this.indexFrom))) {
			super.onDraw(c);
		}
	}
}

/** The long hand of the altimeter shows the modulus of the altitude.
 * Modules is not directly sent by FlightGear, and this class
 * manages the calculus.
 */
class C172AltimeterLongHandSurface extends RotateSurface {

	public C172AltimeterLongHandSurface(MyBitmap bitmap,
			float x, float y,
			int pdIdx, float rscale,
			int rcx, int rcy,
			float min, float amin, float max, float amax) {
		super(bitmap, x, y, pdIdx, rscale, rcx, rcy, min, amin, max, amax);
	}
	
	protected float getRotationAngle(PlaneData pd) {
		float v = pd.getFloat(pdIdx);
		return (v % 1000) * 360 /1000;
	}
}

/** A special surface to draw the attitude. See usage example in the C172 */
class C172AtiSurface extends Surface {
	private Matrix matrix;

	public C172AtiSurface(MyBitmap bitmap, float x, float y) {
		super(bitmap, x, y);
		matrix = new Matrix();
	}
	@Override
	public void onDraw(Canvas c) {
		if (planeData == null || bitmap == null || bitmap.getScaledBitmap() == null) {
			return;
		}
		
		// draw pitch
		matrix.reset();
		float col = parent.getCol();
		float row = parent.getRow();
		float gridSize = parent.getGridSize();
		float scale = parent.getScale();
		// translate 22 /  pixels each 5 degrees
		float roll = planeData.getFloat(PlaneData.ROLL);
		if (roll > 60) {
			roll = 60;
		} else if (roll < -60) {
			roll = -60;
		}
		float pitch = planeData.getFloat(PlaneData.PITCH);
		if (pitch > 30) {
			pitch = 30;
		} else if (pitch < -30) {
			pitch = -30;
		}
		Bitmap b = bitmap.getScaledBitmap();
		matrix.postTranslate(((0.5f + col) * gridSize) * scale - b.getWidth() / 2, ((0.5f + row) * gridSize + pitch * (22 * gridSize/ 512) / 5) * scale - b.getHeight() / 2);
		matrix.postRotate(-roll, ((0.5f + col) * gridSize) * scale, ((0.5f + row) * gridSize) * scale);
		c.drawBitmap(b, matrix, null);
	}
}


/** A special surface to draw the flag from/to in a VOR. See an example in the C172 */
class C172FromToGSSurface extends Surface {
	private int nav_to, nav_from, gs; // position of this flags in PlaneData
	// if gs == -1, this surface manages to/from flags
	// if gs != -1, this surface manages the gs (to/from are ignored)
	
	private Rect rectFrom, rectTo, rectGs, rectPos;

	public C172FromToGSSurface(MyBitmap bitmap, float x, float y, int nav_to, int nav_from, int gs) {
		super(bitmap, x, y);
		this.nav_from = nav_from;
		this.nav_to = nav_to;
		this.gs = gs;
		rectFrom = rectTo = rectGs = rectPos = null;
	}
	
	@Override
	public void onBitmapChanged() {
		final float col = parent.getCol();
		final float row = parent.getRow();
		final float scale = parent.getScale();
		final float realscale = parent.getScale() * parent.getGridSize();
		final int left = (int) ((col + relx) * realscale);
		final int top = (int) ((row + rely) * realscale);
		
		Bitmap b = this.bitmap.getScaledBitmap();
		rectFrom = new Rect(b.getWidth() / 3, 0, 2 * b.getWidth() / 3, b.getHeight());
		rectTo = new Rect(0, 0, b.getWidth() / 3, b.getHeight());
		rectGs = new Rect(2 * b.getWidth() / 3, 0, b.getWidth(), b.getHeight());
		rectPos = new Rect(left, top, (int)(left + b.getWidth() / 3 * scale), (int)(top + b.getHeight() * scale));
	}
	
	@Override
	public void onDraw(Canvas c) {
		if (planeData == null || bitmap == null || bitmap.getScaledBitmap() == null) {
			return;
		}

		Bitmap b = bitmap.getScaledBitmap();
		
		if (gs == -1) {
			if (planeData.getBool(nav_to)) {
				c.drawBitmap(b, rectTo, rectPos, null);
			} else if (planeData.getBool(nav_from)) {
				c.drawBitmap(b,	rectFrom, rectPos, null);
			}
		} else {
			if (planeData.getBool(gs)) {
				c.drawBitmap(b, rectGs, rectPos, null);
			}
		}
	}
}

class DMENumber extends StaticSurface {
	private int idxDME;
	private Typeface face;
	private Paint font;
	private StringBuffer sb = null;
	
	// Bitmap is not currently used
	public DMENumber(MyBitmap bitmap, float x, float y, int idxDME, Typeface face) {
		super(bitmap, x, y);
		this.idxDME = idxDME;
		this.face = face;
	}
	
	@Override
	public void onBitmapChanged() {
		font = new Paint();
		font.setColor(Color.RED);
		font.setTypeface(this.face);
		font.setTextSize((parent.getScale() * parent.getGridSize()) / 16); // 8); //bitmap.getScaledBitmap().getHeight());
		
		sb = new StringBuffer();
	}
	
	public void onDraw(Canvas c) {
		if (planeData == null) { // || bitmap == null || bitmap.getScaledBitmap() == null) {
			return;
		}
		
		float distance = planeData.getFloat(this.idxDME);
		
		// do not show anything if distance < 0.1
		if (distance < 0.1) {
			return;
		}
		
		int hundreds = (int)((distance / 100 ) % 100);
		int tens =  (int)((distance / 10 ) % 10);
		int units = (int)(distance % 10);
		int firstdecimal = (int)((distance * 10) % 10);
		
		
		final float realscale = parent.getScale() * parent.getGridSize();
		final int left = (int) ((this.getParent().getCol() + relx) * realscale);
		final int top = (int) ((this.getParent().getRow() + rely) * realscale);
		
		// draw always first decimal and units
		if (sb != null) {
			sb.setLength(0); // clear the stringbuffer
			if (hundreds > 0) sb.append(hundreds); else sb.append(" ");
			if (tens > 0) sb.append(tens); else sb.append(" ");
			sb.append(units).append(".").append(firstdecimal);
			c.drawText(sb.toString(), left, top, font);
		}
		
	}
}

class ClockNumber extends StaticSurface {
	private Typeface face;
	private Paint font;
	private StringBuffer sb = null;
	private int idx;
	
	// Bitmap is not currently used
	public ClockNumber(MyBitmap bitmap, int idx, float x, float y, Typeface face) {
		super(bitmap, x, y);
		this.face = face;
		this.idx = idx;
	}
	
	@Override
	public void onBitmapChanged() {
		font = new Paint();
		font.setColor(Color.RED);
		font.setTypeface(this.face);
		font.setTextSize((parent.getScale() * parent.getGridSize()) / 16); // 8); //bitmap.getScaledBitmap().getHeight());
		font.setTextAlign(Align.CENTER);
		
		sb = new StringBuffer();
	}
	
	public void onDraw(Canvas c) {
		if (planeData == null) { // || bitmap == null || bitmap.getScaledBitmap() == null) {
			return;
		}
		
		// draw always first decimal and units
		if (sb != null) {
			int seconds = planeData.getInt(this.idx);
			
			int hours = seconds / (60 * 60);
			int minutes = seconds / 60 - hours * 60;
			seconds = seconds % 60;
			
			final float realscale = parent.getScale() * parent.getGridSize();
			final int left = (int) ((this.getParent().getCol() + relx) * realscale);
			final int top = (int) ((this.getParent().getRow() + rely) * realscale);

			
			sb.setLength(0); // clear the stringbuffer
			sb.append(hours).append(".").append(minutes).append(".").append(seconds);
			c.drawText(sb.toString(), left, top, font);
		}
		
	}
}

class C172GS1 extends SlippingSurface {

	public C172GS1(MyBitmap bitmap, float rotation, int prop, float min,
			int xmin, int ymin, float max, int xmax, int ymax) {
		super(bitmap, rotation, prop, min, xmin, ymin, max, xmax, ymax);
	}
	
	@Override
	public void onDraw(Canvas c) {
		if (planeData == null || bitmap == null || bitmap.getScaledBitmap() == null) {
			return;
		}
		
		// draw only if the GS1 is in range
		if (planeData.getBool(PlaneData.GS1_INRANGE)) {
			super.onDraw(c);
		}
	}
}


