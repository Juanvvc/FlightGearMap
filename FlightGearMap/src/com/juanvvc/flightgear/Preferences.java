package com.juanvvc.flightgear;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// pre API 11 (version < Android 3.0)
      addPreferencesFromResource(R.xml.preferences);

      
////post API 11 (version > Android 3.0)
//      // Display the fragment as the main content.
//      getFragmentManager().beginTransaction()
//              .replace(android.R.id.content, new SettingsFragment())
//              .commit();
//
//  }
//  
//  public static class SettingsFragment extends PreferenceFragment {
//      @Override
//      public void onCreate(Bundle savedInstanceState) {
//          super.onCreate(savedInstanceState);
//
//          // Load the preferences from an XML resource
//          addPreferencesFromResource(R.xml.preferences);
//      }
  }

}
