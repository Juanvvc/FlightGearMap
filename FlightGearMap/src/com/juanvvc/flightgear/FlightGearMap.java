package com.juanvvc.flightgear;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.juanvvc.flightgear.instruments.CalibratableSurfaceManager;

// TODO: make tile source configurable
// TODO: make port configurable
// TODO: remember zoom level

/** Main activity of the application.
 * @author juanvi
 */
public class FlightGearMap extends Activity {
	/** Reference to the map view. */
	private MapView mapView;
	/** Reference to the panel view. */
	private PanelView panelView;
	/** Reference to the available overlays. */
	private PlaneOverlay planeOverlay;
	/** The port for UDP communications. */
	private int udpPort = 5501;
	/** Reference to the UDP Thread. */
	private UDPReceiver udpReceiver = null;
	/** Reference to the Telnet Thread. */
	private CalibratableSurfaceManager calibratableManager = null;
	/** The wakelock to lock the screen and prevent sleeping. */
	private PowerManager.WakeLock wakeLock = null;
	/** If set, use the wakeLock.
	 * TODO: the wakeLock was not always working. Use this option for debugging
	 */
	private static final boolean USE_WAKELOCK = true;
	/** Timeout milliseconds for the UDP socket. */
	public static final int SOCKET_TIMEOUT = 10000;
	/** A reference to the currently displayed dialog. */
	private AlertDialog currentDialog;
	/** Identifier of the default panel distribution. */
	private int defaultDistribution;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.map_simplepanel);
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.getController().setZoom(15);
        
        planeOverlay = new PlaneOverlay(this);
        mapView.getOverlays().add(planeOverlay);
        
        panelView = (PanelView) findViewById(R.id.panel);
