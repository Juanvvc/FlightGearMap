package com.juanvvc.flightgear.panels;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;

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

/** Distributed instruments as in a Cessna 337 Skymaster */
public class Cessna337 {
	
	public static Instrument createInstrument(InstrumentType type, Context context, float col, float row) {
		MyBitmap hand1 = new MyBitmap("misc1.png", 380, 10, 40, 270); // used in the ASI (long hand)
		MyBitmap hand3 = new MyBitmap("misc2.png", 4, 200, 140, 24); // used in small instruments
		MyBitmap hand4 = new MyBitmap("misc2.png", 40, 200, 100, 24); // used in even smaller instruments
		MyBitmap headings = new MyBitmap("nav2.png", -1, -1, -1, -1);
		MyBitmap switches1 = new MyBitmap("switches.png", 0, 0, 128, 368);
		MyBitmap switches2 = new MyBitmap("switches.png", 128, 0, 128, 368);
		MyBitmap switches3 = new MyBitmap("switches.png", 258, 0, 122, 306);

		
		switch (type) {
		case SPEED:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("speed2.png", -1, -1, -1, -1), 0, 0),
					new RotateSurface(hand1, 236, 56, PlaneData.SPEED, 1, 256, 256, 0, 0, 200, 350),
					new CalibratableRotateSurface(new MyBitmap("speed4.png", -1, -1, -1, -1), 256-228, 256-228, null, true, -1, 256, 256, 0, 0, 360, -360),
				});
		case RPM:
			return new Instrument(col, row, context, new Surface[] {
				new StaticSurface(new MyBitmap("rpm2.png", -1, -1, -1, -1), 0, 0),
				new RotateSurface(hand1, 276-20, 56, PlaneData.RPM, 1, 276, 256, 500, 270-60, 3000, 270+60),
				new RotateSurface(hand1, 236-20, 56, PlaneData.RPM2, 1, 236, 256, 500, 90+60, 3000, 90-60),
				new StaticSurface(new MyBitmap("misc4.png", 160, 0, 160, 160), 256-80, 256-80)
			});
		case MANIFOLD:
			return new Instrument(col, row, context, new Surface[] {
				new StaticSurface(new MyBitmap("manifold2.png", -1, -1, -1, -1), 0, 0),
				new RotateSurface(hand3, 276, 244, PlaneData.MANIFOLD, 1, 256, 256, 10, 180-70, 32, 180+60),
				new RotateSurface(hand3, 276, 244, PlaneData.MANIFOLD2, 1, 256, 256, 10, 70, 32, -60),
				new StaticSurface(new MyBitmap("misc4.png", 0, 0, 160, 160), 256-80, 256-80)
			});
		case HEADING: // Actually, it is a RMI
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("nav3.png", 0, 190, 320, 320), 256-160, 256-160),
					new RotateSurface(new MyBitmap("nav4.png", 398, 50, 54, 324), 256-27, 256-162, PlaneData.ADF_DEFLECTION, 1, 256, 256, 0, 0, 720, 720),  // for some reason, the ADF instrument shows headings from 0 to 720
					new RotateSurface(headings, 0, 0, PlaneData.HEADING, 1, 256, 256, 0, 0, 360, -360),
					new C172HIBug(new MyBitmap("misc2.png", 242, 290, 44, 44), 256-22, 36, "/autopilot/settings/heading-bug-deg", true, -1, 256, 256, -180, -180, 180, 180),
					new C172HIBug(new MyBitmap("nav4.png", 248, 200, 32, 300), 236, 100, null, false, PlaneData.NAV1_HEADING, 256, 256, 0, 0, 360, 360),
					new StaticSurface(new MyBitmap("nav5.png", -1, -1, -1, -1), 0, 0),
					new StaticSurface(new MyBitmap("nav1.png", -1, -1, -1, -1), 0, 0)
				});
		case RADAR:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("radar1.png", -1, -1, -1, -1), 0, 0),
					new CalibratableRotateSurface(new MyBitmap("misc2.png", 242, 290, 44, 44), 256-22, 12, null, false, -1, 256, 256, 0, 0, 240, 240),
					new C337RadarSurface(hand1, 236, 56, PlaneData.ALTITUDE_AGL, 1, 256, 256, 0, 0, 2500, 240),
					new StaticSurface(new MyBitmap("radar2.png", -1, -1, -1, -1), 0, 0)
			});
		case FUEL:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("Fuel-Oil-base.png", 0, 0, 512, 204), 0, 0),
					new RotateSurface(hand4, 154, 142, PlaneData.FUEL1, 1, 154, 154, 0, -150, 75, -30),
					new RotateSurface(hand4, 400, 142, PlaneData.FUEL2, 1, 400, 154, 0, -150, 75, -30)
			});
		case OIL_PRESS:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("Fuel-Oil-base.png", 0, 212, 512, 204), 0, 0),
					new RotateSurface(hand4, 120, 142, PlaneData.OIL_PRESS, 1, 120, 154, 0, -145, 100, -35),
					new RotateSurface(hand4, 368, 142, PlaneData.OIL2_PRESS, 1, 368, 154, 0, -145, 100, -35)
			});
		case CYL_TEMP:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("CHT-Oil-base.png", 0, 0, 512, 204), 0, 0),
					new RotateSurface(hand4, 154, 142, PlaneData.CHT1_TEMP, 1, 154, 154, 200, -145, 500, -35),
					new RotateSurface(hand4, 400, 142, PlaneData.CHT2_TEMP, 1, 400, 154, 200, -145, 500, -35)
			});
		case OIL_TEMP:
			return new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("CHT-Oil-base.png", 0, 212, 512, 204), 0, 0),
					new RotateSurface(hand4, 120, 142, PlaneData.OIL_TEMP, 1, 120, 154, 75, -145, 260, -35),
					new RotateSurface(hand4, 368, 142, PlaneData.OIL2_TEMP, 1, 368, 154, 75, -145, 260, -35),
					
			});
		case SWITCHES:
			return new Instrument(col, row, context, new Surface[] {
					new SwitchSurface(switches2, 0, 0, "/controls/engines/engine[0]/master-bat", "BATT"),
					new SwitchSurface(switches2, 128, 0, "/controls/engines/engine[0]/master-alt", "ALT1"),
					new SwitchSurface(switches2, 256, 0, "/controls/engines/engine[1]/master-alt", "ALT2"),
					new SwitchSurface(switches1, 384, 0, "/controls/switches/master-avionics", "AVI"),
					

					new SwitchSurface(switches3, 512+0, 0, "/controls/lighting/nav-lights", "NAV"),
					new SwitchSurface(switches3, 512+128, 0, "/controls/lighting/beacon", "BCN"),
					new SwitchSurface(switches3, 512+256, 0, "/controls/lighting/taxi-light", "TAX"),
					new SwitchSurface(switches3, 512+384, 0, "/controls/lighting/landing-lights", "LNG"),
				});

		default:
			MyLog.w(SenecaII.class.getSimpleName(), "Instrument not available: " + type);
			return null;
		}
	}
	
	public static ArrayList<Instrument> getInstrumentPanel(Context context) {
		final ArrayList<Instrument> instruments = new ArrayList<Instrument>();
		instruments.add(Cessna337.createInstrument(InstrumentType.SPEED, context, 1, 0));
		instruments.add(Cessna172.createInstrument(InstrumentType.ATTITUDE, context, 2, 0));
		instruments.add(Cessna172.createInstrument(InstrumentType.ALTIMETER, context, 3, 0));
		instruments.add(Cessna172.createInstrument(InstrumentType.NAV1, context, 4, 0));
		instruments.add(Cessna172.createInstrument(InstrumentType.TURN_RATE, context, 1, 1));
		instruments.add(Cessna337.createInstrument(InstrumentType.HEADING, context, 2, 1));
		instruments.add(Cessna172.createInstrument(InstrumentType.CLIMB_RATE, context, 3, 1));
		instruments.add(Cessna172.createInstrument(InstrumentType.NAV2, context, 4, 1));

		instruments.add(Cessna337.createInstrument(InstrumentType.MANIFOLD, context, 0, 0));
		instruments.add(Cessna337.createInstrument(InstrumentType.RPM, context, 0, 1));
		instruments.add(Cessna337.createInstrument(InstrumentType.RADAR, context, 4, 2));
		
		instruments.add(Cessna337.createInstrument(InstrumentType.FUEL, context, 0, 2));
		instruments.add(Cessna337.createInstrument(InstrumentType.OIL_PRESS, context, 1, 2));
		instruments.add(Cessna337.createInstrument(InstrumentType.CYL_TEMP, context, 0, 2.5f));
		instruments.add(Cessna337.createInstrument(InstrumentType.OIL_TEMP, context, 1, 2.5f));
		
		instruments.add(Cessna337.createInstrument(InstrumentType.SWITCHES, context, 2, 2));
		instruments.add(Cessna172.createInstrument(InstrumentType.TRIMFLAPS, context, 3, 2.5f));
		instruments.add(Cessna172.createInstrument(InstrumentType.DME, context, 2, 2.5f));
		return instruments;
	}
}

