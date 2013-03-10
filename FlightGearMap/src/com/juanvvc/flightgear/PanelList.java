package com.juanvvc.flightgear;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.juanvvc.flightgear.panels.InstrumentPanel;
import com.juanvvc.flightgear.panels.MapInstrumentPanel;
import com.juanvvc.flightgear.panels.PanelView;

/** This activity shows a list of the available distributions. */
public class PanelList extends ListActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.panellist);
		
		// TODO: choose a better way to manage this list
		ListAdapter adapter = new ArrayAdapter<String>(
				this,
				android.R.layout.simple_list_item_1,
				new String[]{"Map and simple controls", "Only map", "Cessna 172", "Seneca II", "Generic glass panel"}
				);
		this.setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = null;
		Bundle bundle = new Bundle();
		
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
}
