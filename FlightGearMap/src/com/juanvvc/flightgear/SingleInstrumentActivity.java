package com.juanvvc.flightgear;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.juanvvc.flightgear.instruments.InstrumentType;
import com.juanvvc.flightgear.panels.Cessna172;
import com.juanvvc.flightgear.panels.PanelView;

/** A simplification of InstrumentActivity to show only one instrument, and allow chaging the instrument */
public class SingleInstrumentActivity extends InstrumentActivity implements OnClickListener{
	
	private int currentInstrument = 0;
	private static int MAX_INSTRUMENT = 8;

    public void onCreate(Bundle savedInstanceState) {
    	// Initialize planaOverlay to bypass some initializations in our parent's constructor
    	planeOverlay = new PlaneOverlay(this);
        super.onCreate(savedInstanceState);
        
    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    	if (sp.getBoolean("fullscreen", true)) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	}
        
    	this.setContentView(R.layout.singleinstrument);
    	this.panelView = (PanelView)this.findViewById(R.id.panel);
    	this.mapView = null;
    	
    	Button b = (Button) this.findViewById(R.id.prev_instrument);
    	b.setOnClickListener(this);
    	b = (Button) this.findViewById(R.id.next_instrument);
    	b.setOnClickListener(this);
    	
  		// set instruments centered or not
  		boolean centered = sp.getBoolean("center_instruments", true);
  		if ( this.panelView != null ) {
  			this.panelView.setCenterInstruments(centered);
  		}
    }
    
   
    @Override
	protected void onStart() {
		super.onStart();
		
		// get the current instrument from preferences
		SharedPreferences sp = this.getPreferences(MODE_PRIVATE);
		currentInstrument = sp.getInt("current_instrument", 0);
		currentInstrument = Math.abs(currentInstrument%MAX_INSTRUMENT);
		
		this.setInstrument(currentInstrument);
	}
    
    @Override
	protected void onPause() {
		super.onPause();
		
		// save the current instrument into the preferences
		SharedPreferences sp = this.getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor ed = sp.edit();
		ed.putInt("current_instrument", currentInstrument);
		ed.commit();
	}
    

    // change the current instrument
	public void onClick(View v) {
    	if (v.getId() == R.id.prev_instrument) {
    		if (currentInstrument <= 0) {
    			currentInstrument = MAX_INSTRUMENT;
    		}
    		currentInstrument = currentInstrument - 1;
    	} else if (v.getId() == R.id.next_instrument) {
    		currentInstrument = currentInstrument + 1;
    		if (currentInstrument >= MAX_INSTRUMENT) {
    			currentInstrument = 0;
    		}
    	}
    	
    	setInstrument(currentInstrument);
    }
    
    private void setInstrument(int i) {
    	
    	MyLog.i(this, "Setting instrument: " + i);
    	
    	panelView = (PanelView) findViewById(R.id.panel);
    	
    	panelView.setVisibility(View.VISIBLE);

    	switch(i) {
    	case 0: panelView.setInstrument(Cessna172.createInstrument(InstrumentType.SPEED, this, 0, 0)); break;
    	case 1: panelView.setInstrument(Cessna172.createInstrument(InstrumentType.ALTIMETER, this, 0, 0)); break;
    	case 2: panelView.setInstrument(Cessna172.createInstrument(InstrumentType.HEADING, this, 0, 0)); break;
    	case 3: panelView.setInstrument(Cessna172.createInstrument(InstrumentType.ATTITUDE, this, 0, 0)); break;
    	case 4: panelView.setInstrument(Cessna172.createInstrument(InstrumentType.CLIMB_RATE, this, 0, 0)); break;
    	case 5: panelView.setInstrument(Cessna172.createInstrument(InstrumentType.NAV1, this, 0, 0)); break;
    	case 6: panelView.setInstrument(Cessna172.createInstrument(InstrumentType.ADF, this, 0, 0)); break;
    	case 7: panelView.setInstrument(Cessna172.createInstrument(InstrumentType.HSI1, this, 0, 0)); break;
    	}
    	
    	panelView.invalidate();

    	if (this.calibratableManager != null) {
    		this.calibratableManager.empty();
    		panelView.postCalibratableSurfaceManager(this.calibratableManager);
    	}
    }
}
