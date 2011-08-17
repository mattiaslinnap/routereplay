package com.linnap.routereplay;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
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
	
	MapView mainMapView;
	MyLocationOverlay myLocation;
	ExpectedPositionOverlay expectedPosition;

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapmovement);
		
		replay = ChooseReplayActivity.getReplay();
		handler = new Handler();
		startClockMillis = 0;		
		
		mainMapView = (MapView)findViewById(R.id.mapview);
		mainMapView.setBuiltInZoomControls(true);
		
		myLocation = new MyLocationOverlay(this, mainMapView);
		mainMapView.getOverlays().add(myLocation);
		
		zoomToReplay();
		
		mainMapView.getOverlays().add(new ExpectedPathOverlay(replay));
		expectedPosition = new ExpectedPositionOverlay();
		mainMapView.getOverlays().add(expectedPosition);
	}

	Runnable updateTiming = new Runnable() {
		public void run() {
			long millis = millisSinceStart();
			if (millis == 0)
				return;
			
			expectedPosition.updateFix(findNextFix());
			((TextView)findViewById(R.id.replay_progress)).setText(Utils.formatDeltaMillisAsTime(millis) + " / " + Utils.formatDeltaMillisAsTime(replay.durationMillis()));
			mainMapView.invalidate();
			if (millis <= replay.durationMillis())
				handler.postDelayed(this, 1000);
			else
				((TextView)findViewById(R.id.replay_progress)).setText("Finished");
		}
	};

	long millisSinceStart() {
		if (startClockMillis == 0)
			return 0;
		else
			return SystemClock.elapsedRealtime() - startClockMillis;
	}
	

	Fix findNextFix() {
		if (startClockMillis == 0)
			return null;
		
		long searchStart = SystemClock.elapsedRealtime();
		int count = 0;
		try {
			long firstFixOffset = replay.fullgps.get(0).offset;
			for (Fix f : replay.fullgps) {
				if (f.offset - firstFixOffset >= millisSinceStart()) {
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

	
	public void onResume() {
		super.onResume();
		myLocation.enableMyLocation();
		
		expectedPosition.updateFix(null);
		if (startClockMillis == 0)
			startClockMillis = SystemClock.elapsedRealtime();		
		updateTiming.run();
	}
	
	public void onPause() {
		super.onPause();
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
