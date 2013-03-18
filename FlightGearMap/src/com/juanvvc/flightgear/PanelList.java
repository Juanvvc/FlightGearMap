package com.juanvvc.flightgear;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.juanvvc.flightgear.panels.InstrumentPanel;
import com.juanvvc.flightgear.panels.MapInstrumentPanel;
import com.juanvvc.flightgear.panels.PanelView;

/** This activity shows a list of the available distributions. */
public class PanelList extends Activity implements OnItemClickListener{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.panellist);
		
		GridView gridview = (GridView) this.findViewById(R.id.gridview);
		
		// TODO: choose a better way to manage this list
		gridview.setAdapter(new DistributionAdapter());
		gridview.setOnItemClickListener(this);
//		new ArrayAdapter<String>(
//				this,
//				android.R.layout.simple_list_item_1,
//				new String[]{"Map and simple controls", "Only map", "Cessna 172", "Seneca II", "Generic glass panel"}
//				);
//		this.setListAdapter(adapter);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Intent intent = null;
		Bundle bundle = new Bundle();
		
		// This select muct match the order of the images in the THUMBS array (see bellow)
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
			bundle.putBoolean("onlymap", false);
			bundle.putInt("distribution", PanelView.Distribution.C172_INSTRUMENTS);
			intent.putExtras(bundle);
			break;
		case 3: // Seneca II
			intent = new Intent(this.getApplicationContext(), InstrumentPanel.class);
			bundle.putBoolean("onlymap", false);
			bundle.putInt("distribution", PanelView.Distribution.SENECAII_PANEL);
			intent.putExtras(bundle);
			break;			
		case 4: // generic glass panel
			intent = new Intent(this.getApplicationContext(), MapInstrumentPanel.class);
			bundle.putBoolean("onlymap", false);
			bundle.putBoolean("liquid", true);
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
	
	private class DistributionAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return THUMBS.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		private int[] THUMBS = {R.drawable.dist_simplemap, R.drawable.dist_onlymap, R.drawable.dist_c172, R.drawable.dist_senecaii, R.drawable.dist_liquid, R.drawable.dist_onlymap};
		private int[] THUMBS_LABELS = {R.string.dist_simplemap, R.string.dist_onlymap, R.string.dist_c172, R.string.dist_senecaii, R.string.dist_liquid, R.string.dist_comms};

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
