package com.juanvvc.flightgear.panels;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.juanvvc.flightgear.FGFSConnection;
import com.juanvvc.flightgear.MyLog;
import com.juanvvc.flightgear.PlaneData;
import com.juanvvc.flightgear.R;

public class CommsPanel extends Activity implements OnClickListener {
	private ConnTask connTask;
	private int selectedEditText = -1;
	
	/**
	 * Add identifiers of views to this array to mark them as
	 * "changed, send to fgfs"
	 */
	private ArrayList<Integer> changedfreqs;
	/** A reference to the currently displayed dialog. */
	private AlertDialog currentDialog;
	/** The wakelock to lock the screen and prevent sleeping. */
	private PowerManager.WakeLock wakeLock = null;
	/** If set, use the wakeLock.
	 * TODO: the wakeLock was not always working. Use this option for debugging
	 */
	private static final boolean USE_WAKELOCK = true;
	/** Timeout milliseconds for the UDP socket. */
	private static final int SOCKET_TIMEOUT = 10000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		this.setContentView(R.layout.commspanel);

		changedfreqs = new ArrayList<Integer>();

		Button b;
		b = (Button) this.findViewById(R.id.swapcom1);
		b.setOnClickListener(this);
		b = (Button) this.findViewById(R.id.swapcom2);
		b.setOnClickListener(this);
		b = (Button) this.findViewById(R.id.swapnav1);
		b.setOnClickListener(this);
		b = (Button) this.findViewById(R.id.swapnav2);
		b.setOnClickListener(this);
		b = (Button) this.findViewById(R.id.swapadf);
		b.setOnClickListener(this);
		this.findViewById(R.id.swapcom1).setOnClickListener(this);
		this.findViewById(R.id.swapcom2).setOnClickListener(this);
		this.findViewById(R.id.swapnav1).setOnClickListener(this);
		this.findViewById(R.id.swapnav2).setOnClickListener(this);
		this.findViewById(R.id.swapadf).setOnClickListener(this);
		
