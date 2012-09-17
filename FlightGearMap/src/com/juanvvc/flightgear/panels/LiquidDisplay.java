package com.juanvvc.flightgear.panels;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;

import com.juanvvc.flightgear.PlaneData;
import com.juanvvc.flightgear.instruments.CalibratableRotateSurface;
import com.juanvvc.flightgear.instruments.Instrument;
import com.juanvvc.flightgear.instruments.InstrumentType;
import com.juanvvc.flightgear.instruments.RotateSurface;
import com.juanvvc.flightgear.instruments.StaticSurface;
import com.juanvvc.flightgear.instruments.Surface;

public class LiquidDisplay {
	public static Instrument createInstrument(InstrumentType type, Context context, float col, float row) {
		switch(type) {
		case ATTITUDE:
			return new Instrument(col, row, context, new Surface[] {
					new LiquidAtiSurface("pitchscale.png", 70, 138),
					new RotateSurface("ai.roll.ref.png", 0, 0, PlaneData.ROLL, 1, 256, 256, -180, 180, 180, -180),
					new StaticSurface("ai.ref.png", 0, 0)
				});
		case HSI1:
			return new Instrument(col, row, context, new Surface[] {
					new RotateSurface("hsi.png", 0, 0, PlaneData.HEADING, 1, 256, 256, 0, 0, 360, -360),
					new CalibratableRotateSurface("hand4.png", 236, 56, "/instrumentation/nav/radials/selected-deg", 1, true, -1, 256, 256, 0, 0, 360, -360),
					new StaticSurface("hsi2.png", 0, 0)
			});
		default:
			return null;
		}
	}
	
	public static ArrayList<Instrument> getInstrumentPanel(Context context) {
		final ArrayList<Instrument> instruments = new ArrayList<Instrument>();
		instruments.add(createInstrument(InstrumentType.ATTITUDE, context, 0.5f, 0.25f));
		instruments.add(createInstrument(InstrumentType.HSI1, context, 0.5f, 1.5f));
		return instruments;
	}
}

class LiquidAtiSurface extends Surface {
	private Matrix matrix;

	public LiquidAtiSurface(String file, float x, float y) {
		super(file, x, y);
		matrix = new Matrix();
	}
	@Override
	public void onDraw(Canvas c, Bitmap b) {
		if (planeData == null) {
			return;
		}
		
		// draw pitch
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
		c.drawBitmap(b, matrix, null);
		
		float[] p = {0, 0, 0, b.getHeight()};
		matrix.mapPoints(p);
		Paint paintGrad = new Paint();
		paintGrad.setShader(new LinearGradient(
				p[0], p[1], p[2], p[3],
				new int[]{0xff0000aa, 0xff0000ff, 0xff0000ff, 0xffffffff, 0xffffffff, 0xffA36008, 0xffA36008},
				new float[]{0f, 0.25f, 0.49f, 0.495f, 0.505f, 0.51f, 1.0f},
				Shader.TileMode.CLAMP));
		c.drawPaint(paintGrad);
		
		c.drawBitmap(b, matrix, null);
		
	}
}
