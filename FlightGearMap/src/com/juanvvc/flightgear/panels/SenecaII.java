package com.juanvvc.flightgear.panels;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;

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

public class SenecaII {

	public static ArrayList<Instrument> getInstrumentPanel(Context context) {
		final ArrayList<Instrument> instruments = new ArrayList<Instrument>();
		instruments.add(Cessna337.createInstrument(InstrumentType.SPEED, context, 1, 0));
		instruments.add(Cessna172.createInstrument(InstrumentType.ATTITUDE, context, 2, 0));
		instruments.add(Cessna172.createInstrument(InstrumentType.ALTIMETER, context, 3, 0));
		instruments.add(Cessna337.createInstrument(InstrumentType.RADAR, context, 4, 0));
		instruments.add(Cessna172.createInstrument(InstrumentType.ADF, context, 0, 1));
		instruments.add(Cessna172.createInstrument(InstrumentType.TURN_RATE, context, 1, 1));
		instruments.add(Cessna172.createInstrument(InstrumentType.HSI1, context, 2, 1));
		instruments.add(Cessna172.createInstrument(InstrumentType.CLIMB_RATE, context, 3, 1));
		instruments.add(Cessna172.createInstrument(InstrumentType.NAV2, context, 4, 1));

		instruments.add(Cessna337.createInstrument(InstrumentType.MANIFOLD, context, 3, 2.5f));
		instruments.add(Cessna172.createInstrument(InstrumentType.RPM, context, 2, 2.5f));
		instruments.add(Cessna172.createInstrument(InstrumentType.RPM2, context, 4, 2.5f));
		
		instruments.add(Cessna337.createInstrument(InstrumentType.FUEL, context, 0, 2));
		instruments.add(Cessna337.createInstrument(InstrumentType.OIL_PRESS, context, 1, 2));
		instruments.add(Cessna337.createInstrument(InstrumentType.CYL_TEMP, context, 3, 2));
		instruments.add(Cessna337.createInstrument(InstrumentType.OIL_TEMP, context, 4, 2));
		
		instruments.add(Cessna337.createInstrument(InstrumentType.SWITCHES, context, 0, 2.5f));
		instruments.add(Cessna172.createInstrument(InstrumentType.TRIMFLAPS, context, 0, 0));
		instruments.add(Cessna172.createInstrument(InstrumentType.DME, context, 2, 2));
		return instruments;
	}
}
