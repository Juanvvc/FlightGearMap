package com.juanvvc.flightgear.panels;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Region;
import android.graphics.Shader;
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

/** Distributes instruments in a liquid display.
 * TODO: this class is not working (and probably never will) */
public class LiquidDisplay {
	public static Instrument createInstrument(InstrumentType type, Context context, float col, float row) {
		switch(type) {
		case ATTITUDE:
			return new Instrument(col, row, context, new Surface[] {
					new LiquidAtiSurface(new MyBitmap("pitchscale.png", -1, -1, -1, -1), 70, 138),
					new RotateSurface(new MyBitmap("ai.roll.ref.png", -1, -1, -1, -1), 0, 0, PlaneData.ROLL, 1, 256, 256, -180, 180, 180, -180),
					new StaticSurface(new MyBitmap("ai.ref.png", -1, -1, -1, -1), -256, -162)
				});
//		case HSI1:
//			// The center of the instrument is (256, 274)
//			return new Instrument(col, row, context, new Surface[] {
//					new CalibratableRotateSurface(new MyBitmap("liquid.hsi3.png", 0, 0, 328, 328), 256-164, 274-164, "/instrumentation/heading-indicator/indicated-heading-deg", true, PlaneData.HEADING, 256, 274, 0, 0, 360, -360),
//					new StaticSurface(new MyBitmap("liquid.hsi2.png", 0, 0, 408, 416), 256-204, 256-208),
//					new SlippingSurface(new MyBitmap("liquid.hsi2.png", 412, 124, 32, 32), 0, PlaneData.GS1_DEFLECTION, -1, 50, 256+85, 1, 50, 256-85),
//					new SlippingSurface(new MyBitmap("liquid.hsi2.png", 452, 124, 32, 32), 0, PlaneData.GS1_DEFLECTION, -1, 430, 256+85, 1, 430, 256-85),
//					new HSINeedle(new MyBitmap("liquid.hsi2.png", 444, 164, 32, 64), 256-16, 340, PlaneData.NAV1_SEL_RADIAL, PlaneData.HEADING, 180), // CDI, head
//					new HSINeedle(new MyBitmap("liquid.hsi2.png", 484, 172, 20, 68), 256-10, 130, PlaneData.NAV1_SEL_RADIAL, PlaneData.HEADING, 180), // CDI, tail
//					new HSINeedle(new MyBitmap("liquid.hsi2.png", 178, 456, 184, 52), 256-92, 274-26, PlaneData.NAV1_SEL_RADIAL, PlaneData.HEADING, 0), // CDI, scale
//					new HSINeedleDeflection(new MyBitmap("liquid.hsi2.png", 412, 172, 16, 152), 256-8, 274-76, PlaneData.NAV1_SEL_RADIAL, PlaneData.HEADING, PlaneData.NAV1_DEFLECTION, 0), // CDI, deflection
//					new StaticSurface(new MyBitmap("liquid.hsi1.png", -1, -1, -1, -1), 0, 0)
//				});
		case BELTS:
			Typeface face = Typeface.createFromAsset(context.getAssets(), "14_LED1.ttf");
			return new Instrument(col, row, context, new Surface[] {
					new AltitudeBeltSurface(128, 512, face),
					new SpeedBeltSurface(128, 512, face)
			});
		default:
			MyLog.w(LiquidDisplay.class, "Instrument is null: " + type);
			return null;
		}
	}
	
	public static ArrayList<Instrument> getInstrumentPanel(Context context) {
		final ArrayList<Instrument> instruments = new ArrayList<Instrument>();
		instruments.add(createInstrument(InstrumentType.ATTITUDE, context, 0f, 0.0f));
		instruments.add(createInstrument(InstrumentType.HSI1, context, 0f, 1.0f));
		instruments.add(createInstrument(InstrumentType.BELTS, context, 0, 0.15f));
		return instruments;
	}
}

class LiquidAtiSurface extends Surface {
	private Matrix matrix;

