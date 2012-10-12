package com.tomszom.sensormonitor;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class SensorMonitor extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_monitor);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_sensor_monitor, menu);
        return true;
    }
}
