package com.juanvvc.flightgear;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.juanvvc.flightgear.panels.PanelView;

/* ////////////////////////////////////////
  check list: before uploading a new version to Google Play:
  
  1.- Create the normal version:
  - List the changes and date in res/raw/changelog.txt
  - Check the version number in the manifest: it should be higher than the one in Google Play
  - Commit the version to git and set a new flag:
      git commit -a -m blahblahblah
      git tag v2.0
      git push --tags github
      git push --tags linsertel
  - Set DEBUG_VERSION to false in this file
  - Export the project to FligthGear.apk
  - Signature: ~/.android/myjuanvvc.keystore
  - Send to Google Play. Remember listing the last changes and updating the description, if necessary
  
  2.- Create the donate version:
  - Set DONATE_VERSION to true in this file
  - Change the icon in the manifest to ic_launcherplus
  - RMB on each package Refactor->Rename to com.juanvvc.flightgeardonate
  - RMB on the project name->Android tools->rename application package to com.juanvvc.flightgeardonate
  - Run the project to check if it is working!
  - Export the project to FlightGearDonate.apk
  - Signature: ~/.android/myjuanvvc.keystore
  - Send to Google Play. Remember listing the last changes and updating the description, if necessary
  
  3.- Back to development version:
  - Undo all changes. Easy way:
      Exit Eclipse
      git --hard reset
  - Open Eclipse again, ok to the warning message
  - Start a new iteration by updating the version number in the manifest: one higher
      
///////////////////////////////////////// */

/** This activity shows a list of the available panel distributions. */
public class PanelListActivity extends Activity implements OnItemClickListener{
	
	/** Set this to true if this is the donate version.
	 * The donate version does not show adds and it makes sure that the debug options are not set.
	 * Currently, these are the only differences.
	 * If you change this option, remember changing the icon as well and Android Tools->Rename application package
	 */
	private static final boolean DONATE_VERSION = false;
	/** If on, it is the debug version */
	private static final boolean DEBUG_VERSION = true;
	
