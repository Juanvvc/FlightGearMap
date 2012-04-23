package com.juanvvc.flightgear;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

public abstract class Instrument {
	float x;
	float y;
	float scale;
	Context context;
	ArrayList<String> imgFiles;
	ArrayList<Bitmap> imgs;
	ArrayList<Bitmap> imgsScaled;
	
	public static final int SEMICLOCKSIZE = 256;
	public static final int ARROW_CENTERX = 20;
	public static final int ARROW_CENTERY = 200;
	static final int ARROW1 = 0;
	static final int ARROW2 = 1;
	
	boolean ready = false;
	
	public Instrument(float x, float y, Context c) {
		this.x = x;
		this.y = y;
		context = c;
		scale = 1;
		imgFiles = new ArrayList<String>();
		imgs = new ArrayList<Bitmap>();
		imgsScaled = new ArrayList<Bitmap>();
		imgFiles.add("arrow1.png");
		imgFiles.add("arrow2.png");
		ready = false;
	}
	
	public void loadImages() throws Exception {
		AssetManager mng = context.getAssets();
		for(String f: this.imgFiles) {
			imgs.add(BitmapFactory.decodeStream(mng.open(f)));
		}
		setScale(scale);
		
		ready = true;
	}
	
	public void setScale(float s) {
		scale = s;
		Matrix matrix = new Matrix();
		matrix.setScale(scale,  scale);
		for(Bitmap b: this.imgsScaled) {
			b.recycle();
		}
		imgsScaled.clear();
		for(Bitmap b: this.imgs) {
			imgsScaled.add(Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true));
		}
	}
	
	public abstract void onDraw(Canvas c, PlaneData pd);
}

class Altimeter extends Instrument {
	public Altimeter(float x, float y, Context c) {
		super(x, y, c);
		this.imgFiles.add("alt1.png");
	}

	@Override
	public void onDraw(Canvas c, PlaneData pd) {
		if (! ready) {
			return;
		}
		Matrix matrix = new Matrix();
		matrix.setTranslate(x * scale, y * scale);
		c.drawBitmap(imgsScaled.get(2), matrix, null);

		double alt2Angle = ((pd.getAltitude() / 1000) * 360 / 10);
		matrix.reset();
		matrix.postTranslate((x + SEMICLOCKSIZE - ARROW_CENTERX) * scale, (y + SEMICLOCKSIZE - ARROW_CENTERY) * scale);
		matrix.postRotate((float) alt2Angle, (x + SEMICLOCKSIZE ) * scale, (y + SEMICLOCKSIZE) * scale);
		c.drawBitmap(imgsScaled.get(ARROW2), matrix, null);
		double alt1Angle = ((pd.getAltitude() % 1000) * 360 / 1000);
		matrix.reset();
		matrix.postTranslate((x + SEMICLOCKSIZE - ARROW_CENTERX) * scale, (y + SEMICLOCKSIZE - ARROW_CENTERY) * scale);
		matrix.postRotate((float) alt1Angle, (x + SEMICLOCKSIZE ) * scale, (y + SEMICLOCKSIZE) * scale);
		c.drawBitmap(imgsScaled.get(ARROW1), matrix, null);			
	}
}

class Attitude extends Instrument {
	public Attitude(float x, float y, Context c) {
		super(x, y, c);
		this.imgFiles.add("ati0.png");
		this.imgFiles.add("ati1.png");
		this.imgFiles.add("ati2.png");
		this.imgFiles.add("ati3.png");
	}
	@Override
	public void onDraw(Canvas c, PlaneData pd) {
		if (! ready) {
			return;
		}
		Matrix matrix = new Matrix();
		matrix.setTranslate(x * scale, y * scale);
		c.drawBitmap(imgsScaled.get(2), matrix, null);
		
		Bitmap ati1 = imgs.get(3);
		Bitmap ati1Scaled = imgsScaled.get(3);
		Bitmap ati2 = imgs.get(4);
		Bitmap ati2Scaled = imgsScaled.get(4);
		
		
		// draw pitch
		matrix.reset();
		// translate 23 pixels each 5 degrees
		matrix.postTranslate((x + SEMICLOCKSIZE - ati1.getWidth() / 2) * scale, ( y + SEMICLOCKSIZE - ati1.getHeight() / 2 + pd.getPitch() / 5 * 23) * scale);
		matrix.postRotate(-pd.getRoll(), (x + SEMICLOCKSIZE) * scale, (y + SEMICLOCKSIZE) * scale);
		c.drawBitmap(ati1Scaled, matrix, null);
		// draw roll
		matrix.reset();
		matrix.postTranslate((x + SEMICLOCKSIZE - ati2.getWidth() / 2) * scale, (y + SEMICLOCKSIZE - ati2.getHeight() / 2) * scale);
		matrix.postRotate(-pd.getRoll(), (x + SEMICLOCKSIZE) * scale, (y + SEMICLOCKSIZE) * scale);
		c.drawBitmap(ati2Scaled, matrix, null);
		
		matrix.reset();
		matrix.setTranslate(x * scale, y * scale);
		c.drawBitmap(imgsScaled.get(5), matrix, null);
	}
}

