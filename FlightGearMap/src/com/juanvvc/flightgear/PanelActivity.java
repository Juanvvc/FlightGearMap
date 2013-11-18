package com.juanvvc.flightgear;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Vector;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.juanvvc.flightgear.instruments.CalibratableSurfaceManager;
import com.juanvvc.flightgear.maps.CompassOverlay;
import com.juanvvc.flightgear.maps.DatabaseHelper;
import com.juanvvc.flightgear.maps.MapOverlay;
import com.juanvvc.flightgear.maps.SolidTileSource;
import com.juanvvc.flightgear.panels.PanelView;

/** Shows a panel with only instruments.
 * @author juanvi
 */
public class PanelActivity extends Activity {
	/** Reference to the map view. */
	MapView mapView = null;
	/** Reference to the panel view. */
	PanelView panelView = null;
	/** Reference to the available overlays. */
	MapOverlay planeOverlay;
	/** Reference to the distances overlay */
	CompassOverlay compassOverlay;
	/** The port for UDP communications. */
	private int udpPort = 5501;
	/** The port for Telnet communications. */
	private int telnetPort = 9000;
	/** Reference to the UDP Thread. */
	private UDPReceiver udpReceiver = null;
	/** Reference to the Telnet Thread. */
	protected CalibratableSurfaceManager calibratableManager = null;
	/** The wakelock to lock the screen and prevent sleeping. */
	private PowerManager.WakeLock wakeLock = null;
	/** The identifier of the distribution. Must be an integer from PanelView.Distribution. */
	private int distribution;
	/** If set, use the wakeLock.
	 * TODO: the wakeLock was not always working. Use this option for debugging
	 */
	private static final boolean USE_WAKELOCK = true;
	/** Timeout milliseconds for the UDP socket. */
	public static final int SOCKET_TIMEOUT = 10000;
	/** A reference to the currently displayed dialog. */
	private AlertDialog currentDialog = null;
	/** A reference to the currently displayed toast */
	private Toast currentToast = null;
	/** If set, show the airports on the map */
	private boolean show_airports = true;
	/** If set, show the navaids on the map */
	private boolean show_navaids = true;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (this.planeOverlay != null) {
        	// we use planeOverlay to check if the views have been started.
        	// Activities that extend this view should initiate planeOverlay
        	// and then call super.onCreate() to bypass this constructor
        	return;
        }
        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    	if (sp.getBoolean("fullscreen", true)) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	}
		
        planeOverlay = new MapOverlay(this);
        compassOverlay = new CompassOverlay(this);
        
        boolean onlymap = false;
        boolean showmap = false;
        boolean liquid = false;
        if (this.getIntent() != null && this.getIntent().getExtras() != null) {
        	onlymap = this.getIntent().getExtras().getBoolean("onlymap");
        	liquid = this.getIntent().getExtras().getBoolean("liquid");
        	showmap = this.getIntent().getExtras().getBoolean("showmap");
        }
        
        if (onlymap) {
        	this.setContentView(R.layout.only_map);
        	this.panelView = null;
        	this.mapView = (MapView)this.findViewById(R.id.mapview);
        } else if (liquid) {
           	this.setContentView(R.layout.map_liquid);
        	this.panelView = (PanelView) findViewById(R.id.panel);
        	this.mapView = (MapView)this.findViewById(R.id.mapview);
        	
        	panelView.setVisibility(View.VISIBLE);
        	panelView.setDistribution(PanelView.Distribution.LIQUID_PANEL);
        	panelView.invalidate();

        	if (this.calibratableManager != null) {
        		this.calibratableManager.empty();
        		panelView.postCalibratableSurfaceManager(this.calibratableManager);
        	} 		
    	} else if (showmap) {
        	this.setContentView(R.layout.map_simplepanel);
        	this.panelView = (PanelView)this.findViewById(R.id.panel);
        	this.mapView = (MapView)this.findViewById(R.id.mapview);
        	
        	// notice that the distribution of the panel does not change: use the one specified in the XML
        	mapView.invalidate();
        	panelView.invalidate();
        	if (this.calibratableManager != null) {
        		this.calibratableManager.empty();
        		panelView.postCalibratableSurfaceManager(this.calibratableManager);
        	}
        } else {
		
    		setContentView(R.layout.instruments);
            // get the distribution from the intent
            try{
            	this.distribution = getIntent().getExtras().getInt("distribution", PanelView.Distribution.C172_INSTRUMENTS);
            	// TODO: check savedInstanceState?
            } catch(Exception e) {
            	this.distribution = PanelView.Distribution.C172_INSTRUMENTS;
            }        
            panelView = (PanelView) this.findViewById(R.id.panel);
            mapView = null;
            this.setDistribution();
    	}
    }
    
    /** Load preferences. */
    public void loadPreferences() {
    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    	String mapType = sp.getString("map_type", null);
    	String planeType = sp.getString("plane_type", null);
  		String port = null;
  		
  		// select the plane
  		try {
  			if (planeType.equals("plane2")) {
  				this.planeOverlay.loadIcon(this, R.drawable.plane2);
  			} else if (planeType.equals("plane3")) {
  				this.planeOverlay.loadIcon(this, R.drawable.plane3);
  			} else if (planeType.equals("plane4")) {
  				this.planeOverlay.loadIcon(this, R.drawable.plane4);
  			} else if (planeType.equals("plane5")) {
  				this.planeOverlay.loadIcon(this, R.drawable.plane5);
  			} else {
  				this.planeOverlay.loadIcon(this, R.drawable.plane1);
  			}
  		} catch (Exception e) {
  			this.planeOverlay.loadIcon(this, R.drawable.plane1);
  		}
  		
  		// select the map type
		if (mapView != null) {
	  		try {
	  			if (mapType.equals("cycle")) {
	  				mapView.setTileSource(TileSourceFactory.CYCLEMAP);
	//  			} else if (mapType.equals("hills")) {
	//  				mapView.setTileSource(TileSourceFactory.HILLS);
	//			} else if (mapType.equals("public_transport")) {
	//				mapView.setTileSource(TileSourceFactory.PUBLIC_TRANSPORT);
	  			} else if (mapType.equals("mapquest")) {
	  				mapView.setTileSource(TileSourceFactory.MAPQUESTAERIAL);
	  			} else if (mapType.equals("solid")) {
	  				mapView.setTileSource(new SolidTileSource(this));
	  			} else {
	  				mapView.setTileSource(TileSourceFactory.MAPNIK);
	  			}
	  		} catch (Exception e) {
	  			mapView.setTileSource(TileSourceFactory.MAPNIK);
	  		}
	  		
	        mapView.setBuiltInZoomControls(true);
	        mapView.getController().setZoom(15);
	       
	        mapView.getOverlays().add(planeOverlay);
		}
  		
  		// select the UDP port
    	port = sp.getString("udp_port", "5501");
  		try {
  			udpPort = Integer.valueOf(port);
  		} catch (Exception e) {
  			udpPort = 5501;
  		}
  		
  		// select the telnet port
    	port = sp.getString("telnet_port", "9000");
  		try {
  			telnetPort = Integer.valueOf(port);
  		} catch (Exception e) {
  			telnetPort = 9000;
  		}
  		
  		// set instruments centered or not
  		boolean centered = sp.getBoolean("center_instruments", true);
  		PanelView pv = (PanelView) this.findViewById(R.id.panel);
  		if ( pv!= null ) {
  			pv.setCenterInstruments(centered);
  		}
  		
  		// show different elements on the map
  		show_airports = sp.getBoolean("show_airports", true);
  		show_navaids = sp.getBoolean("show_navaids", true);
  		boolean show_distances = sp.getBoolean("show_distances", true);
  		if (!show_distances) {
  			this.compassOverlay = null;
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
    	showToast(getString(R.string.waiting_connection), Toast.LENGTH_LONG);

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
//    	if (USE_WAKELOCK && wakeLock != null && wakeLock.isHeld()) {
//    		wakeLock.release();
//    	}
    }
    
	/** Shows a toast on the screen */
	private void showToast(String msg, int duration) {
		if(currentToast != null) {
			currentToast.cancel();
		}
		currentToast = Toast.makeText(this, msg, duration);
		currentToast.show();
	}

	/** Sets a distribution of instruments on screen.
	 * 
	 * @return True if the distribution is set
	 */
	private boolean setDistribution() {
    	panelView = (PanelView) findViewById(R.id.panel);
    	
    	panelView.setVisibility(View.VISIBLE);
    	panelView.setDistribution(this.distribution);
    	panelView.invalidate();

    	if (this.calibratableManager != null) {
    		this.calibratableManager.empty();
    		panelView.postCalibratableSurfaceManager(this.calibratableManager);
    	}
    	return true;
	}
	
	/** An AsyncTask to receive data from a remote UDP server.
	 * When new data is received, the panelView is updated.
	 * Hence, a frame is drawn each time a new messages arrives.
	 * THIS IS THE THREAD THAT UPDATES THE PANELVIEW */
	private class UDPReceiver extends AsyncTask<Integer, PlaneData, String> {
		private boolean firstMessage = true;
		private DatabaseHelper db = null;
		private Vector<MapOverlay> overlays = null;

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
			
			MyLog.d(this, "UDP Thread started. Listening port: " + params[0]);
			
			byte[] buf = new byte[512];
			boolean canceled = false;
			String msg = null;
			firstMessage = true;
			
			PlaneData pd = new PlaneData();
			// TODO: make this configurable
			pd.setMovingAverage(PlaneData.NAV1_DEFLECTION, true);
			pd.setMovingAverage(PlaneData.GS1_DEFLECTION, true);
			pd.setMovingAverage(PlaneData.NAV2_DEFLECTION, true);
			pd.setMovingAverage(PlaneData.CLIMB_RATE, true);
			
			// if there is a mapview, start the database checker
			if (mapView != null) {
				MyLog.d(this, "Starting database");
				db = new DatabaseHelper(PanelActivity.this);
				try {
					db.createDatabase();
					db.openDatabase();
					overlays = new Vector<MapOverlay>();
				} catch (IOException e) {
					MyLog.e(this, "Database cannot be created: " + e.toString());
					db = null;
				}
			}
			int UPDATE_OVERLAYS=100;
			int update_db = UPDATE_OVERLAYS;
			
			while(!canceled) {
				DatagramPacket p = new DatagramPacket(buf, buf.length);
				
				try {
					socket.receive(p);

					pd.parse(new String(p.getData()));
					
					if (db != null) {
						update_db++;
						if (update_db > UPDATE_OVERLAYS) {
							update_db = 0;
							MyLog.d(this, "Checking database");
							
							if (show_airports) {
								// check the airports 
								Cursor cursor = db.getAirports(
										pd.getFloat(PlaneData.LATITUDE),
										pd.getFloat(PlaneData.LONGITUDE));
								if (cursor!=null) {
									cursor.moveToFirst();
									overlays.removeAllElements();
									while (!cursor.isAfterLast()) {
										float lat = cursor.getFloat(2);
										float lng = cursor.getFloat(3);
										MapOverlay a = new MapOverlay(PanelActivity.this);
										a.setPosition(new GeoPoint((int)(lat * 1E6), (int)(lng * 1E6)), 0);
										a.setText(cursor.getString(1));
										a.loadIcon(PanelActivity.this, R.drawable.airport);
										overlays.add(a);
										cursor.moveToNext();
									}
									cursor.close();
								}
							}
							
							if (show_navaids) {
								// check the navaids
								Cursor cursor = db.getNavaids(
										pd.getFloat(PlaneData.LATITUDE),
										pd.getFloat(PlaneData.LONGITUDE));
								if (cursor!=null) {
									cursor.moveToFirst();
									while (!cursor.isAfterLast()) {
										float lat = cursor.getFloat(3);
										float lng = cursor.getFloat(4);
										int type = cursor.getInt(2);
										MapOverlay a = new MapOverlay(PanelActivity.this);
										a.setPosition(new GeoPoint((int)(lat * 1E6), (int)(lng * 1E6)), 0);
	
										switch(type) {
										case 2: // NDB
											a.loadIcon(PanelActivity.this, R.drawable.ndb);
											a.setText(cursor.getString(1)+" "+cursor.getString(6));
											//a.setDescription(cursor.getString(7));
											overlays.add(a);
											break;
										case 3: //VOR and VOR-DME
										case 4: // ILS
										case 5: // LOC
											a.loadIcon(PanelActivity.this, R.drawable.vor);
											a.setText(cursor.getString(1)+" "+cursor.getString(6));
											//a.setDescription(cursor.getString(7));
											overlays.add(a);
											break;
										case 6: // GS
										default:
										}
										cursor.moveToNext();
									}
									cursor.close();
								}
							}
						}
					}
					
					this.publishProgress(pd);
					if (panelView != null && panelView.getVisibility() == View.VISIBLE) {
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
			
			if (db!=null) {
				db.close();
			}
			
			MyLog.d(this, "UDP Thread finished");
			
			return msg;
		}

		@Override
		protected void onProgressUpdate(PlaneData... values) {
			if (firstMessage) {
				showToast(getString(R.string.conn_established), Toast.LENGTH_SHORT);
				firstMessage = false;
			}
			// A new data arrived to the UDP listener
			if (planeOverlay != null && mapView != null) {
					GeoPoint p = new GeoPoint(
							(int)(values[0].getFloat(PlaneData.LATITUDE) * 1E6),
							(int)(values[0].getFloat(PlaneData.LONGITUDE) * 1E6));
					mapView.getController().setCenter(p);
					
					// if there are pending overlays, remove all current overlays and add them
					if (overlays!=null && overlays.size() > 0) {
						mapView.getOverlays().clear();
						mapView.getOverlays().addAll(overlays);
						if (compassOverlay != null) {
							mapView.getOverlays().add(compassOverlay);
						}
						mapView.getOverlays().add(planeOverlay);
						overlays.removeAllElements();
					}
					
					// update the panel and plane overlay
					planeOverlay.setPosition(p, values[0].getFloat(PlaneData.HEADING_MOV));
					if (compassOverlay != null) {
						compassOverlay.setPosition(p, values[0].getFloat(PlaneData.HEADING_MOV), values[0].getFloat(PlaneData.SPEED));
					}
			}
			
			if (panelView != null) {
				panelView.postPlaneData(values[0]);
				// check if the calibratable manager is still running
				if (calibratableManager == null || !calibratableManager.isAlive()) {
			        calibratableManager = new CalibratableSurfaceManager(PreferenceManager.getDefaultSharedPreferences(PanelActivity.this));
			        calibratableManager.start();
			        if (panelView != null) {
			        	panelView.postCalibratableSurfaceManager(calibratableManager);
			        }
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
	    	
	    	if(PanelActivity.this.isFinishing()) {
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
		        	currentDialog = new AlertDialog.Builder(PanelActivity.this).setIcon(R.drawable.ic_launcher)
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
			        txt = txt + getString(R.string.run_fgfs_using) + " --generic=socket,out,10," + readableIP + "," + udpPort + ",udp,andatlas --telnet=" + telnetPort;
			        
			        // show the dialog on screen
					currentDialog = new AlertDialog.Builder(PanelActivity.this).setIcon(R.drawable.ic_launcher)
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
	        	showToast(e.toString() + " " + getString(R.string.critical_error), Toast.LENGTH_LONG);
	        }
		}
	}
}