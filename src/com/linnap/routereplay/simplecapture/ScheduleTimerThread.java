package com.linnap.routereplay.simplecapture;

import java.util.List;

import android.os.SystemClock;
import android.util.Log;

import com.linnap.routereplay.SimpleCaptureActivity;
import com.linnap.routereplay.Utils;
import com.linnap.routereplay.capture.EventSaver;
import com.linnap.routereplay.replay.Replay;

public class ScheduleTimerThread extends Thread {
	
	SimpleCaptureActivity activity;
	Replay replay;
	GpsHelper gpsHelper;
	EventSaver saver;
	
	public ScheduleTimerThread(SimpleCaptureActivity activity, Replay replay, GpsHelper gpsHelper, EventSaver saver) {
		this.activity = activity;
		this.replay = replay;
		this.gpsHelper = gpsHelper;
		this.saver = saver;
	}
	
	public void run() {
		saver.addEvent("starting", null);
		long startElapsed = SystemClock.elapsedRealtime();
		long startOffset = replay.startOffset();
		
		for (List<Long> period : replay.schedule) {
			long periodStart = period.get(0);
			long periodEnd = period.get(1);
			
			long now = SystemClock.elapsedRealtime();
			long sinceStart = now - startElapsed; 
			long sleepFor = (periodStart - startOffset) - sinceStart;
			if (sleepFor < 0) {
				activity.seriousError("Cannot sleep for " + sleepFor);
				sleepFor = 0;
			}
			long collectFor = periodEnd - periodStart;			
			
			saver.addEvent("start_sleep", null);
			activity.infoMessage("Sleeping for " + sleepFor);
			sleepAlertInterrupt(sleepFor);	
			gpsHelper.start();
			activity.infoMessage("Collecting for " + collectFor);
			saver.addEvent("start_collect", null);
			sleepAlertInterrupt(collectFor);
			
			if (!gpsHelper.gotFixes) {
				long extraCollectStart = SystemClock.elapsedRealtime();
				while (!gpsHelper.gotFixes) {
					sleepAlertInterrupt(100);
				}
				activity.infoMessage("TTFF missed, extra collect for " + (SystemClock.elapsedRealtime() - extraCollectStart));
			}
			
			gpsHelper.stop();			
		}
		
		activity.threadFinished();
	}
	
	public void sleepAlertInterrupt(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			activity.seriousError("SimpleTimerThread sleep for " + millis + " interrupted");
		}
	}

}
