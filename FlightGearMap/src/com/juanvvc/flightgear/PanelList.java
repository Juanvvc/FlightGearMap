package com.juanvvc.flightgear;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.juanvvc.flightgear.panels.InstrumentPanel;
import com.juanvvc.flightgear.panels.LiquidPanel;
import com.juanvvc.flightgear.panels.MapControls;

public class PanelList extends ListActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.panellist);
		
		ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[]{"Map and simple controls", "Only map", "Cessna 172", "Generic glass panel"});
		this.setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = null;
		Bundle bundle = new Bundle();
		
		switch(position) {
		case 0:
			intent = new Intent(this.getApplicationContext(), MapControls.class);
			bundle.putBoolean("onlymap", false);
			intent.putExtras(bundle);
			break;
		case 1:
			intent = new Intent(this.getApplicationContext(), MapControls.class);
			bundle.putBoolean("onlymap", true);
			intent.putExtras(bundle);
			break;
		case 2:
			intent = new Intent(this.getApplicationContext(), InstrumentPanel.class);
			bundle.putBoolean("onlymap", false);
			intent.putExtras(bundle);
			break;
		case 3:
			intent = new Intent(this.getApplicationContext(), LiquidPanel.class);
			bundle.putBoolean("onlymap", false);
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
	    inflater.inflate(R.menu.available_distributions, menu);
	    return true;
	}
}
