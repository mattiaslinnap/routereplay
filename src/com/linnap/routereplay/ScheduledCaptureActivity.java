package com.linnap.routereplay;

import java.io.FileNotFoundException;

import com.linnap.routereplay.capture.CaptureProgress;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class ScheduledCaptureActivity extends Activity {

	private Handler handler;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scheduledcapture);
        
        this.handler = new Handler();
        
        ApplicationGlobals app = (ApplicationGlobals)getApplicationContext();
        if (app.capture != null) {
        	Log.e(Utils.TAG, "Capture already exists");
        } else {
        	try {
        		Log.i(Utils.TAG, "New capture");
    			app.initializeCapture();
    			app.advanceCaptureAndScheduleNextWakeup();
    			((TextView)findViewById(R.id.capture_status)).setText("Experiment started...");
    		} catch (FileNotFoundException e) {
    			Log.e(Utils.TAG, "" + e);
    		}
        }
    }
	
	public void onResume() {
		super.onResume();
		ApplicationGlobals app = (ApplicationGlobals)getApplicationContext();
		CaptureProgress capture = app.capture;
		Log.d(Utils.TAG, "Activity resuming. Capture " + (capture == null ? "is null" : "exists"));
		
		if (capture != null) {
			Log.d(Utils.TAG, "ScheduledCapture resuming. Capture exists.");
			if (capture.nextScheduleIndex >= capture.replay.schedule.size()) {
				((TextView)findViewById(R.id.capture_status)).setText("Experiment finished");
			} else {
				((TextView)findViewById(R.id.capture_status)).setText("Experiment at index " + capture.nextScheduleIndex + " of " + capture.replay.schedule.size());
			}
		} else {
			Log.d(Utils.TAG, "ScheduledCapture resuming. Capture is null.");
		}
	}
	
	public void onPause() {
		super.onPause();
		Log.d(Utils.TAG, "ScheduledCapture pausing");
	}
}
