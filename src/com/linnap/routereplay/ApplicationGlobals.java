package com.linnap.routereplay;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.net.Uri;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.linnap.routereplay.alarmcapture.AlarmReceiver;
import com.linnap.routereplay.captureutil.EventSaver;
import com.linnap.routereplay.replay.Replay;

public class ApplicationGlobals extends Application {
	// Some fields that must persist no matter which Activity is visible - including when all activities are killed,
	// and be accessible from all threads and contexts.
	
	public static final boolean PULSE_CAMERA = true;
	WakeLock wakeLock;
	
	public Replay loadedReplay;	
	public boolean beep_on_gps;
	
	// Active Capture Data
	public EventSaver captureSaver;
	public ArrayList<PendingIntent> pendingIntents = new ArrayList<PendingIntent>();
	
	public void initializeCapture() throws FileNotFoundException {
		Log.w(Utils.TAG, "Initing new capture");
		
		if (loadedReplay != null) {
			if (captureSaver == null) {
				captureSaver = new EventSaver(loadedReplay.name, Utils.phoneId(this));
				
				insertPulse();
				
				captureSaver.addEvent("capture_started", null);
				
				long now = SystemClock.elapsedRealtime();
				
				for (int i = 0; i < loadedReplay.schedule.size(); ++i) {
					long delay = loadedReplay.schedule.get(i).get(0) - loadedReplay.startOffset();
					long captureFor = loadedReplay.schedule.get(i).get(1) - loadedReplay.schedule.get(i).get(0);
					
					long wakeup = now + delay;
					
					// Schedule GPS
					scheduleAlarm(AlarmReceiver.ACTION_GPS, wakeup, captureFor, i);
				}
				
				// Finish experiment
				scheduleAlarm(AlarmReceiver.ACTION_FINISH, now + loadedReplay.endOffset() - loadedReplay.startOffset(), -1, loadedReplay.schedule.size());				
			} else {
				Log.e(Utils.TAG, "Cannot INIT capture, a saver already exists");
				Utils.alarm(this);
			}
		} else {
			Log.e(Utils.TAG, "Cannot INIT capture, no loaded replay!");
			Utils.alarm(this);
		}
	}
	
	public void finishCapture() {		
		
		if (captureSaver != null) {
			Log.w(Utils.TAG, "Finishing capture");
			captureSaver.addEvent("capture_finished", null);
			insertPulse();
			
			captureSaver.close();
			captureSaver = null;
			
			if (pendingIntents == null) {
				Log.e(Utils.TAG, "Cannot FINISH capture because pending intents array does not exist!");
				Utils.alarm(this);
			} else {
				pendingIntents.clear();
			}
		} else {
			Log.e(Utils.TAG, "Cannot FINISH capture because saver does not exist");
			Utils.alarm(this);
		}
		Utils.alarm(this);
	}
	
	public void cancelCapture() {
		if (captureSaver != null) {
			Log.w(Utils.TAG, "Cancelling capture");
			captureSaver.addEvent("capture_cancelled", null);
			captureSaver.close();
			captureSaver = null;
			
			// Remove pending intents.
			if (pendingIntents != null) {
				AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
				for (PendingIntent pi : pendingIntents) {
					Log.w(Utils.TAG, "Cancelled " + pi);
					alarmManager.cancel(pi);
				}
				pendingIntents.clear();
			} else {
				Log.e(Utils.TAG, "Cannot CANCEL alarms because pendingintents array does not exist!");
			}
		} else {
			Log.e(Utils.TAG, "Cannot CANCEL capture because saver does not exist");
		}
	}
	
	void scheduleAlarm(String action, long wakeup, long captureFor, int counter) {
		AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		
		// Using counter as URI, so that intents would not be equal and override each other.
		String dummyAction = Intent.ACTION_RUN;
		Uri dummyData = Uri.parse("foo://" + counter);
		
		Intent intent = new Intent(dummyAction, dummyData, this, AlarmReceiver.class);
		intent.putExtra(AlarmReceiver.ACTION_KEY, action);
		intent.putExtra(AlarmReceiver.EXPECTED_TIME_KEY, wakeup);
		intent.putExtra(AlarmReceiver.CAPTURE_FOR_KEY, captureFor);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, AlarmReceiver.GPS_THREAD_WAKEUP_CODE, intent, 0);
		pendingIntents.add(pendingIntent);
		alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, wakeup, pendingIntent);
		Log.d(Utils.TAG, "Alarm set for " + wakeup + ", that is " + (wakeup - SystemClock.elapsedRealtime()) + " from now");
	}
	
	void insertPulse() {
		if (PULSE_CAMERA) {
			Camera camera = Camera.open();
			try {				
				Thread.sleep(5000);
				
				if (captureSaver != null)
					captureSaver.addEvent("spinstart", null);
				Parameters params = camera.getParameters();
				params.setFlashMode(Parameters.FLASH_MODE_TORCH);			
				camera.setParameters(params);						
				Thread.sleep(1000);						
				params = camera.getParameters();
				params.setFlashMode(Parameters.FLASH_MODE_OFF);			
				camera.setParameters(params);
				if (captureSaver != null)
					captureSaver.addEvent("spinend", null);
				
				Thread.sleep(1000);				
			} catch (InterruptedException e) {
				Log.e(Utils.TAG, "Interrupted while inserting pulse");
			} finally {
				camera.release();
				camera = null;
			}
		} else {
			throw new RuntimeException("CPU spin pulse not implemented");
		}
	}
	
	WakeLock getWakelock() {
		if (wakeLock == null) {
			wakeLock = ((PowerManager)getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Utils.TAG);
			wakeLock.setReferenceCounted(true);
		}
		return wakeLock;
	}
}
