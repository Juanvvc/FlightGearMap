package com.juanvvc.flightgear;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

/** Main activity of the application.
 * @author juanvi
 */
public class FlightGearMap extends MapActivity {
	/** Reference to the map view. */
	private MapView mapView;
	/** Reference to the panel view. */
	private PanelView panelView;
	/** Reference to the available overlays. */
	private List<Overlay> mapOverlays;
	/** For some reason, if you want to create a marker you need to add it to a ItemizedOverlay. */
	private PlaneMovementOverlay itemizedOverlay;
	/** The port for UDP communications. */
	private static final int PORT = 5501;
	/** A string used for debugging. */
	private static final String TAG = "FlightGear";
	/** Reference to the UDP Thread. */
	private UDPReceiver udpReceiver = null;
	/** The wakelock to lock the screen and prevent sleeping. */
//	private PowerManager.WakeLock wakeLock = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.map_simplepanel);
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.getController().setZoom(15);
        
        mapOverlays = mapView.getOverlays();
        itemizedOverlay = new PlaneMovementOverlay(this.getResources().getDrawable(R.drawable.plane3), this);
        mapOverlays.add(itemizedOverlay);
        
        panelView = (PanelView) findViewById(R.id.panel);
    }
    
    @Override
    protected void onResume() {
    	super.onStart();
        udpReceiver = (UDPReceiver) new UDPReceiver().execute(PORT);
        
        // Tries to read and show the IP address. Not always working!
        try{
	        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
	        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
	        int ipAddress = wifiInfo.getIpAddress();
	        
	        if (ipAddress == 0) {
	        	new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher)
					.setTitle(R.string.network_not_detected)
					.setPositiveButton(android.R.string.ok, null).show();
	        } else {
		        String readableIP = String.format("%d.%d.%d.%d",
		        		(ipAddress & 0xff),
		        		(ipAddress >> 8 & 0xff),
		        		(ipAddress >> 16 & 0xff),
		        		(ipAddress >> 24 & 0xff));
		        
		        String txt = "--generic=socket,out,5," + readableIP + "," + PORT + ",udp,andatlas";
				new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher)
					.setTitle(txt)
					.setPositiveButton(android.R.string.ok, null).show();
	        }
        } catch (Exception e) {
        	Toast.makeText(this, "Cannot get IP Address: " + e.toString(), Toast.LENGTH_LONG);
        }

//        if (wakeLock != null && wakeLock.isHeld()) {
//        	wakeLock.release();
//        }
//        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
//        wakeLock.acquire();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	if (udpReceiver != null) {
    		udpReceiver.cancel(true);
    		udpReceiver = null;
    	}
//    	if (wakeLock != null && wakeLock.isHeld()) {
//    		wakeLock.release();
//    	}
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	// one last paranoic test.
    	if (udpReceiver != null) {
    		udpReceiver.cancel(true);
    		udpReceiver = null;
    	}
//    	if (wakeLock != null && wakeLock.isHeld()) {
//    		wakeLock.release();
//    	}
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.available_distributions, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.map_simplepanel:
	        	panelView.setVisibility(View.VISIBLE);
	        	panelView.setDistribution(PanelView.Distribution.SIMPLE_VERTICAL_PANEL);
	        	mapView.setVisibility(View.VISIBLE);
	            return true;
	        case R.id.only_map:
	        	panelView.setVisibility(View.GONE);
	        	mapView.setVisibility(View.VISIBLE);
	            return true;
	        case R.id.only_simplepanel:
	        	panelView.setVisibility(View.GONE);
	        	panelView.setDistribution(PanelView.Distribution.SIMPLE_HORIZONTAL_PANEL);
	        	mapView.setVisibility(View.VISIBLE);
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	/** An AsyncTask to receive data from a remote UDP server. */
	private class UDPReceiver extends AsyncTask<Integer, PlaneData, String> {

		@Override
		protected String doInBackground(Integer... params) {
			DatagramSocket socket;
			try {
				socket = new DatagramSocket(params[0]);
			} catch (SocketException e) {
				myLog.e(TAG, e.toString());
				return e.toString();
			}
			
			byte[] buf = new byte[255];
			
			while(true) {
				DatagramPacket p = new DatagramPacket(buf, buf.length);
				try {
					socket.receive(p);
					
					PlaneData pd = new PlaneData();
					pd.parse(new String(p.getData()));
					// new data is managed as a "progressUpdate" event of the AsyncTask
					this.publishProgress(pd);
					
				} catch (Exception e) {
					myLog.w(TAG, e.toString());
				}
			}
		}

		@Override
		protected void onProgressUpdate(PlaneData... values) {
			// A new data arrived to the UDP listener
			if (itemizedOverlay != null) {
				// remove all markers (i.e, the last position of the plane)
				itemizedOverlay.clear();
				// and create a new marker in the new position
				GeoPoint p = new GeoPoint((int)(values[0].getLatitude() * 1E6), (int)(values[0].getLongitude() * 1E6));
				mapView.getController().setCenter(p);
				OverlayItem i = new OverlayItem(p, "", "");
				itemizedOverlay.addOverlay(i, values[0].getHeading());
				
				// update the panel
				panelView.setPlaneData(values[0]);
				
				// redraw the views
				mapView.invalidate();
				findViewById(R.id.panel).invalidate();
				
			}
		}
	}
}