	// List of thumbnails of the distributions
	private static final int[] THUMBS = {R.drawable.dist_simplemap, R.drawable.dist_onlymap, R.drawable.dist_c172, R.drawable.dist_c337, R.drawable.dist_comms, R.drawable.dist_single, R.drawable.dist_basic};
	// List of labels
	private static final int[] THUMBS_LABELS = {R.string.dist_simplemap, R.string.dist_onlymap, R.string.dist_c172, R.string.dist_c337, R.string.dist_comms, R.string.single_instrument, R.string.basic_instruments};

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.panellist);
		
		MyLog.setDebug(DEBUG_VERSION);
		
		GridView gridview = (GridView) this.findViewById(R.id.gridview);
		if (gridview != null) {
			// the layout has a gridview: it is a large screen
			gridview.setAdapter(new DistributionAdapter());
			gridview.setOnItemClickListener(this);
		} else {
			// the layout does not have a gridview: small screen. Use a simple list
			ListView listview = (ListView) this.findViewById(R.id.listview);
			String[] labels = new String[THUMBS_LABELS.length];
			for (int i=0; i<labels.length; i++) {
				labels[i] = this.getResources().getString(THUMBS_LABELS[i]);
			}
			listview.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, labels));
			listview.setOnItemClickListener(this);
		}

		// if it is the first run of this version, show the changelog.txt
		ChangeLog cl = new ChangeLog(this);
        if (cl.firstRun() || MyLog.isDebug()) {
            cl.getFullLogDialog().show();
        }
        
        // Create the ads
        if (!DONATE_VERSION) {
        	LinearLayout layout = (LinearLayout)findViewById(R.id.panelviewLayout);
	        
        	TextView tv = new TextView(this);
        	tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        	tv.setText(R.string.consider_donating);
        	tv.setMovementMethod(LinkMovementMethod.getInstance());
        	layout.addView(tv);
        	
	        AdView adView = new AdView(this, AdSize.BANNER, "a15196dbbc3193b");
	        layout.addView(adView);	        
	        AdRequest adRequest = new AdRequest();
	        if (MyLog.isDebug()) {
	        	adRequest.addTestDevice("874C587B68F6782F0CD99504C02613A8"); // Tablet
	        	adRequest.addTestDevice("DD57E9E77A859C5F4EE8C1F52334557B"); // HTC Phone
	        	adRequest.addTestDevice("6BBDE7DC8D834F4C186AB2A8A4B64D9B"); //CHUWI
	        }
	        adView.loadAd(adRequest);
        } else {
        	// if we are in the donate version, make sure the debug options are not set. Useful for debugging.
        	MyLog.setDebug(false);
        }
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Intent intent = null;
		Bundle bundle = new Bundle();
		
		// This index must match the order of the images in the THUMBS array (see bellow)
		// We will start a MapInstrumentPanel or a InstrumentPanel depending on the choose
		switch(position) {
		case 0: // map and simple controls
			intent = new Intent(this.getApplicationContext(), InstrumentActivity.class);
			bundle.putBoolean("showmap", true);
			bundle.putBoolean("onlymap", false);
			intent.putExtras(bundle);
			break;
		case 1: // only map
			intent = new Intent(this.getApplicationContext(), InstrumentActivity.class);
			bundle.putBoolean("showmap", true);
			bundle.putBoolean("onlymap", true);
			intent.putExtras(bundle);
			break;
		case 2: // Cessna 172
			intent = new Intent(this.getApplicationContext(), InstrumentActivity.class);
			bundle.putInt("distribution", PanelView.Distribution.C172_INSTRUMENTS);
			bundle.putBoolean("showmap", false);
			intent.putExtras(bundle);
			break;
		case 3: // Cessna 337
			intent = new Intent(this.getApplicationContext(), InstrumentActivity.class);
			bundle.putInt("distribution", PanelView.Distribution.C337_INSTRUMENTS);
			bundle.putBoolean("showmap", false);
			intent.putExtras(bundle);
			break;
// TODO: the SenecaII can not be chosen because currently is very similar to the Cessna337.
//		case 4: // Seneca II
//			intent = new Intent(this.getApplicationContext(), InstrumentPanel.class);
//			bundle.putInt("distribution", PanelView.Distribution.SENECAII_PANEL);
//			intent.putExtras(bundle);
//			break;			
// TODO: I don't have enough knowledge to develop a working glass cockpit
//		case 4: // generic glass panel
//			intent = new Intent(this.getApplicationContext(), InstrumentActivity.class);
//			bundle.putBoolean("onlymap", false);
//			bundle.putBoolean("liquid", true);
//			intent.putExtras(bundle);
//			break;
		case 4: // comms panel
			//intent = new Intent(this.getApplicationContext(), InstrumentPanel.class);
			intent = new Intent(this.getApplicationContext(), CommsActivity.class);
			intent.putExtras(bundle);
			break;
		case 5: // single instrument panel
			intent = new Intent(this.getApplicationContext(), SingleInstrumentActivity.class);
			intent.putExtras(bundle);
			break;
		case 6: // basic instruments
			intent = new Intent(this.getApplicationContext(), InstrumentActivity.class);
			bundle.putInt("distribution", PanelView.Distribution.BASIC_HORIZONTAL_PANEL);
			bundle.putBoolean("showmap", false);
			intent.putExtras(bundle);
		default:
		}
		
		if (intent != null) {
			this.startActivity(intent);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
        case R.id.settings:
	        Intent intent = new Intent(this.getApplicationContext(), Preferences.class);
	        this.startActivity(intent);
	        return true;
        default:
        	return super.onOptionsItemSelected(item);
	    }
	}
	
	/** The Adapter of the available distributions */
	private class DistributionAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return THUMBS.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
		
		/** Creates a thumbnail and label of a distribution */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			ViewGroup viewGroup;
	        ImageView imageView;
	        TextView label;
	        if (convertView == null) {  // if it's not recycled, initialize some attributes
				viewGroup = (ViewGroup) LayoutInflater.from(PanelListActivity.this).inflate(R.layout.distthumb, null);
				imageView = (ImageView) viewGroup.findViewById(R.id.thumb);
	            imageView.setLayoutParams(new LinearLayout.LayoutParams(320, 240));
	            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
	            imageView.setPadding(8, 8, 8, 1);
	            label = (TextView) viewGroup.findViewById(R.id.label);
	        } else {
	            viewGroup = (ViewGroup) convertView;
				imageView = (ImageView) viewGroup.findViewById(R.id.thumb);
				label = (TextView) viewGroup.findViewById(R.id.label);
	        }
	        
            imageView.setImageResource(THUMBS[position]);
            label.setText(THUMBS_LABELS[position]);
	        	        
	        return viewGroup;
		}
		
	}
}