		if (this.findViewById(R.id.keyboard) == null) {
			// if no keyboard in the layout (i.e, small screens)
			// then manage the EditTexts with the standard keyboard
			EditText et;
			et = (EditText) this.findViewById(R.id.dme);
			et.addTextChangedListener(new MyTextWatcher(R.id.dme));
			et = (EditText) this.findViewById(R.id.adfrad);
			et.addTextChangedListener(new MyTextWatcher(R.id.adfrad));
			et = (EditText) this.findViewById(R.id.nav1rad);
			et.addTextChangedListener(new MyTextWatcher(R.id.nav1rad));
			et = (EditText) this.findViewById(R.id.nav2rad);
			et.addTextChangedListener(new MyTextWatcher(R.id.nav2rad));
			et = (EditText) this.findViewById(R.id.ssr);
			et.addTextChangedListener(new MyTextWatcher(R.id.ssr));
		} else {
			// Large screens: use our keyboard
			
			this.findViewById(R.id.com1stb).setOnClickListener(this);
			this.findViewById(R.id.com2stb).setOnClickListener(this);
			this.findViewById(R.id.nav1stb).setOnClickListener(this);
			this.findViewById(R.id.nav2stb).setOnClickListener(this);
			this.findViewById(R.id.nav1rad).setOnClickListener(this);
			this.findViewById(R.id.nav2rad).setOnClickListener(this);
			this.findViewById(R.id.dme).setOnClickListener(this);
			this.findViewById(R.id.adfrad).setOnClickListener(this);
			this.findViewById(R.id.adfstb).setOnClickListener(this);
			this.findViewById(R.id.ssr).setOnClickListener(this);
			
			this.findViewById(R.id.key0).setOnClickListener(this);
			this.findViewById(R.id.key1).setOnClickListener(this);
			this.findViewById(R.id.key2).setOnClickListener(this);
			this.findViewById(R.id.key3).setOnClickListener(this);
			this.findViewById(R.id.key4).setOnClickListener(this);
			this.findViewById(R.id.key5).setOnClickListener(this);
			this.findViewById(R.id.key6).setOnClickListener(this);
			this.findViewById(R.id.key7).setOnClickListener(this);
			this.findViewById(R.id.key8).setOnClickListener(this);
			this.findViewById(R.id.key9).setOnClickListener(this);
			this.findViewById(R.id.keyDot).setOnClickListener(this);
			this.findViewById(R.id.keyClear).setOnClickListener(this);
			
			selectedEditText = -1;
		}
	}
	
	/** This textwatcher monitors changes in an EditText. */
	private class MyTextWatcher implements TextWatcher {
		private Integer id;
		/** @param myid The identifier of the view to monitor. Use EditText.addTextChangedListener(new MyTextWatcher(id)) */
		MyTextWatcher(int myid) {
			id = Integer.valueOf(myid);
		}
		@Override
		public void afterTextChanged(Editable s) {
			// after a change, add the identifier of the view to the array of changed frequencies
			synchronized(CommsPanel.this.changedfreqs) {
				changedfreqs.add(id);
			}
		}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
		
	}

	@Override
	protected void onPause() {
		MyLog.i(this, "Pausing threads");
		if (connTask != null) {
			connTask.cancel(true); // TODO: actually, the thread only stops after a timeout
			connTask = null;
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
		if (connTask != null) {
			connTask.cancel(true);
			connTask = null;
		}
	}
	
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	MyLog.i(this, "Starting threads");
    	if (connTask == null) {
    		connTask = (ConnTask) new ConnTask().execute();
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

	private void swapFrequencies(final int id1, final int id2) {
		TextView tv = (TextView) this.findViewById(id1);
		// An EditText is a TextView, also
		TextView stb = (TextView) this.findViewById(id2);
		CharSequence cs = tv.getText();
		tv.setText(stb.getText());
		stb.setText(cs);
		synchronized(changedfreqs) {
			this.changedfreqs.add(Integer.valueOf(id1));
		}
	}

	@Override
	public void onClick(View v) {
		TextView et;
		switch (v.getId()) {
		case R.id.swapcom1:
			this.swapFrequencies(R.id.com1, R.id.com1stb);
			break;
		case R.id.swapcom2:
			this.swapFrequencies(R.id.com2, R.id.com2stb);
			break;
		case R.id.swapnav1:
			this.swapFrequencies(R.id.nav1, R.id.nav1stb);
			break;
		case R.id.swapnav2:
			this.swapFrequencies(R.id.nav2, R.id.nav2stb);
			break;
		case R.id.swapadf:
			this.swapFrequencies(R.id.adf, R.id.adfstb);
			break;
		case R.id.com1stb:
		case R.id.com2stb:
		case R.id.nav1stb:
		case R.id.nav2stb:
		case R.id.adfstb:
		case R.id.nav1rad:
		case R.id.nav2rad:
		case R.id.ssr:
		case R.id.dme:
			if (this.selectedEditText > 0) {
				// deselect currently selected edittext
				this.findViewById(this.selectedEditText).setBackgroundColor(Color.GRAY);
			}
			// select the new one
			this.selectedEditText = v.getId();
			v.setBackgroundColor(Color.GRAY);
			break;
		case R.id.keyClear:
			if (this.selectedEditText > 0) {
				((TextView) this.findViewById(this.selectedEditText)).setText("");
			}
			break;
		case R.id.key1:
		case R.id.key2:
		case R.id.key3:
		case R.id.key4:
		case R.id.key5:
		case R.id.key6:
		case R.id.key7:
		case R.id.key8:
		case R.id.key9:
		case R.id.key0:
		case R.id.keyDot:
			// if v is a Button, it must be the keyboard: append the value
			if (this.selectedEditText > 0) {
				et = (TextView) this.findViewById(this.selectedEditText);
				et.setText(et.getText().toString() + ((Button) v).getText().toString());
				synchronized(this.changedfreqs) {
					changedfreqs.add(v.getId());
				}
			} // if button pressed but no selected edittext, do nothing
			break;
		}
	}
	

	/**
	 * An AsyncTask to receive data from a remote UDP server and manage
	 * connections. When new data is received, frequencies are updated.
	 */
	private class ConnTask extends AsyncTask<Object, PlaneData, String> {
		private String udpport;
		private boolean firstMessage;
		FGFSConnection conn = null;
		// we use this array as a temporal storage for the frequencies.
		// You should check the indexes in the code, but they should go like this:
		// com1, com2, nav1, nav2, adf, dme, com1stb, com2stb...
		private float[] freqs = new float[15];

		@Override
		protected String doInBackground(Object... params) {

			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(CommsPanel.this);

			boolean cancelled = false;
			int waitPeriod = 5000;
			int port = 9000;
			String fgfsIP = "192.168.1.2";

			// read preferences
			try {
				waitPeriod = Integer.parseInt(sp.getString("update_period", "500"));
				// check limits
				waitPeriod = Math.max(waitPeriod, 500);
			} catch (NumberFormatException e) {
				MyLog.w(this, "Config error: wrong update_period=" + sp.getString("update_period", "default")	+ ".");
				waitPeriod = 5000;
			}
			try {
				port = Integer.parseInt(sp.getString("telnet_port", "9000"));
				// check limits
				port = Math.max(port, 1);
			} catch (ClassCastException e) {
				MyLog.w(this, "Config error: wrong port=" + sp.getString("telnet_port", "default"));
				port = 9000;
			}
			fgfsIP = sp.getString("fgfs_ip", "192.168.1.2");
			udpport = sp.getString("udp_port", "5501");

			MyLog.i(this, "Telnet: " + fgfsIP + ":" + port + " " + waitPeriod + "ms");

			try {
				MyLog.e(this, "Trying telnet connection to " + fgfsIP + ":"	+ port);
				conn = new FGFSConnection(fgfsIP, port, CommsPanel.SOCKET_TIMEOUT);
				MyLog.d(this, "Upwards connection ready");
			} catch (IOException e) {
				MyLog.w(this, e.toString());
				conn = null;
				return null;
			}
			
			firstMessage = true;
			// call a progress update to show the first message
			try{
				freqs[0] = conn.getFloat("/instrumentation/comm/frequencies/selected-mhz");
				freqs[1] = conn.getFloat("/instrumentation/comm[1]/frequencies/selected-mhz");
				freqs[2] = conn.getFloat("/instrumentation/nav/frequencies/selected-mhz");
				freqs[3] = conn.getFloat("/instrumentation/nav[1]/frequencies/selected-mhz");
				freqs[4] = conn.getFloat("/instrumentation/adf/frequencies/selected-khz");
				freqs[5] = conn.getFloat("/instrumentation/dme/frequencies/selected-mhz");
				freqs[6] = conn.getFloat("/instrumentation/comm/frequencies/standby-mhz");
				freqs[7] = conn.getFloat("/instrumentation/comm[1]/frequencies/standby-mhz");
				freqs[8] = conn.getFloat("/instrumentation/nav/frequencies/standby-mhz");
				freqs[9] = conn.getFloat("/instrumentation/nav[1]/frequencies/standby-mhz");
				freqs[10] = conn.getFloat("/instrumentation/adf/frequencies/standby-khz");
				freqs[11] = conn.getFloat("/instrumentation/nav/radials/selected-deg");
				freqs[12] = conn.getFloat("/instrumentation/nav[1]/radials/selected-deg");
				freqs[13] = conn.getFloat("/instrumentation/adf/rotation-deg");
				freqs[14] = conn.getFloat("/instrumentation/adf/rotation-deg"); // TODO
				this.publishProgress((PlaneData)null);
			} catch (IOException e) {
				return "Couldn't read frequencies";
			}
			
			String prop = null, prop2 = null;
			float value = 0.0f, value2 = 0.0f;
			String msg = null;
			
			while (!cancelled) {
				try {
					synchronized (changedfreqs) {
						for (Integer cs : changedfreqs) {
							prop = null;
							prop2 = null;
							switch (cs.intValue()) {
							case R.id.com1:
								prop = "/instrumentation/comm/frequencies/selected-mhz";
								value = Float.parseFloat(((TextView) CommsPanel.this.findViewById(R.id.com1)).getText().toString());
								prop2 = "/instrumentation/comm/frequencies/standby-mhz";
								value2 = Float.parseFloat(((TextView) CommsPanel.this.findViewById(R.id.com1stb)).getText().toString());
								break;
							case R.id.com2:
								prop = "/instrumentation/comm[1]/frequencies/selected-mhz";
								value = Float.parseFloat(((TextView) CommsPanel.this.findViewById(R.id.com2)).getText().toString());
								prop2 = "/instrumentation/comm[1]/frequencies/standby-mhz";
								value2 = Float.parseFloat(((TextView) CommsPanel.this.findViewById(R.id.com2stb)).getText().toString());
								break;
							case R.id.nav1:
								prop = "/instrumentation/nav/frequencies/selected-mhz";
								value = Float.parseFloat(((TextView) CommsPanel.this.findViewById(R.id.nav1)).getText().toString());
								prop2 = "/instrumentation/nav/frequencies/standby-mhz";
								value2 = Float.parseFloat(((TextView) CommsPanel.this.findViewById(R.id.nav1stb)).getText().toString());
								break;
							case R.id.nav2:
								prop = "/instrumentation/nav[1]/frequencies/selected-mhz";
								value = Float.parseFloat(((TextView) CommsPanel.this.findViewById(R.id.nav2)).getText().toString());
								prop2 = "/instrumentation/nav[1]/frequencies/standby-mhz";
								value2 = Float.parseFloat(((TextView) CommsPanel.this.findViewById(R.id.nav2stb)).getText().toString());
								break;
							case R.id.adf:
								prop = "/instrumentation/adf/frequencies/selected-khz";
								value = Float.parseFloat(((TextView) CommsPanel.this.findViewById(R.id.adf)).getText().toString());
								prop2 = "/instrumentation/adf/frequencies/standby-khz";
								value2 = Float.parseFloat(((TextView) CommsPanel.this.findViewById(R.id.adfstb)).getText().toString());
								break;
							case R.id.dme:
								prop = "/instrumentation/dme/frequencies/selected-mhz";
								value = Float.parseFloat(((EditText) CommsPanel.this.findViewById(R.id.dme)).getText().toString());
								prop2 = null;
								break;
							case R.id.nav1rad:
								prop = "/instrumentation/nav/radials/selected-deg";
								value = Float.parseFloat(((EditText) CommsPanel.this.findViewById(R.id.nav1rad)).getText().toString());
								prop2 = null;
								break;
							case R.id.nav2rad:
								prop = "/instrumentation/nav[1]/radials/selected-deg";
								value = Float.parseFloat(((EditText) CommsPanel.this.findViewById(R.id.nav2rad)).getText().toString());
								prop2 = null;
								break;
							case R.id.adfrad:
								prop = "/instrumentation/adf/rotation-deg";
								value = Float.parseFloat(((EditText) CommsPanel.this.findViewById(R.id.adfrad)).getText().toString());
								prop2 = null;
								break;
							case R.id.ssr:
								prop = "/instrumentation/adf/rotation-deg"; // TODO
								value = Float.parseFloat(((EditText) CommsPanel.this.findViewById(R.id.ssr)).getText().toString());
								prop2 = null;
								break;
							default:
								// does nothing. For example, nav1stb was changed
							}
							if (prop != null) {
								conn.setFloat(prop, value);
							}
							if (prop2 != null) {
								conn.setFloat(prop2, value2);
							}
						}
					}
					Thread.sleep(waitPeriod);
					cancelled = conn.isClosed();
				} catch (SocketTimeoutException e) {
					MyLog.e(this, e.toString());
					cancelled = true;
					msg = getString(R.string.conn_timeout);
				} catch (InterruptedException e) {
					cancelled = true;
				} catch (IOException e) {
					MyLog.w(this, MyLog.stackToString(e));
					msg = "Connection error";
					cancelled = true;
				} catch(NumberFormatException e) {
					// most times you can safely ignore this exception, since it means
					// the user removed all numbers from the EditText
					MyLog.w(this, "Number format exception");
				}
			}

			try {
				conn.close();
			} catch (IOException e) {
				MyLog.w(this, "Error closing connection: " + e.toString());
			}

			MyLog.d(this, "Connection thread finished");

			return msg;
		}

		@Override
		protected void onProgressUpdate(PlaneData... values) {
			if (firstMessage && conn!= null) {
				Toast.makeText(CommsPanel.this,	getString(R.string.conn_established), Toast.LENGTH_LONG).show();
				firstMessage = false;
				//Update messages
				((TextView) findViewById(R.id.com1)).setText(Float.toString(freqs[0]));
				((TextView) findViewById(R.id.com2)).setText(Float.toString(freqs[1]));
				((TextView) findViewById(R.id.nav1)).setText(Float.toString(freqs[2]));
				((TextView) findViewById(R.id.nav2)).setText(Float.toString(freqs[3]));
				((TextView) findViewById(R.id.adf)).setText(Float.toString(freqs[4]));
				((EditText) findViewById(R.id.dme)).setText(Float.toString(freqs[5]));
				((TextView) findViewById(R.id.com1stb)).setText(Float.toString(freqs[6]));
				((TextView) findViewById(R.id.com2stb)).setText(Float.toString(freqs[7]));
				((TextView) findViewById(R.id.nav1stb)).setText(Float.toString(freqs[8]));
				((TextView) findViewById(R.id.nav2stb)).setText(Float.toString(freqs[9]));
				((TextView) findViewById(R.id.adfstb)).setText(Float.toString(freqs[10]));
				((EditText) findViewById(R.id.nav1rad)).setText(Float.toString(freqs[11]));
				((EditText) findViewById(R.id.nav2rad)).setText(Float.toString(freqs[12]));
				((EditText) findViewById(R.id.adfrad)).setText(Float.toString(freqs[13]));
				((EditText) findViewById(R.id.ssr)).setText(Float.toString(freqs[14]));
				
				// freqs array is not used any more
				freqs = null;
			}
			
//			 // check if the calibratable manager is still running
//			 if (calibratableManager == null || !calibratableManager.isAlive()) {
//				 calibratableManager = new
//				 CalibratableSurfaceManager(PreferenceManager.getDefaultSharedPreferences(InstrumentPanel.this));
//				 calibratableManager.start();
//				 if (panelView != null) {
//					 panelView.postCalibratableSurfaceManager(calibratableManager);
//				 }
//			 }
		}

		@Override
		protected void onPostExecute(String msg) {
			if (isCancelled()) {
				// I think that if isCancelled(), this method is not called.
				// Still, just in case
				return;
			}

			if (currentDialog != null) {
				currentDialog.dismiss();
			}

			if (CommsPanel.this.isFinishing()) {
				return;
			}

			try {
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
					// if no IP in the WiFi network.
					currentDialog = new AlertDialog.Builder(CommsPanel.this)
							.setIcon(R.drawable.ic_launcher)
							.setTitle(getString(R.string.warning))
							.setMessage(
									getString(R.string.network_not_detected)
											+ " "
											+ getString(R.string.critical_error))
							.setPositiveButton(
									android.R.string.ok,
									new android.content.DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											finish();
										}
									}).show();
				} else {
					// convert Ip to a readable IP
					String readableIP = String.format("%d.%d.%d.%d",
							(ipAddress & 0xff), (ipAddress >> 8 & 0xff),
							(ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));

					// add information about fgfs+++
					txt = txt + getString(R.string.run_fgfs_using)
							+ " --generic=socket,out,10," + readableIP + ","
							+ udpport + ",udp,andatlas --telnet=9000";

					// show the dialog on screen
					currentDialog = new AlertDialog.Builder(CommsPanel.this)
							.setIcon(R.drawable.ic_launcher)
							.setTitle(getString(R.string.warning))
							.setMessage(txt)
							.setPositiveButton(
									android.R.string.ok,
									new android.content.DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// when the user click ok, the
											// receivers restart
											connTask = (ConnTask) new ConnTask()
													.execute();
										}
									})
							.setNegativeButton(
									R.string.quit,
									new android.content.DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											finish();
										}
									}).show();
				}
			} catch (Exception e) {
				Toast.makeText(
						CommsPanel.this,
						e.toString() + " " + getString(R.string.critical_error),
						Toast.LENGTH_LONG).show();
			}
		}
	}
}
