package com.juanvvc.flightgear.panels;

import java.util.ArrayList;

import android.content.Context;

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

public class SenecaII {
	
	public static Instrument createInstrument(InstrumentType type, Context context, float col, float row) {
		// Note: only includes those instruments that are different from the ones provided by Cessna172
		switch (type) {
//		case NAV2:
//			return new Instrument(col, row, context, new Surface[] {
//					new StaticSurface("nav1.png", 0, 0),
//					new CalibratableRotateSurface("nav2.png", 0, 0, "/instrumentation/nav[1]/radials/selected-deg", 1, true, -1, 256, 256, 0, 0, 360, -360),
//					new FromToGSSurface("nav4.png", 308, 220, PlaneData.NAV2_TO, PlaneData.NAV2_FROM, -1),
//					new RotateSurface("hand5.png", 245, 100, PlaneData.NAV2_DEFLECTION, 1, 256, 100, -10, 25, 10, -25),
//					new StaticSurface("nav3.png", 0, 0)
//				});
//		case HSI1:
//			return new Instrument(col, row, context, new Surface[] {
//					new RotateSurface("hdg1.png", 0, 0, PlaneData.HEADING, 1, 256, 256, 0, 0, 360, -360),
//					new StaticSurface("hdg2.png", 0, 0)
//				});
//		case TRIMFLAPS:
//			return new Instrument(col, row, context, new Surface[] {
//					new StaticSurface("trimflaps.png", 65, 10),
//					new SlippingSurface("hand3.png", 90, PlaneData.ELEV_TRIM, -1, 280, 394, 1, 280, 26),
//					new SlippingSurface("hand3.png", -90, PlaneData.FLAPS, 0, 200, 66, 1, 200, 434)
//				});
//		case SWITCHES:
//			return new Instrument(col, row, context, new Surface[] {
//					new SwitchSurface("switches.png", 0, 152, "/controls/lighting/taxi-light", "TAX"),
//					new SwitchSurface("switches.png", 128, 152, "/controls/lighting/nav-lights", "NAV"),
//					new SwitchSurface("switches.png", 256, 152, "/controls/lighting/beacon", "BCN"),
//					new SwitchSurface("switches.png", 384, 152, "/controls/lighting/landing-lights", "LNG"),
//				});
		default:
			MyLog.w(SenecaII.class.getSimpleName(), "Instrument not available: " + type);
			return null;
		}
	}
	
	public static ArrayList<Instrument> getInstrumentPanel(Context context) {
		final ArrayList<Instrument> instruments = new ArrayList<Instrument>();
//		instruments.add(Cessna172.createInstrument(InstrumentType.SPEED, context, 1, 0));
		instruments.add(Cessna172.createInstrument(InstrumentType.ATTITUDE, context, 2, 0));
		instruments.add(Cessna172.createInstrument(InstrumentType.ALTIMETER, context, 3, 0));
//		instruments.add(Cessna172.createInstrument(InstrumentType.NAV1, context, 4, 0));
		instruments.add(Cessna172.createInstrument(InstrumentType.TURN_RATE, context, 1, 1));
		instruments.add(SenecaII.createInstrument(InstrumentType.HSI1, context, 2, 1));
		instruments.add(Cessna172.createInstrument(InstrumentType.CLIMB_RATE, context, 3, 1));
		instruments.add(SenecaII.createInstrument(InstrumentType.NAV2, context, 4, 1));

		instruments.add(Cessna172.createInstrument(InstrumentType.ADF, context, 0, 1));
		
		instruments.add(Cessna172.createInstrument(InstrumentType.SWITCHES, context, 2, 2));
		instruments.add(Cessna172.createInstrument(InstrumentType.TRIMFLAPS, context, 3, 2));
		return instruments;
	}
}