/** The radar altitude instrument that I chose is not linear.
 * This surface rotates the handle according to some predefined values.
 */
class C337RadarSurface extends RotateSurface {

	public C337RadarSurface(MyBitmap bitmap,
			float x, float y,
			int pdIdx, float rscale,
			int rcx, int rcy,
			float min, float amin, float max, float amax) {
		super(bitmap, x, y, pdIdx, rscale, rcx, rcy, min, amin, max, amax);
	}

	@Override
	protected float getRotationAngle(PlaneData pd) {
		float v = pd.getFloat(pdIdx);
		if (v < 500) { // from 0 to 500, angles from 0 to 160
			return 160.0f * v/500;
		} else if(v < 1000) { // from 500 to 1500, angles from 160 to 180.
			return 160 + 20f * (v - 500) / 500;
		} else if(v < 1500) { // from 1000 to 1500, angles from 180 to 200
			return 180 + 20f * (v - 1000) / 500;
		} else if(v < 2000) { // from 1500 to 2000, angles from 200 to 220
			return 200 + 20f * (v - 1500) / 500;
		} else if(v < 2500) { // from 2000 to 2500, angles from 220 to 240
			return 220 + 20f * (v - 2000) / 500;
		} else { // other values: saturated
			return 240;
		}
	}
}
