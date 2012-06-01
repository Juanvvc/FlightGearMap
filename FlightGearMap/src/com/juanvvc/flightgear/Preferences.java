package com.juanvvc.flightgear;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO: this is deprecated, but still much easier to code than current preferences
		this.addPreferencesFromResource(R.xml.preferences);
	}

}
