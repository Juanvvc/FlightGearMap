package com.juanvvc.flightgear;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.juanvvc.flightgear.panels.CommsPanel;
import com.juanvvc.flightgear.panels.InstrumentPanel;
import com.juanvvc.flightgear.panels.MapInstrumentPanel;
import com.juanvvc.flightgear.panels.PanelView;

/** This activity shows a list of the available panel distributions. */
public class PanelList extends Activity implements OnItemClickListener{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.panellist);
		
		GridView gridview = (GridView) this.findViewById(R.id.gridview);
		
		gridview.setAdapter(new DistributionAdapter());
		gridview.setOnItemClickListener(this);

		// if it is the first run of this version, show the changelog.txt
		ChangeLog cl = new ChangeLog(this);
        if (cl.firstRun())
            cl.getLogDialog().show();

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Intent intent = null;
		Bundle bundle = new Bundle();
		
		// This index must match the order of the images in the THUMBS array (see bellow)
		// We will start a MapInstrumentPanel or a InstrumentPanel depending on the choose
		switch(position) {
		case 0: // map and simple controls
			intent = new Intent(this.getApplicationContext(), MapInstrumentPanel.class);
			bundle.putBoolean("onlymap", false);
			intent.putExtras(bundle);
			break;
		case 1: // only map
			intent = new Intent(this.getApplicationContext(), MapInstrumentPanel.class);
			bundle.putBoolean("onlymap", true);
			intent.putExtras(bundle);
			break;
		case 2: // Cessna 172
			intent = new Intent(this.getApplicationContext(), InstrumentPanel.class);
			bundle.putInt("distribution", PanelView.Distribution.C172_INSTRUMENTS);
			intent.putExtras(bundle);
			break;
		case 3: // Cessna 337
			intent = new Intent(this.getApplicationContext(), InstrumentPanel.class);
			bundle.putInt("distribution", PanelView.Distribution.C337_INSTRUMENTS);
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
//			intent = new Intent(this.getApplicationContext(), MapInstrumentPanel.class);
//			bundle.putBoolean("onlymap", false);
//			bundle.putBoolean("liquid", true);
//			intent.putExtras(bundle);
//			break;
		case 4: // comms panel
			//intent = new Intent(this.getApplicationContext(), InstrumentPanel.class);
			intent = new Intent(this.getApplicationContext(), CommsPanel.class);
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
		
		// List of thumbnails of the distributions
		private int[] THUMBS = {R.drawable.dist_simplemap, R.drawable.dist_onlymap, R.drawable.dist_c172, R.drawable.dist_c337, R.drawable.dist_comms};
		// List of labels
		private int[] THUMBS_LABELS = {R.string.dist_simplemap, R.string.dist_onlymap, R.string.dist_c172, R.string.dist_c337, R.string.dist_comms};

		/** Creates a thumbnail and label of a distribution */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			ViewGroup viewGroup;
	        ImageView imageView;
	        TextView label;
	        if (convertView == null) {  // if it's not recycled, initialize some attributes
				viewGroup = (ViewGroup) LayoutInflater.from(PanelList.this).inflate(R.layout.distthumb, null);
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