//mapView.setVisibility(View.GONE);
//panelView.setDistribution(PanelView.Distribution.C172_INSTRUMENTS);
        // get the default distribution
        this.defaultDistribution = panelView.getDistribution();        
    }
    
    /** Load preferences. */
    public void loadPreferences() {
    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    	String mapType = sp.getString("map_type", null);
    	String planeType = sp.getString("plane_type", null);
  		String port = sp.getString("udp_port", "5501");
  		
  		// select the plane
  		try {
  			if (planeType.equals("plane2")) {
  				this.planeOverlay.loadPlane(this, R.drawable.plane2);
  			} else if (planeType.equals("plane3")) {
  				this.planeOverlay.loadPlane(this, R.drawable.plane3);
  			} else if (planeType.equals("plane4")) {
  				this.planeOverlay.loadPlane(this, R.drawable.plane4);
  			} else if (planeType.equals("plane5")) {
  				this.planeOverlay.loadPlane(this, R.drawable.plane5);
  			} else {
  				this.planeOverlay.loadPlane(this, R.drawable.plane1);
  			}
  		} catch (Exception e) {
  			this.planeOverlay.loadPlane(this, R.drawable.plane1);
  		}
  		
  		// select the map type
  		try {
  			if (mapType.equals("cycle")) {
  				mapView.setTileSource(TileSourceFactory.CYCLEMAP);
//  			} else if (mapType.equals("hills")) {
//  				mapView.setTileSource(TileSourceFactory.HILLS);
//  			} else if (mapType.equals("topo")) {
//  				mapView.setTileSource(TileSourceFactory.TOPO);
  			} else if (mapType.equals("public_transport")) {
  				mapView.setTileSource(TileSourceFactory.PUBLIC_TRANSPORT);
  			} else if (mapType.equals("mapquest")) {
  				mapView.setTileSource(TileSourceFactory.MAPQUESTAERIAL);
  			} else {
  				mapView.setTileSource(TileSourceFactory.MAPNIK);
  			}
  		} catch (Exception e) {
  			mapView.setTileSource(TileSourceFactory.MAPNIK);
  		}
  		
  		// select the UDP port
  		try {
  			udpPort = new Integer(port).intValue();
  		} catch (Exception e) {
  			udpPort = 5501;
  		}
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	this.loadPreferences();
    	
    	MyLog.i(this, "Starting threads");
    	if (udpReceiver == null) {
    		udpReceiver = (UDPReceiver) new UDPReceiver().execute(udpPort);
    	}
    	Toast.makeText(this, getString(R.string.waiting_connection), Toast.LENGTH_LONG).show();
    	

    	if (USE_WAKELOCK) {
	        if (wakeLock != null && wakeLock.isHeld()) {
	        	wakeLock.release();
	        }
	        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
	        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "FlightGearMap");
	        wakeLock.acquire();
    	}
    }
    
    @Override
    protected void onPause() {
    	MyLog.i(this, "Pausing threads");
    	if (udpReceiver != null) {
    		udpReceiver.cancel(true); // TODO: actually, the thread only stops after a timeout
    		udpReceiver = null;
    	}
        if (calibratableManager != null) {
        	calibratableManager.interrupt();
        	calibratableManager = null;
        }
    	MyLog.i(this, "Stopping dialogs");
    	if (currentDialog != null) {
    		currentDialog.dismiss();
    		currentDialog = null;
    	}    	
    	if (USE_WAKELOCK && wakeLock != null && wakeLock.isHeld()) {
    		wakeLock.release();
    	}
    	super.onPause();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	// one last paranoid test.
    	if (udpReceiver != null) {
    		udpReceiver.cancel(true);
    		udpReceiver = null;
    	}
        if (calibratableManager != null) {
        	calibratableManager.interrupt();
        }
    	if (USE_WAKELOCK && wakeLock != null && wakeLock.isHeld()) {
    		wakeLock.release();
    	}
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.available_distributions, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// panel is not redrawn after a change in distribution, do not know the reason
	    switch (item.getItemId()) {
	        case R.id.map_simplepanel:
	        	panelView.setVisibility(View.VISIBLE);
	        	mapView.setVisibility(View.VISIBLE);
	        	panelView.setDistribution(defaultDistribution);
	        	mapView.invalidate();
	        	panelView.invalidate();
	        	if (this.calibratableManager != null) {
	        		this.calibratableManager.empty();
	        		panelView.postCalibratableSurfaceManager(this.calibratableManager);
	        	}
	            return true;
	        case R.id.only_map:
	        	panelView.setVisibility(View.GONE);
	        	panelView.setDistribution(PanelView.Distribution.ONLY_MAP);
	        	if (this.calibratableManager != null) {
	        		this.calibratableManager.empty();
	        	}
	        	mapView.setVisibility(View.VISIBLE);
	        	mapView.invalidate();
	        	panelView.invalidate();
	        	return true;
	        case R.id.only_simplepanel:
	        	panelView.setVisibility(View.VISIBLE);
	        	panelView.setDistribution(PanelView.Distribution.SIMPLE_HORIZONTAL_PANEL);
	        	mapView.setVisibility(View.GONE);
	        	mapView.invalidate();
	        	panelView.invalidate();

	        	if (this.calibratableManager != null) {
	        		this.calibratableManager.empty();
	        	}
	        	return true;
	        case R.id.c172_panel:
	        	panelView.setVisibility(View.VISIBLE);
	        	panelView.setDistribution(PanelView.Distribution.C172_INSTRUMENTS);
	        	mapView.setVisibility(View.GONE);
	        	mapView.invalidate();
	        	panelView.invalidate();

	        	if (this.calibratableManager != null) {
	        		this.calibratableManager.empty();
	        		panelView.postCalibratableSurfaceManager(this.calibratableManager);
	        	}
	        	return true;
	        case R.id.settings:
	        	startActivity(new Intent(this, Preferences.class));
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	/** An AsyncTask to receive data from a remote UDP server.
	 * When new data is received, the panelView is updated.
	 * THIS IS THE THREAD THAT UPDATES THE PANELVIEW */
	private class UDPReceiver extends AsyncTask<Integer, PlaneData, String> {
		private boolean firstMessage = true;

		@Override
		protected String doInBackground(Integer... params) {
			DatagramSocket socket;
	
			try {
				socket = new DatagramSocket(params[0]);
				socket.setSoTimeout(SOCKET_TIMEOUT);
			} catch (SocketException e) {
				MyLog.e(this, e.toString());
				return e.toString() + " " + getString(R.string.wait);
			}
			
			byte[] buf = new byte[512];
			boolean canceled = false;
			String msg = null;
			firstMessage = true;
			
			while(!canceled) {
				DatagramPacket p = new DatagramPacket(buf, buf.length);
				
				try {
					socket.receive(p);
					
					PlaneData pd = new PlaneData(null);
					pd.parse(new String(p.getData()));
					// new data is managed as a "progressUpdate" event of the AsyncTask
					panelView.postPlaneData(pd);
					this.publishProgress(pd);
					
					if (panelView.getVisibility() == View.VISIBLE) {
						panelView.redraw();
					}
					
					canceled = this.isCancelled();
				} catch(SocketTimeoutException e) {
					MyLog.e(this, e.toString());
					canceled = true;
					msg = getString(R.string.conn_timeout);
				} catch (Exception e) {
					MyLog.e(this, MyLog.stackToString(e));
					canceled = true;
					msg = e.toString() + "\n" + getResources().getString(R.string.update_andatlas);
				}
			}
			
			socket.close();
			
			return msg;
		}

		@Override
		protected void onProgressUpdate(PlaneData... values) {
			if (firstMessage) {
				Toast.makeText(FlightGearMap.this, getString(R.string.conn_established), Toast.LENGTH_LONG).show();
				firstMessage = false;
			}
			
			// A new data arrived to the UDP listener
			if (planeOverlay != null) {
				// move the overlay to the new position
				GeoPoint p = new GeoPoint(
						(int)(values[0].getFloat(PlaneData.LATITUDE) * 1E6),
						(int)(values[0].getFloat(PlaneData.LONGITUDE) * 1E6));
				mapView.getController().setCenter(p);
				
				// update the panel and overlay
				planeOverlay.setPlaneData(values[0], p);
				mapView.invalidate();
				
				// check if the calibratable manager is still running
				if (calibratableManager == null || !calibratableManager.isAlive()) {
			        calibratableManager = new CalibratableSurfaceManager(PreferenceManager.getDefaultSharedPreferences(FlightGearMap.this));
			        calibratableManager.start();
			        panelView.postCalibratableSurfaceManager(calibratableManager);
				}
			}
		}
		
		@Override
		protected void onPostExecute(String msg) {
			if (isCancelled()) {
				// I think that if isCancelled(), this method is not called. Still, just in case
				return;
			}
			
	    	if (currentDialog != null) {
	    		currentDialog.dismiss();
	    	}
	    	
	    	if(FlightGearMap.this.isFinishing()) {
	    		return;
	    	}
	        
	        try{
		        // Tries to read the IP address. Not always working!
		        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		        int ipAddress = wifiInfo.getIpAddress();
		        
		        String txt = "";
		        if (msg != null) {
		        	txt = txt + msg + "\n\n";
		        }
		        
		        // Dismiss the current dialog, if any
		        if (currentDialog != null) {
		        	currentDialog.dismiss();
		        }
		        
		        if (ipAddress == 0) {
		        	// if no IP in the wifi network.
		        	currentDialog = new AlertDialog.Builder(FlightGearMap.this).setIcon(R.drawable.ic_launcher)
		        		.setTitle(getString(R.string.warning))
						.setMessage(getString(R.string.network_not_detected) + " " + getString(R.string.critical_error))
						.setPositiveButton(android.R.string.ok, new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								finish();
							}
						})
						.show();
		        } else {
		        	// convert Ip to a readable IP
			        String readableIP = String.format("%d.%d.%d.%d",
			        		(ipAddress & 0xff),
			        		(ipAddress >> 8 & 0xff),
			        		(ipAddress >> 16 & 0xff),
			        		(ipAddress >> 24 & 0xff));
			        
			        // add information about fgfs+++
			        txt = txt + getString(R.string.run_fgfs_using) + " --generic=socket,out,10," + readableIP + "," + udpPort + ",udp,andatlas --telnet=9000";
			        
			        // show the dialog on screen
					currentDialog = new AlertDialog.Builder(FlightGearMap.this).setIcon(R.drawable.ic_launcher)
						.setTitle(getString(R.string.warning))
						.setMessage(txt)
						.setPositiveButton(android.R.string.ok, new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// when the user click ok, the receivers restart
						        udpReceiver = (UDPReceiver) new UDPReceiver().execute(udpPort);
						        if (calibratableManager != null) {
						        	calibratableManager.interrupt();
						        }

							}
						})
						.setNegativeButton(R.string.quit, new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								finish();
							}
						})
						.show();
		        }
	        } catch (Exception e) {
	        	Toast.makeText(FlightGearMap.this, e.toString() + " " + getString(R.string.critical_error), Toast.LENGTH_LONG).show();
	        }
		}
	}
}