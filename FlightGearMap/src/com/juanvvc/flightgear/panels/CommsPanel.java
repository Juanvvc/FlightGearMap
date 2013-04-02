package com.juanvvc.flightgear.panels;

import java.io.IOException;

import com.juanvvc.flightgear.FGFSConnection;
import com.juanvvc.flightgear.MyBitmap;
import com.juanvvc.flightgear.MyLog;
import com.juanvvc.flightgear.R;
import com.juanvvc.flightgear.instruments.FreqPicker;
import com.juanvvc.flightgear.instruments.Surface;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class CommsPanel extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		this.setContentView(R.layout.commspanel);
		FreqPicker fp;
		fp = ((FreqPicker) this.findViewById(R.id.comm1)); fp.setLabel("COM1:"); fp.setSelectedFreq(123.45, 123.45);
		fp = ((FreqPicker) this.findViewById(R.id.comm2)); fp.setLabel("COM2:"); fp.setSelectedFreq(123.45, 123.45);
		fp = ((FreqPicker) this.findViewById(R.id.nav1)); fp.setLabel("NAV1:"); fp.setSelectedFreq(123.45, 123.45);
		fp = ((FreqPicker) this.findViewById(R.id.nav2)); fp.setLabel("NAV2:"); fp.setSelectedFreq(123.45, 123.45);
		fp = ((FreqPicker) this.findViewById(R.id.adf)); fp.setLabel("ADF:"); fp.setSelectedFreq(400, 400);
		fp = ((FreqPicker) this.findViewById(R.id.dme)); fp.setLabel("DME:"); fp.setSelectedFreq(400, 400); fp.showSelected(false);
	}
	
	private class CommPseudoSurface extends Surface {
		private boolean firstRead = false;
		
		public CommPseudoSurface() {
			super(null, -1, 1);
			this.firstRead = true;
		}

		@Override
		public void update(FGFSConnection conn) throws IOException {
			if (conn == null || conn.isClosed()) {
				return;
			}
		}

		@Override
		public void onDraw(Canvas c) {
			// does nothing
		}
	}
}
