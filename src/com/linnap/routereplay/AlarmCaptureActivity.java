package com.linnap.routereplay;

import java.io.FileNotFoundException;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class AlarmCaptureActivity extends Activity {
	
	ApplicationGlobals app;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarmcapture);
		
		app = (ApplicationGlobals)getApplicationContext();
		app.getWakelock().acquire();
		
		try {
			
			
			Log.d(Utils.TAG, "Now is " + SystemClock.elapsedRealtime());
			
			try {
				if (app.loadedReplay != null) {
					app.initializeCapture();
				} else {
					Toast.makeText(this, "ERROR: no loaded replay", Toast.LENGTH_LONG).show();
					Utils.alarm(this);
				}
			} catch (FileNotFoundException e) {
				Toast.makeText(this, "ERROR: cannot open file", Toast.LENGTH_LONG).show();
				Log.e(Utils.TAG, "Cannot open saver file!", e);
				Utils.alarm(this);
			}
		} finally {		
			app.getWakelock().release();
		}
	}
	
	
}
