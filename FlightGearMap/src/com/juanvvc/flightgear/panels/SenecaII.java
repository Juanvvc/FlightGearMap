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
import com.juanvvc.flightgear.instruments.SwitchSurface;

public class SenecaII {
	
	public static Instrument createInstrument(InstrumentType type, Context context, float col, float row) {
		// Note: only includes those instruments that are different from the ones provided by Cessna172
		switch (type) {
		case HSI1:
			return new Instrument(col, row, context, new Surface[] {
					new CalibratableRotateSurface(new MyBitmap("hdg1.png", -1, -1, -1, -1), 0, 0, "/instrumentation/heading-indicator/indicated-heading-deg", 1, true, PlaneData.HEADING, 256, 256, 0, 0, 360, -360),
					new StaticSurface(new MyBitmap("hsi2.png", 0, 0, 408, 416), 256-204, 256-208),
					new SlippingSurface(new MyBitmap("hsi2.png", 412, 124, 32, 32), 0, PlaneData.GS1_DEFLECTION, -1, 50, 256+85, 1, 50, 256-85),
					new SlippingSurface(new MyBitmap("hsi2.png", 452, 124, 32, 32), 0, PlaneData.GS1_DEFLECTION, -1, 430, 256+85, 1, 430, 256-85),
					new HSINeedle(new MyBitmap("hsi2.png", 444, 164, 32, 64), 256, 100, PlaneData.NAV1_SEL_RADIAL, PlaneData.HEADING, 0),
					new HSINeedle(new MyBitmap("hsi2.png", 444, 164, 32, 64), 256, 412, PlaneData.NAV1_SEL_RADIAL, PlaneData.HEADING, 0),
					new HSINeedle(new MyBitmap("hsi2.png", 178, 456, 184, 52), 256-92, 256-26, PlaneData.NAV1_SEL_RADIAL, PlaneData.HEADING, 0),
					new HSINeedleDeflection(new MyBitmap("hsi2.png", 412, 172, 16, 152), 256, 100, PlaneData.NAV1_SEL_RADIAL, PlaneData.HEADING, PlaneData.NAV1_DEFLECTION, 0),
					new StaticSurface(new MyBitmap("hsi1.png", -1, -1, -1, -1), 0, 0)
				});
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
		instruments.add(Cessna172.createInstrument(InstrumentType.NAV2, context, 4, 1));

		instruments.add(Cessna172.createInstrument(InstrumentType.ADF, context, 0, 1));
		
		instruments.add(Cessna172.createInstrument(InstrumentType.SWITCHES, context, 2, 2));
		instruments.add(Cessna172.createInstrument(InstrumentType.TRIMFLAPS, context, 3, 2));
		return instruments;
	}
}

class HSINeedle extends RotateSurface {
	private int pdIdx2;
	private float roffset;
	
	public HSINeedle(MyBitmap bitmap, float x, float y, int pdIdx, int pdIdx2, float roffset) {
		super(bitmap, x, y, pdIdx, -1, 256, 256, 0, 0, 360, -360);
		this.pdIdx2 = pdIdx2;
		this.roffset = roffset;
	}
	
	protected float getRotationAngle(PlaneData pd) {
		float v = super.getRotationAngle(pd);
		return v - pd.getFloat(this.pdIdx2)+ roffset;
	}
}
class HSINeedleDeflection extends HSINeedle {
	private int pdIdx3;
	public HSINeedleDeflection(MyBitmap bitmap, float x, float y, int pdIdx, int pdIdx2, int pdIdx3, float roffset) {
		super(bitmap, x, y, pdIdx, pdIdx2, roffset);
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
		m.setTranslate(
				(col + relx - planeData.getFloat(this.pdIdx3) * 80 / 512f ) * realscale,
				(row + rely ) * realscale);
		m.postRotate(
				getRotationAngle(this.planeData),
				(col + rcx / 512f ) * realscale,
				(row + rcy / 512f ) * realscale);
		c.drawBitmap(bitmap.getScaledBitmap(), m, null);
	}
}