class Speed extends Instrument {
	public Speed(float x, float y, Context c) {
		super(x, y, c);
		this.imgFiles.add("speed.png");
	}
	@Override
	public void onDraw(Canvas c, PlaneData pd) {
		if (! ready) {
			return;
		}
		Matrix matrix = new Matrix();
		matrix.setTranslate(x * scale, y * scale);
		c.drawBitmap(imgsScaled.get(2), matrix, null);
		
		// climb speed
		// 40kts = 30º, 200kts = 320º
		double speedAngle = (pd.getSpeed() - 40) * 290 / 160  + 30;
		if (speedAngle < 0) {
			speedAngle = 0;
		} else if (speedAngle > 330) {
			speedAngle = 330;
		}
		matrix.reset();
		matrix.postTranslate((x + SEMICLOCKSIZE - ARROW_CENTERX) * scale, (y + SEMICLOCKSIZE - ARROW_CENTERY) * scale);
		matrix.postRotate((float) speedAngle, (x + SEMICLOCKSIZE) * scale, (y + SEMICLOCKSIZE) * scale);
		c.drawBitmap(imgsScaled.get(ARROW1), matrix, null);
	}
}

class RPM extends Instrument {
	public RPM(float x, float y, Context c) {
		super(x, y, c);
		this.imgFiles.add("rpm.png");
	}
	@Override
	public void onDraw(Canvas c, PlaneData pd) {
		if (! ready) {
			return;
		}
		Matrix matrix = new Matrix();
		matrix.setTranslate(x * scale, y * scale);
		c.drawBitmap(imgsScaled.get(2), matrix, null);
		
		// rpm
		double rpmAngle = ((pd.getRPM() / 100) * ARROW_CENTERY / 25) - (35 * 90) / 25;
		matrix.reset();
		matrix.postTranslate((x + SEMICLOCKSIZE - ARROW_CENTERX) * scale, (y + SEMICLOCKSIZE - ARROW_CENTERY) * scale);
		matrix.postRotate((float) rpmAngle, (x + SEMICLOCKSIZE) * scale, (y + SEMICLOCKSIZE) * scale);
		c.drawBitmap(imgsScaled.get(ARROW1), matrix, null);
	}
}

class ClimbRate extends Instrument {
	public ClimbRate(float x, float y, Context c) {
		super(x, y, c);
		this.imgFiles.add("climb.png");
	}
	@Override
	public void onDraw(Canvas c, PlaneData pd) {
		if (! ready) {
			return;
		}
		Matrix matrix = new Matrix();
		matrix.setTranslate(x * scale, y * scale);
		c.drawBitmap(imgsScaled.get(2), matrix, null);
		
		// climb speed
		double climbAngle = (pd.getRate() * 180 / 2000) - 90;
		matrix.reset();
		matrix.postTranslate((x + SEMICLOCKSIZE - ARROW_CENTERX) * scale, (y + SEMICLOCKSIZE - ARROW_CENTERY) * scale);
		matrix.postRotate((float) climbAngle, (x + SEMICLOCKSIZE) * scale, (y + SEMICLOCKSIZE) * scale);
		c.drawBitmap(imgsScaled.get(ARROW1), matrix, null);
	}
}

class TurnSlip extends Instrument {
	public TurnSlip(float x, float y, Context c) {
		super(x, y, c);
		this.imgFiles.add("trn0.png");
		this.imgFiles.add("trn1.png");
		this.imgFiles.add("slip.png");
		this.imgFiles.add("turn.png");
	}
	@Override
	public void onDraw(Canvas c, PlaneData pd) {
		if (! ready) {
			return;
		}
		Matrix matrix = new Matrix();
		matrix.setTranslate(x * scale, y * scale);
		c.drawBitmap(imgsScaled.get(2), matrix, null);
		
		Bitmap slip = imgs.get(4);
		Bitmap slipScaled = imgsScaled.get(4);
		
		// draw slip skid
		matrix.reset();
		// (0.7*SEMICLOSEWIDTH is calibrated with on screen instruments with FG sim)
		matrix.setTranslate((x + (1 - 0.7f * pd.getSlipSkid()) * SEMICLOCKSIZE - slip.getWidth() / 2 ) * scale, (y + 1.3f * SEMICLOCKSIZE - slip.getHeight() / 2) * scale);
		c.drawBitmap(slipScaled, matrix, null);
		
		matrix.reset();
		matrix.setTranslate(x * scale, y * scale);
		c.drawBitmap(imgsScaled.get(3), matrix, null);
		
		Bitmap turn = imgs.get(5);
		Bitmap turnScaled = imgsScaled.get(5);
		
		// turn rate
		// 20º means a turn rate = 1 turn each 2 minuts
		double turnAngle = (pd.getTurnRate() * 20);
		matrix.reset();
		matrix.postTranslate((x + SEMICLOCKSIZE - turn.getWidth() / 2) * scale, (y + SEMICLOCKSIZE - turn.getHeight() / 2) * scale);
		matrix.postRotate((float) turnAngle, (x + SEMICLOCKSIZE) * scale, (y + SEMICLOCKSIZE) * scale);
		c.drawBitmap(turnScaled, matrix, null);
	}
}