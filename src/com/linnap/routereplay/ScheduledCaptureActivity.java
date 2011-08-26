package com.linnap.routereplay;

import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.TextView;

import com.linnap.routereplay.capture.CaptureProgress;

public class ScheduledCaptureActivity extends Activity {

	private Handler handler;
	private WakeLock wakeLock;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scheduledcapture);
        
        this.handler = new Handler();
        wakeLock = ((PowerManager)getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Utils.TAG);
        wakeLock.setReferenceCounted(false);
        wakeLock.acquire();
        
        ApplicationGlobals app = (ApplicationGlobals)getApplicationContext();
        if (app.capture != null) {
        	Log.e(Utils.TAG, "Capture already exists");
        } else {
        	try {        		
        		Log.i(Utils.TAG, "New capture");
    			app.initializeCapture();
    			Utils.sleepLogInterrupt(10000);
    			app.insertPulse();
    			app.advanceCaptureAndScheduleNextWakeup();
    			((TextView)findViewById(R.id.capture_status)).setText("Experiment started...");
    		} catch (FileNotFoundException e) {
    			Log.e(Utils.TAG, "" + e);
    		}
        }
        wakeLock.release();
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
