package com.juanvvc.flightgear.panels;

import java.util.ArrayList;

import android.content.Context;

import com.juanvvc.flightgear.PlaneData;
import com.juanvvc.flightgear.instruments.CalibratableRotateSurface;
import com.juanvvc.flightgear.instruments.Instrument;
import com.juanvvc.flightgear.instruments.InstrumentType;
import com.juanvvc.flightgear.instruments.RotateSurface;
import com.juanvvc.flightgear.instruments.SlippingSurface;
import com.juanvvc.flightgear.instruments.StaticSurface;
import com.juanvvc.flightgear.instruments.Surface;
import com.juanvvc.flightgear.instruments.SwitchSurface;
import com.juanvvc.flightgear.instruments2.AtiSurface;
import com.juanvvc.flightgear.instruments2.FromToGSSurface;

/** Distributed instruments as in a Cessna 172 */
public class Cessna172 {
	
	public static Instrument createInstrument(InstrumentType type, Context context, float col, float row) {
		switch (type) {

		case SPEED:
			return new Instrument(col, row, context, new Surface[] {
				new StaticSurface("speed.png", 0, 0),
				new C172AirSpeedSurface("hand1.png", 236, 56, PlaneData.SPEED, 1, 256, 256, 0, 0, 200, 320)
			});
		case ATTITUDE:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface("ati0.png", 0, 0),
					new AtiSurface("ati1.png", 70, 138),
					new RotateSurface("ati2.png", 23, 23, PlaneData.ROLL, 1, 256, 256, -180, 180, 180, -180),
					new StaticSurface("ati3.png", 0, 0)
				});
		case ALTIMETER:
			return new Instrument(col, row, context, new Surface[] {
					new CalibratableRotateSurface("alt0.png", 0, 0, "/instrumentation/altimeter/setting-inhg", 1, true, -1, 256, 256, 27.9f, 210, 31.5f, -150),
					new StaticSurface("alt1.png", 0, 0),
					new RotateSurface("hand2.png", 236, 56, PlaneData.ALTITUDE, 0.001f, 256, 256, 0, 0, 30, 3 * 360),
					new C172AltimeterLongHandSurface("hand1.png", 236, 56, PlaneData.ALTITUDE, 1, 256, 256, 0, 0, 10, 360)
				});
		case NAV1:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface("nav6.png", 0, 0),
					new CalibratableRotateSurface("nav2.png", 0, 0, "/instrumentation/nav/radials/selected-deg", 1, true, -1, 256, 256, 0, 0, 360, -360),
					new FromToGSSurface("nav4.png", 310, 210, PlaneData.NAV1_TO, PlaneData.NAV1_FROM, -1),
					new FromToGSSurface("nav4.png", 185, 210, -1, -1, PlaneData.GS1_INRANGE),
					new RotateSurface("hand5.png", 245, 100, PlaneData.NAV1_DEFLECTION, 1, 256, 100, -10, 25, 10, -25),
					new RotateSurface("hand5.png", 105, 266, PlaneData.GS1_DEFLECTION, 1, 105, 266, -1, -65, 1, -115),
					new StaticSurface("nav3.png", 0, 0)
				});	
		case NAV2:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface("nav1.png", 0, 0),
					new CalibratableRotateSurface("nav2.png", 0, 0, "/instrumentation/nav[1]/radials/selected-deg", 1, true, -1, 256, 256, 0, 0, 360, -360),
					new FromToGSSurface("nav4.png", 308, 220, PlaneData.NAV2_TO, PlaneData.NAV2_FROM, -1),
					new RotateSurface("hand5.png", 245, 100, PlaneData.NAV2_DEFLECTION, 1, 256, 100, -10, 25, 10, -25),
					new StaticSurface("nav3.png", 0, 0)
				});
		case ADF:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface("black.png", 0, 0),
					new CalibratableRotateSurface("nav2.png", 0, 0, "/instrumentation/adf/rotation-deg", 1, true, -1, 256, 256, 0, 0, 360, -360),
					new RotateSurface("hand4.png", 236, 100, PlaneData.ADF_DEFLECTION, 1, 256, 256, 0, 0, 360, 360),
					new StaticSurface("nav5.png", 0, 0),
					new StaticSurface("nav3.png", 0, 0)
				});
		case HEADING:
			return new Instrument(col, row, context, new Surface[] {
					new CalibratableRotateSurface("hdg1.png", 0, 0, "/instrumentation/heading-indicator/indicated-heading-deg", 1, true, PlaneData.HEADING, 256, 256, 0, 0, 360, -360),
					new StaticSurface("hdg2.png", 0, 0)
				});
		case TURN_RATE:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface("trn0.png", 0, 0),
					new RotateSurface("slip.png", 230, 300, PlaneData.SLIP, 1, 256, 0, -1, -25, 1, 25),
					new StaticSurface("trn1.png", 0, 0),
					new RotateSurface("turn.png", 94, 219, PlaneData.TURN_RATE, 1, 256, 256, -4, -80, 4, 80),
				});
		case CLIMB_RATE:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface("climb.png", 0, 0),
					new RotateSurface("hand1.png", 236, 56, PlaneData.CLIMB_RATE, 1, 256, 256, -2000, -265, 2000, 85)
				});
		case RPM:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface("rpm.png", 0, 0),
					new RotateSurface("hand1.png", 236, 56, PlaneData.RPM, 1, 256, 256, 0, -125, 3500, 125)
				});
		case FUEL:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface("fuel1.png", 0, 0),
					new RotateSurface("hand3.png", -20, 7, PlaneData.FUEL1, 1, 0, 230, 0, 160, 26, 20),
					new RotateSurface("hand3.png", 268, 7, PlaneData.FUEL2, 1, 288, 230, 0, -160, 26, -20)
				});
		case OIL:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface("oil1.png", 0, 0),
					new RotateSurface("hand3.png", -20, 12, PlaneData.OIL_TEMP, 1, 0, 230, 75, 160, 245, 20),
					new RotateSurface("hand3.png", 268, 12, PlaneData.OIL_PRESS, 1, 288, 230, 0, -160, 115, -20)
				});
		case BATT:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface("battery-c172p.png", 0, 0),
					new RotateSurface("hand3.png", -20, 12, PlaneData.AMP, 1, 0, 230, -40, 145, 40, 35),
					new RotateSurface("hand3.png", 268, 12, PlaneData.VOLT, 1, 288, 230, 0, -145, 40, -35)
				});
		case TRIMFLAPS:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface("trimflaps.png", 65, 10),
					new SlippingSurface("hand3.png", 90, PlaneData.ELEV_TRIM, -1, 280, 394, 1, 280, 26),
					new SlippingSurface("hand3.png", -90, PlaneData.FLAPS, 0, 200, 66, 1, 200, 434)
				});
		case SWITCHES:
			return new Instrument(col, row, context, new Surface[] {
					new SwitchSurface("switches.png", 0, 152, "/controls/anti-ice/pitot-heat", "PTO"),
					new SwitchSurface("switches.png", 128, 152, "/controls/lighting/nav-lights", "NAV"),
					new SwitchSurface("switches.png", 256, 0, "/controls/lighting/taxi-light", "TAX"),
					new SwitchSurface("switches.png", 256, 152, "/controls/lighting/beacon", "BCN"),
					new SwitchSurface("switches.png", 384, 0, "/controls/lighting/landing-lights", "LNG"),
					new SwitchSurface("switches.png", 384, 152, "/controls/lighting/strobe", "DTR"),
				});
		default:
			return null;
		}
	}
	
	public static ArrayList<Instrument> getInstrumentPanel(Context context) {
		final ArrayList<Instrument> instruments = new ArrayList<Instrument>();
		instruments.add(Cessna172.createInstrument(InstrumentType.SPEED, context, 1, 0));
		instruments.add(Cessna172.createInstrument(InstrumentType.ATTITUDE, context, 2, 0));
		instruments.add(Cessna172.createInstrument(InstrumentType.ALTIMETER, context, 3, 0));
		instruments.add(Cessna172.createInstrument(InstrumentType.NAV1, context, 4, 0));
		instruments.add(Cessna172.createInstrument(InstrumentType.TURN_RATE, context, 1, 1));
		instruments.add(Cessna172.createInstrument(InstrumentType.HEADING, context, 2, 1));
		instruments.add(Cessna172.createInstrument(InstrumentType.CLIMB_RATE, context, 3, 1));
		instruments.add(Cessna172.createInstrument(InstrumentType.NAV2, context, 4, 1));

		instruments.add(Cessna172.createInstrument(InstrumentType.RPM, context, 1, 2));
		instruments.add(Cessna172.createInstrument(InstrumentType.ADF, context, 4, 2));
		
		instruments.add(Cessna172.createInstrument(InstrumentType.BATT, context, 0.2f, 0));
		instruments.add(Cessna172.createInstrument(InstrumentType.OIL, context, 0.2f, 1));
		instruments.add(Cessna172.createInstrument(InstrumentType.FUEL, context, 0.2f, 2));
		
		instruments.add(Cessna172.createInstrument(InstrumentType.SWITCHES, context, 2, 2));
		instruments.add(Cessna172.createInstrument(InstrumentType.TRIMFLAPS, context, 3, 2));
		return instruments;
	}
}

/** The airspeed instrument that I chose for C172 is not linear.
 * This surface rotates the handle in a not linear scale using a polynomial to adjust the curve
 * that was calculated using octave.
 */
class C172AirSpeedSurface extends RotateSurface {

	public C172AirSpeedSurface(String file,
			float x, float y,
			int pdIdx, float rscale,
			int rcx, int rcy,
			float min, float amin, float max, float amax) {
		super(file, x, y, pdIdx, rscale, rcx, rcy, min, amin, max, amax);
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

/** The long hand of the altimeter shows the modulus of the altitude.
 * Modules is not directly sent by FlightGear, and this class
 * manages the calculus.
 */
class C172AltimeterLongHandSurface extends RotateSurface {

	public C172AltimeterLongHandSurface(String file,
			float x, float y,
			int pdIdx, float rscale,
			int rcx, int rcy,
			float min, float amin, float max, float amax) {
		super(file, x, y, pdIdx, rscale, rcx, rcy, min, amin, max, amax);
	}
	
	protected float getRotationAngle(PlaneData pd) {
		float v = pd.getFloat(pdIdx);
		return (v % 1000) * 360 /1000;
	}
}