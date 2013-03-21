package com.juanvvc.flightgear.panels;

import java.util.ArrayList;

import android.content.Context;

import com.juanvvc.flightgear.MyBitmap;
import com.juanvvc.flightgear.MyLog;
import com.juanvvc.flightgear.instruments.Instrument;
import com.juanvvc.flightgear.instruments.InstrumentType;
import com.juanvvc.flightgear.instruments.StaticSurface;
import com.juanvvc.flightgear.instruments.Surface;

public class Comms {
	public static ArrayList<Instrument> createInstrument(InstrumentType type, Context context, float col, float row) {
		final MyBitmap kx165back1 = new MyBitmap("kx165-1.png", 0, 0, 512, 512);
		final MyBitmap kx165back2 = new MyBitmap("kx165-1.png", 512, 0, 512, 512);
		final MyBitmap commsnumbers = new MyBitmap("kx165.png", -1, -1, -1 , -1);
		
		ArrayList<Instrument> a = new ArrayList<Instrument>();
		
		switch(type) {
		case COMM1:
			a.add(new Instrument(col, row, context, new Surface[] {
					new StaticSurface(kx165back1, 0, 0)
				}));
			a.add(new Instrument(col+1, row, context, new Surface[] {
					new StaticSurface(kx165back2, 0, 0)
				}));
			return a;
		case COMM2:
			a.add(new Instrument(col, row, context, new Surface[] {
					new StaticSurface(kx165back1, 0, 0)
				}));
			a.add(new Instrument(col+1, row, context, new Surface[] {
					new StaticSurface(kx165back2, 0, 0)
				}));
			return a;
		case COMMADF:
			a.add(new Instrument(col, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("kt50-1.png", 0, 0, 512, 512), 0, 0)
				}));
			a.add(new Instrument(col+1, row, context, new Surface[] {
					new StaticSurface(new MyBitmap("kt50-1.png", 512, 0, 512, 512), 0, 0)
				}));
			return a;
		default:
			MyLog.w("CommPanel", "Instrument is null: " + type);
			return a;
		}
	}
	
	public static ArrayList<Instrument> getInstrumentPanel(Context context) {
		ArrayList<Instrument> instruments = new ArrayList<Instrument>();
		instruments.addAll(Comms.createInstrument(InstrumentType.COMM1, context, 1, 0));
		instruments.addAll(Comms.createInstrument(InstrumentType.COMM2, context, 1, 1));
		instruments.addAll(Comms.createInstrument(InstrumentType.COMMADF, context, 1, 2));
		instruments.add(Cessna172.createInstrument(InstrumentType.NAV1, context, 0, 0));
		instruments.add(Cessna172.createInstrument(InstrumentType.NAV2, context, 0, 1));
		instruments.add(Cessna172.createInstrument(InstrumentType.ADF, context, 0, 2));
		return instruments;
	}
}
