package com.linnap.routereplay.alarmcapture;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.linnap.routereplay.ApplicationGlobals;
import com.linnap.routereplay.Utils;
import com.linnap.routereplay.captureutil.EventSaver;

public class AlarmReceiver extends BroadcastReceiver {
	public static final int GPS_THREAD_WAKEUP_CODE = 1;
	public static final String EXPECTED_TIME_KEY = "expected-time";
	public static final String CAPTURE_FOR_KEY = "capture-time";
	
	public static final String ACTION_KEY = "action";
	public static final String ACTION_GPS = "gps";
	public static final String ACTION_FINISH = "finish";
	
	public void onReceive(Context context, Intent intent) {
		String action = intent.getStringExtra(ACTION_KEY);
		long expectedWakeup = intent.getLongExtra(EXPECTED_TIME_KEY, -1);
		long now = SystemClock.elapsedRealtime();
		long receivedLate = now - expectedWakeup;
		long captureFor = intent.getLongExtra(CAPTURE_FOR_KEY, -1);
		
		Log.i(Utils.TAG, "Alarm received for " + action);
		
		ApplicationGlobals app = (ApplicationGlobals)context.getApplicationContext();
				
		if (app.captureSaver != null) {
			if (Math.abs(receivedLate) > 500) {
				logLateAlarm(app.captureSaver, action, receivedLate);
				Utils.alarm(context);
			}
			
			if (action.equals(ACTION_GPS)) {
				// TODO: Is it important to keep a reference somewhere?
				new GpsCapturePeriod(context, captureFor, app.captureSaver);
			} else if (action.equals(ACTION_FINISH)) {
				// Terminate capture.
				app.finishCapture();
			} else {
				Log.e(Utils.TAG, "Unknown action " + action);
				Utils.alarm(context);
			}
		} else {
			Log.e(Utils.TAG, "Alarm for " + action + " received with no capture saver");
			Utils.alarm(context);
		}
	}
	
	void logLateAlarm(EventSaver saver, String action, long receivedLate) {
		Log.e(Utils.TAG, "Alarm for " + action + " received " + receivedLate + " late.");
		
		JSONObject json = new JSONObject();
		try {
			json.put("action", action);
			json.put("late", receivedLate);
		} catch (JSONException e) {
			Log.e(Utils.TAG, "JSONException", e);
		}
		saver.addEvent("alarm_late", json);
	}
}
