package com.juanvvc.flightgear.panels;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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

import com.juanvvc.flightgear.MyLog;
import com.juanvvc.flightgear.PlaneData;
import com.juanvvc.flightgear.R;
import com.juanvvc.flightgear.instruments.CalibratableSurfaceManager;

/** Main activity of the application.
 * @author juanvi
 */
public class InstrumentPanel extends Activity {
	/** Reference to the panel view. */
	private PanelView panelView = null;
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
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
        setContentView(R.layout.instruments);
        
        panelView = (PanelView) this.findViewById(R.id.panel);
        this.setDistribution();
    }
    
    /** Load preferences. */
    public void loadPreferences() {
    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
  		String port = sp.getString("udp_port", "5501");
	
  		// select the UDP port
  		try {
  			udpPort = Integer.valueOf(port);
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

	/** Sets a distribution of instruments on screen.
	 * 
	 * @return True if the distribution is set
	 */
	private boolean setDistribution() {
    	panelView = (PanelView) findViewById(R.id.panel);
    	
    	panelView.setVisibility(View.VISIBLE);
    	panelView.setDistribution(PanelView.Distribution.C172_INSTRUMENTS);
    	panelView.invalidate();

    	if (this.calibratableManager != null) {
    		this.calibratableManager.empty();
    		panelView.postCalibratableSurfaceManager(this.calibratableManager);
    	}
    	return true;
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
			
			MyLog.d(this, "UDP Thread started. Listening port: " + params[0]);
			
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
					if (panelView != null) {
						panelView.postPlaneData(pd);
						this.publishProgress(pd);
						if (panelView.getVisibility() == View.VISIBLE) {
							panelView.redraw();
						}
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
			
			MyLog.d(this, "UDP Thread finished");
			
			return msg;
		}

		@Override
		protected void onProgressUpdate(PlaneData... values) {
			if (firstMessage) {
				Toast.makeText(InstrumentPanel.this, getString(R.string.conn_established), Toast.LENGTH_LONG).show();
				firstMessage = false;
			}
			
			// check if the calibratable manager is still running
			if (calibratableManager == null || !calibratableManager.isAlive()) {
		        calibratableManager = new CalibratableSurfaceManager(PreferenceManager.getDefaultSharedPreferences(InstrumentPanel.this));
		        calibratableManager.start();
		        if (panelView != null) {
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
	    	
	    	if(InstrumentPanel.this.isFinishing()) {
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
		        	currentDialog = new AlertDialog.Builder(InstrumentPanel.this).setIcon(R.drawable.ic_launcher)
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
					currentDialog = new AlertDialog.Builder(InstrumentPanel.this).setIcon(R.drawable.ic_launcher)
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
	        	Toast.makeText(InstrumentPanel.this, e.toString() + " " + getString(R.string.critical_error), Toast.LENGTH_LONG).show();
	        }
		}
	}
}