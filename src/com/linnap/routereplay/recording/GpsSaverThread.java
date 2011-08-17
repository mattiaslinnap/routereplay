package com.linnap.routereplay.recording;

import android.app.AlarmManager;
import android.content.Context;

import com.linnap.routereplay.Beeper;
import com.linnap.routereplay.Utils;

public class GpsSaverThread extends Thread {
	
	public static final String TAG = Utils.TAG;
	public static final int WAKEUP_REQUEST_CODE = 1;

	private Context context;
	private AlarmManager alarmManager;
	private Beeper beeper;
	
//	public GpsSaverThread(Context context) {
//		this.context = context;
//		this.alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
//		this.beeper = new Beeper(context);		
//	}
//	
//	public void beepWithDelay() {
//		Intent intent = new Intent(this.context, AlarmReceiver.class);
//		PendingIntent sender = PendingIntent.getBroadcast(context, WAKEUP_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//		
//		long wakeupMillis = SystemClock.elapsedRealtime() + 1000;
//		alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, wakeupMillis, sender);
//		Log.i(TAG, "wakeup at " + wakeupMillis);
//	}
//	
	public GpsSaverThread(Context context) {
	}
	
	public void run() {
		// Data needed:
		// Schedule
		// Filename
		
		// Acquire wakelock
		// Open File		
		// Start GPS
		// Wait for X seconds
		// Stop GPS
		// Close File
		// set Broadcast Alarm
		// release wakelock
	}

}
