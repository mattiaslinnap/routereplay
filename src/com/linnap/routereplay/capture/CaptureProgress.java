package com.linnap.routereplay.capture;

import java.io.FileNotFoundException;
import java.util.List;

import android.os.SystemClock;

import com.linnap.routereplay.replay.Replay;

public class CaptureProgress {
	
	public Replay replay;
	public int nextScheduleIndex;	
	long captureStartElapsedMillis;
	public EventSaver saver;
	
	public CaptureProgress(Replay replay, String phoneId) throws FileNotFoundException {
		this.replay = replay;
		this.nextScheduleIndex = -1;
		this.captureStartElapsedMillis = SystemClock.elapsedRealtime();
		this.saver = new EventSaver(replay.name, phoneId);
	}
	
	public long millisSinceStart() {
		return SystemClock.elapsedRealtime() - captureStartElapsedMillis;
	}
	
	public long currentPeriodGpsCollectMillis() {
		List<Long> period = replay.schedule.get(nextScheduleIndex);
		return period.get(1) - period.get(0);
	}
	
	public long nextWakeupElapsedMillis() {
		return captureStartElapsedMillis + (replay.schedule.get(nextScheduleIndex).get(0) - replay.startOffset());
	}
}