	public LiquidAtiSurface(MyBitmap bitmap, float x, float y) {
		super(bitmap, x, y);
		matrix = new Matrix();
	}
	@Override
	public void onDraw(Canvas c) {
		if (planeData == null) {
			return;
		}
		
		Bitmap b = bitmap.getScaledBitmap();
		
		// calculate pitch and matrix
		matrix.reset();
		float col = parent.getCol();
		float row = parent.getRow();
		float gridSize = parent.getGridSize();
		float scale = parent.getScale();
		// translate 25 /  pixels each 5 degrees
		float roll = planeData.getFloat(PlaneData.ROLL);
		if (roll > 60) {
			roll = 60;
		}
		float pitch = planeData.getFloat(PlaneData.PITCH);
		if (pitch > 45) {
			pitch = 45;
		}
		matrix.postTranslate(((0.5f + col) * gridSize) * scale - b.getWidth() / 2, ((0.5f + row) * gridSize + pitch * (25 * gridSize/ 512) / 5) * scale - b.getHeight() / 2);
		matrix.postRotate(-roll, ((0.5f + col) * gridSize) * scale, ((0.5f + row) * gridSize) * scale);
		
		// draw background
		float[] p = {0, 0, 0, b.getHeight()};
		matrix.mapPoints(p);
		Paint paintGrad = new Paint();
		paintGrad.setShader(new LinearGradient(
				p[0], p[1], p[2], p[3],
				new int[]{0xff0000aa, 0xff0000ff, 0xff0000ff, 0xffffffff, 0xffffffff, 0xffA36008, 0xffA36008},
				new float[]{0f, 0.25f, 0.49f, 0.495f, 0.505f, 0.51f, 1.0f},
				Shader.TileMode.CLAMP));
		c.drawPaint(paintGrad);
		
		// draw scale
		Paint shader = new Paint();
		shader.setShader(new LinearGradient(
			((col + 0.5f) * gridSize) * scale, (row * gridSize) * scale,
			((col + 0.5f) * gridSize) * scale, ((1f + row) * gridSize) * scale,
			new int[]{0x0fff, 0xffff, 0xffff, 0x0fff}, null,
			Shader.TileMode.CLAMP));
//	    shader.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
		c.drawBitmap(b, matrix, shader);
	}
}

abstract class NumberBeltSurface extends Surface {
	private int height, width;
	private int interval1;
	private  boolean negative;
	private int size;
	private Typeface face;
		
	public NumberBeltSurface(float x, float y, int size, int interval1, boolean negative, int width, int height, Typeface face) {
		super(null, x, y);
		this.interval1 = interval1;
		this.size = size;
		this.negative = negative;
		this.width = width;
		this.height = height;
		this.face = face;
		
		MyLog.d(this, "NumberBeltInitialized");
	}

	public void onDraw(Canvas c, float value) {
		int j0 = (int) Math.ceil(value / interval1 - size / (2.0 * interval1));
		if (!negative) {
			j0 = Math.max(0, j0);
		}
		int j1 = (int) Math.floor(value / interval1 + size / (2.0 * interval1));
		
		float realscale = parent.getScale() * parent.getGridSize();
		final float col = parent.getCol();
		final float row = parent.getRow();
		
		// The background
		Paint grey = new Paint();
		grey.setColor(0xee666666);
		grey.setStyle(Paint.Style.FILL);
		float x0 = (col + relx) * realscale;
		float y0 = (row + rely) * realscale;
		c.clipRect(x0, y0, x0 + (width / 512f) * realscale, y0 + (height / 512f) * realscale, Region.Op.REPLACE);
		c.drawPaint(grey);
		
		// the scale
		Paint font = new Paint();
		font.setColor(Color.WHITE); 
		font.setTextSize(20);
		font.setTypeface(this.face);
		for (int j=j0; j<=j1; j++) {
			float oy = (height / 512f) * (1.0f - (1.0f * j * interval1 - value) / size - 0.5f) * realscale;
			c.drawText((Integer.valueOf(j*interval1).toString()), x0, y0 + oy, font);
		}
		
		// the actual value and its background
		font.setColor(Color.WHITE);
		font.setTextSize(40);
		c.clipRect(x0, y0 + 0.4f * (height / 512f) * realscale, x0 + (width / 512f) * realscale, y0 + 0.6f * (height / 512f) * realscale);
		Paint black = new Paint();
		black.setColor(Color.BLACK);
		black.setStyle(Paint.Style.FILL);
		c.drawPaint(black);
		c.drawText(Float.valueOf(value).toString(), x0, y0 + (height / (2 * 512f)) * realscale, font);
				
		//c.clipRect(0, 0, c.getWidth(), c.getHeight(), Region.Op.REPLACE);
	}
}

class AltitudeBeltSurface extends NumberBeltSurface {
	public AltitudeBeltSurface(int width, int height, Typeface face) {
		super(512 - width, 0, 1000, 200, true, width, height, face);
	}

	@Override
	public void onDraw(Canvas c) {
		if (planeData == null) {
			return;
		}
		super.onDraw(c, (int) Math.floor(planeData.getFloat(PlaneData.ALTITUDE)));
	}
}
class SpeedBeltSurface extends NumberBeltSurface {
	public SpeedBeltSurface(int width, int height, Typeface face) {
		super(0, 0, 40, 10, false, width, height, face);
	}

	@Override
	public void onDraw(Canvas c) {
		if (planeData == null) {
			return;
		}
		super.onDraw(c, (int) Math.floor(planeData.getFloat(PlaneData.SPEED)));
	}
}