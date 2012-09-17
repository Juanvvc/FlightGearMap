package com.juanvvc.flightgear.panels;

import org.osmdroid.views.MapView;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.juanvvc.flightgear.PlaneOverlay;
import com.juanvvc.flightgear.R;

/** Main activity of the application.
 * @author juanvi
 */
public class LiquidPanel extends MapControls {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        planeOverlay = new PlaneOverlay(this);
        
    	super.onCreate(savedInstanceState);
        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
       	this.setContentView(R.layout.map_liquid);
       	this.panelView = (PanelView)this.findViewById(R.id.panel);
       	this.mapView = (MapView)this.findViewById(R.id.mapview);
       	
       	setDistribution();

      	// notice that the distribution of the panel does not change: use the one specified in the XML
       	mapView.invalidate();
       	panelView.invalidate();
       	if (this.calibratableManager != null) {
       		this.calibratableManager.empty();
       		panelView.postCalibratableSurfaceManager(this.calibratableManager);
       	}
    }
    
	/** Sets a distribution of instruments on screen.
	 * 
	 * @return True if the distribution is set
	 */
	private boolean setDistribution() {
    	panelView = (PanelView) findViewById(R.id.panel);
    	
    	panelView.setVisibility(View.VISIBLE);
    	panelView.setDistribution(PanelView.Distribution.LIQUID_PANEL);
    	panelView.invalidate();

    	if (this.calibratableManager != null) {
    		this.calibratableManager.empty();
    		panelView.postCalibratableSurfaceManager(this.calibratableManager);
    	}
    	return true;
	}
}