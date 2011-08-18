package com.linnap.routereplay;

import java.io.FileNotFoundException;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.linnap.routereplay.capture.AlarmReceiver;
import com.linnap.routereplay.capture.CaptureProgress;
import com.linnap.routereplay.capture.GpsCaptureHelper;
import com.linnap.routereplay.replay.Replay;

public class ApplicationGlobals extends Application {
	// Some fields that must persist no matter which Activity is visible - including when all activities are killed,
	// and be accessible from all threads and contexts.
	
	public Replay loadedReplay;
	public CaptureProgress capture;
	private PendingIntent latestPendingWakeup;
	public GpsCaptureHelper sleepingCaptureHelper;
	
	public void initializeCapture() throws FileNotFoundException {
		Log.w(Utils.TAG, "Initing new capture");
		capture = new CaptureProgress(loadedReplay, Utils.phoneId(this));
	}
	
	public void killCapture() {
		Log.w(Utils.TAG, "Killing capture in progress");
		capture = null;
		if (latestPendingWakeup != null)
			((AlarmManager)this.getSystemService(Context.ALARM_SERVICE)).cancel(latestPendingWakeup);
		if (sleepingCaptureHelper != null)
			sleepingCaptureHelper.stopListening.run();
	}
	
	public void advanceCaptureAndScheduleNextWakeup() {
		if (capture != null) {
			++capture.nextScheduleIndex;
			if (capture.nextScheduleIndex < capture.replay.schedule.size()) {			
				// There are more scheduled periods. Set Broadcast alarm.
				long now = SystemClock.elapsedRealtime();
				long wakeup = capture.nextWakeupElapsedMillis();
				Log.d(Utils.TAG, "Now is " + now + ", setting wakeup for " + wakeup + " (" + (wakeup - now) + " millis delay)"); 
				Intent intent = new Intent(this, AlarmReceiver.class);
				latestPendingWakeup = PendingIntent.getBroadcast(this, AlarmReceiver.GPS_THREAD_WAKEUP_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
				((AlarmManager)this.getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.ELAPSED_REALTIME_WAKEUP, wakeup, latestPendingWakeup);
				Log.d(Utils.TAG, "Alarm set");
				// TODO: Do not go to sleep if the next wakeup is < 1 second away.
			} else {
				Log.d(Utils.TAG, "Experiment finished");
				capture.saver.addEvent("finished", null);
				capture.saver.close();
				
				Beeper beeper = new Beeper(this);
				try {
					beeper.beep();
					Thread.sleep(500);
					beeper.beep();
					Thread.sleep(500);
					beeper.beep();
					Thread.sleep(500);
					beeper.beep();
					Thread.sleep(500);
					beeper.beep();
				} catch (InterruptedException e) {
				}
			}
		} else {
			Log.d(Utils.TAG, "Cannot advance null capture, must have been killed.");
		}
	}
}
