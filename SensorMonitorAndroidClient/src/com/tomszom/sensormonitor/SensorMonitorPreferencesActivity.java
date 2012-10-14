package com.tomszom.sensormonitor;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

public class SensorMonitorPreferencesActivity extends PreferenceActivity {

	private final String tag = getClass().getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(tag, "onCreate");
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.sensormonitor_options);
	}
	
}
