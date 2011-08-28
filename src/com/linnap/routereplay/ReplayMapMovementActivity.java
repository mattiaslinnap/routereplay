package com.linnap.routereplay;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.util.Pair;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.linnap.routereplay.replay.Fix;
import com.linnap.routereplay.replay.Replay;

public class ReplayMapMovementActivity extends MapActivity {

	Replay replay;
	Handler handler;
	long startClockMillis;
	
	WakeLock wakeLock;
	MapView mainMapView;
	MyLocationOverlay myLocation;
	ExpectedPositionOverlay expectedPosition;

	public static final long PERIOD_ACTIVE_NOW = -1;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapmovement);
		
		wakeLock = ((PowerManager)getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, Utils.TAG);
		wakeLock.setReferenceCounted(false);
				
		replay = ((ApplicationGlobals)getApplicationContext()).loadedReplay;
		handler = new Handler();
		startClockMillis = Utils.INVALID_FUTURE_TIME;	
		
		mainMapView = (MapView)findViewById(R.id.mapview);
		mainMapView.setBuiltInZoomControls(true);
		
		myLocation = new MyLocationOverlay(this, mainMapView);
		mainMapView.getOverlays().add(myLocation);
		
		zoomToReplay();
		
		mainMapView.getOverlays().add(new ExpectedPathOverlay(replay));
		expectedPosition = new ExpectedPositionOverlay();
		mainMapView.getOverlays().add(expectedPosition);
		mainMapView.invalidate();
	}

	Runnable updateTiming = new Runnable() {
		public void run() {
			Log.d(Utils.TAG, "Running update for movement map");
			long millis = millisSinceStart();
			if (millis == Utils.INVALID_FUTURE_TIME) {
				Log.d(Utils.TAG, "Update millis invalid, not actually updating. StartClock " + startClockMillis);
				return;
			}
			
			expectedPosition.updateFix(findNextFix());
			((TextView)findViewById(R.id.replay_progress)).setText(Utils.formatDeltaMillisAsTime(millis) + " / " + Utils.formatDeltaMillisAsTime(replay.durationMillis()));
			
			TextView periodInfo = (TextView)findViewById(R.id.replay_time_to_schedule);
			long toPeriod = millisToNextSchedulePeriod();
			if (toPeriod == Utils.INVALID_FUTURE_TIME)
				periodInfo.setText("Unknown");
			else if (toPeriod == PERIOD_ACTIVE_NOW)
				periodInfo.setText("NOW! NOW! NOW!");
			else
				periodInfo.setText(Utils.formatDeltaMillisAsTime(toPeriod));
			
			mainMapView.invalidate();
			if (millis <= replay.durationMillis())
				handler.postDelayed(this, 1000);
			else
				((TextView)findViewById(R.id.replay_progress)).setText("Finished");
		}
	};

	long millisSinceStart() {
		if (startClockMillis == Utils.INVALID_FUTURE_TIME)
			return Utils.INVALID_FUTURE_TIME;
		else
			return SystemClock.elapsedRealtime() - startClockMillis;
	}
	

	Fix findNextFix() {
		if (startClockMillis == Utils.INVALID_FUTURE_TIME)
			return null;
		
		long searchStart = SystemClock.elapsedRealtime();
		long sinceStart = millisSinceStart();
		int count = 0;
		try {
			long startOffset = replay.startOffset();
			for (Fix f : replay.fullgps) {
				if (f.offset - startOffset >= sinceStart) {
					// This is the first fix at or after current time.
					return f;
				}
				++count;
			}
			// Past the end of the replay.
			return null;
		} finally {
			//Log.d(Utils.TAG, "Using fix number " + count + " of " + replay.fullgps.size());
			//Log.d(Utils.TAG, "Next fix search took " + (SystemClock.elapsedRealtime() - searchStart) + " millis.");
		}
	}
	
	long millisToNextSchedulePeriod() {
		if (startClockMillis == Utils.INVALID_FUTURE_TIME)
			return Utils.INVALID_FUTURE_TIME;
		
		long sinceStart = millisSinceStart();
		long startOffset = replay.startOffset();
		
		long toNext = Utils.INVALID_FUTURE_TIME;
		for (List<Long> period : replay.schedule) {
			long pstart = period.get(0) - startOffset;
			long pend = period.get(1) - startOffset;
			
			if (pstart <= sinceStart && sinceStart <= pend)
				return PERIOD_ACTIVE_NOW;
			
			if (pstart > sinceStart) {
				toNext = pstart - sinceStart;
				break;
			}
		}
		return toNext;
	}

	
	public void onResume() {
		super.onResume();
		
		wakeLock.acquire();
		
		myLocation.enableMyLocation();
		
		expectedPosition.updateFix(null);
		if (startClockMillis == Utils.INVALID_FUTURE_TIME)
			startClockMillis = SystemClock.elapsedRealtime();		
		updateTiming.run();
	}
	
	public void onPause() {
		super.onPause();
		
		wakeLock.release();
		
		handler.removeCallbacks(updateTiming);
		expectedPosition.updateFix(null);
		myLocation.disableMyLocation();
	}
	
	protected boolean isRouteDisplayed() {
		return false;  // GPS traces are not routes
	}
	
	void zoomToReplay() {
		Pair<GeoPoint, GeoPoint> bounds = replay.getBounds();
		
		int latSpan = bounds.second.getLatitudeE6() - bounds.first.getLatitudeE6();
		int lngSpan = bounds.second.getLongitudeE6() - bounds.first.getLongitudeE6();
		
		Log.i(Utils.TAG, "" + bounds.first + " " + bounds.second);
		Log.i(Utils.TAG, "" + latSpan + " " + lngSpan);
		
		GeoPoint center = new GeoPoint((bounds.second.getLatitudeE6() + bounds.first.getLatitudeE6()) / 2,
				                       (bounds.second.getLongitudeE6() + bounds.first.getLongitudeE6()) / 2);
		mainMapView.getController().setCenter(center);
		mainMapView.getController().zoomToSpan(latSpan, lngSpan);
	}
}
