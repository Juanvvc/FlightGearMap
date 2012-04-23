package com.juanvvc.flightgear;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;

import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class FlightGearMap extends MapActivity {
	private LinearLayout linearLayout;
	private MapView mapView;
	List<Overlay> mapOverlays;
	Drawable planeIco;
	PlaneMovementOverlay itemizedOverlay;
	private static final int PORT = 5501;
	private static final String TAG = "FlightGear";
	UDPReceiver udpReceiver = null;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);
        
        try{
	        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
	        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
	        int ipAddress = wifiInfo.getIpAddress();
	        String readableIP = String.format("%d.%d.%d.%d",
	        		(ipAddress & 0xff),
	        		(ipAddress >> 8 & 0xff),
	        		(ipAddress >> 16 & 0xff),
	        		(ipAddress >> 24 & 0xff));
	        
	        String txt = "--generic=socket,out,5," + readableIP + "," + PORT + ",udp,andatlas";
			new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher)
				.setTitle(txt)
				.setPositiveButton(android.R.string.ok, null).show();
        } catch (Exception e) {
        	new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher)
				.setTitle("Cannot determine IP Address: " + e.toString())
				.setPositiveButton(android.R.string.ok, null).show();
        }
        
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.getController().setZoom(15);
        
        mapOverlays = mapView.getOverlays();
        planeIco = this.getResources().getDrawable(R.drawable.plane);
        itemizedOverlay = new PlaneMovementOverlay(planeIco, this);
        mapOverlays.add(itemizedOverlay);
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
        udpReceiver = (UDPReceiver) new UDPReceiver().execute(PORT);
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	if (udpReceiver != null) {
    		udpReceiver.cancel(true);
    		udpReceiver = null;
    	}
    	finish();
    }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	/** An AsyncTask to receive data from a remote UDP server. */
	private class UDPReceiver extends AsyncTask<Integer, PlaneData, String> {

		@Override
		protected String doInBackground(Integer... params) {
			DatagramSocket socket;
			try {
				socket = new DatagramSocket(params[0]);
			} catch (SocketException e) {
				return e.toString();
			}
			
			byte[] buf = new byte[255];
			
			while(true) {
				DatagramPacket p = new DatagramPacket(buf, buf.length);
				try {
					socket.receive(p);
					
					PlaneData pd = new PlaneData();
					pd.parse(new String(p.getData()));
					this.publishProgress(pd);
					
				} catch (Exception e) {
					myLog.w(TAG, e.toString());
				}
			}
		}

		@Override
		protected void onProgressUpdate(PlaneData... values) {
			if (itemizedOverlay != null) {
				itemizedOverlay.clear();
				GeoPoint p = new GeoPoint((int)(values[0].getLatitude() * 1E6), (int)(values[0].getLongitude() * 1E6));
				mapView.getController().setCenter(p);
				OverlayItem i = new OverlayItem(p, "", "");
				itemizedOverlay.addOverlay(i, values[0].getHeading());
				
				((SmallPanelView) findViewById(R.id.panel)).setPlaneData(values[0]);
				
				mapView.invalidate();
				findViewById(R.id.panel).invalidate();
				
			}
		}
	}
}