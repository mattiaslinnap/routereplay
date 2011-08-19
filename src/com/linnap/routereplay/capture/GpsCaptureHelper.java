package com.linnap.routereplay.capture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.linnap.routereplay.ApplicationGlobals;
import com.linnap.routereplay.Beeper;
import com.linnap.routereplay.Utils;

public class GpsCaptureHelper implements LocationListener, GpsStatus.Listener {

	public static final boolean BEEP_ON_GPS = true;
	public static final long GPS_DELAY_MILLIS = 1000;
	public static final float GPS_DISTANCE_METERS = 0.0f;
	
	Handler handler;
	Service service;
	ApplicationGlobals app;
	Beeper beeper;
	WakeLock partial;
	LocationManager locationManager;
	volatile boolean locationReceived;
	volatile long extraSleepStart = Utils.INVALID_FUTURE_TIME;
	
	public GpsCaptureHelper(Service service) {
		this.handler = new Handler();
		this.service = service;
		this.app = (ApplicationGlobals)service.getApplicationContext();
	}
	
	public void startListening() {
		if (this.app.capture == null) {
			Log.d(Utils.TAG, "Capture is null, not starting listening");
			return;
		}
		
		// Acquire WakeLock
		partial = ((PowerManager)service.getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Utils.TAG);
		partial.setReferenceCounted(false);
		partial.acquire();
		
		// Start GPS
		locationManager = (LocationManager)service.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_DELAY_MILLIS, GPS_DISTANCE_METERS, this);
		locationManager.addGpsStatusListener(this);
		
		// Schedule stop
		long sleep = this.app.capture.currentPeriodGpsCollectMillis();
		Log.d(Utils.TAG, "Listening going to sleep for " + sleep);
		handler.postDelayed(stopListening, sleep);
		Log.d(Utils.TAG, "Listening going to sleep");
		app.sleepingCaptureHelper = this;
	}
	
	public void waitUntilLocationReceived() {
	}
	
	public Runnable stopListening = new Runnable() {
		public void run() {
			if (!locationReceived && app.capture != null) {
				extraSleepStart = SystemClock.elapsedRealtime();
				handler.postDelayed(this, 100);
				return;
			} else {
				if (extraSleepStart != Utils.INVALID_FUTURE_TIME) {
					long extraSleep = SystemClock.elapsedRealtime() - extraSleepStart;
					Log.w(Utils.TAG, "Extra sleep for " + extraSleep);
					if (app.capture != null) {
						JSONObject json = new JSONObject();
						try {
							json.put("extra_delay", extraSleep);
						} catch (JSONException e) {
							Log.e(Utils.TAG, "JSONException", e);
						}
						app.capture.saver.addEvent("schedule_missed", json);
					}
				}
				
				app.sleepingCaptureHelper = null;
				handler.removeCallbacks(this);
				Log.d(Utils.TAG, "Listening woke up");
				
				// Stop GPS
				locationManager.removeUpdates(GpsCaptureHelper.this);
				locationManager.removeGpsStatusListener(GpsCaptureHelper.this);
				
				// Stop Service
				service.stopSelf();
				
				// Schedule next steps
				app.advanceCaptureAndScheduleNextWakeup();
				
				// Release WakeLock
				partial.release();		
				Log.d(Utils.TAG, "Stopping listening");
			}
		}
	};
	
	public void maybeBeep() {
		if (BEEP_ON_GPS) {
			if (beeper == null)
				beeper = new Beeper(service);
			beeper.beep();
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(Utils.TAG, "Got location " + location);
		if (location != null && app.capture != null) {
			JSONObject json = new JSONObject();
			try {
				json.put("latitude", location.getLatitude());
				json.put("longitude", location.getLongitude());
				json.put("time", location.getTime());
				json.put("provider", location.getProvider());
				if (location.hasAccuracy()) json.put("accuracy", location.getAccuracy());
				if (location.hasAltitude()) json.put("altitude", location.getAltitude());
				if (location.hasBearing()) json.put("bearing", location.getBearing());
				if (location.hasSpeed()) json.put("speed", location.getSpeed());
				json.put("extras", Utils.bundleJson(location.getExtras())); // Omitted if extras == null
			} catch (JSONException e) {
				Log.e(Utils.TAG, "JSONException", e);
			}
			app.capture.saver.addEvent("location_changed", json);
		} else {
			app.capture.saver.addEvent("location_changed", null);
		}
		locationReceived = true;
		maybeBeep();
	}

	@Override
	public void onProviderDisabled(String provider) {
		// Boring.
	}

	@Override
	public void onProviderEnabled(String provider) {
		// Boring.
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// Provider status is boring.
	}

	@Override
	public void onGpsStatusChanged(int event) {
		Log.d(Utils.TAG, "Got gps status " + event);
		JSONObject json = new JSONObject();
		try {
			switch (event) {
				case GpsStatus.GPS_EVENT_FIRST_FIX: json.put("gpsevent", "first_fix"); break;
				case GpsStatus.GPS_EVENT_SATELLITE_STATUS: json.put("gpsevent", "satellite_status"); break;
				case GpsStatus.GPS_EVENT_STARTED: json.put("gpsevent", "started"); break;
				case GpsStatus.GPS_EVENT_STOPPED: json.put("gpsevent", "stopped"); break;
				default: json.put("gpsevent", event);
			}
			GpsStatus status = locationManager.getGpsStatus(null);
			if (status != null) {
				JSONObject statj = new JSONObject();
				statj.put("timetofirstfix", status.getTimeToFirstFix());				
				JSONArray birds = new JSONArray();				
				for (GpsSatellite sat : status.getSatellites()) {
					JSONObject birdj = new JSONObject();					
					birdj.put("azimuth", sat.getAzimuth());
					birdj.put("elevation", sat.getElevation());
					birdj.put("prn", sat.getPrn());
					birdj.put("snr", sat.getSnr());
					birdj.put("almanac", sat.hasAlmanac());
					birdj.put("ephermis", sat.hasEphemeris());
					birdj.put("usedinfix", sat.usedInFix());
					birds.put(birdj);
				}				
				statj.put("satellites", birds);
				json.put("gpsstatus", statj);
			}
		} catch (JSONException e) {
			Log.e(Utils.TAG, "JSONException", e);
		}
		if (app.capture != null)
			app.capture.saver.addEvent("gpsstatus_changed", json);
	}
}
