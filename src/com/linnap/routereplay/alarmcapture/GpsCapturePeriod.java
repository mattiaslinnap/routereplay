package com.linnap.routereplay.alarmcapture;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.linnap.routereplay.Utils;
import com.linnap.routereplay.captureutil.EventSaver;
import com.linnap.routereplay.captureutil.ProcessKiller;

public class GpsCapturePeriod implements LocationListener {

	boolean KILL_PROCESSES = false;
	long GPS_FORCE_STOP_DELAY = 20*1000; // GPS gets force stopped after this many milliseconds after the end of the schedule.
	
	Context context;
	LocationManager locationManager;
	Handler handler = new Handler();
	
	long captureFor;
	EventSaver saver;
	
	volatile boolean timerExpired = false;
	volatile boolean locationReceived = false;
	volatile long listenStartTime;
	
	OnCapturePeriodFinished onCapturePeriodFinished;
	OnWaitingForGpsForWayTooLong onWaitingForGpsForWayTooLong;
	WakeLock wakeLock;
	
	public GpsCapturePeriod(Context context, long captureFor, EventSaver saver) {
		this.context = context;
		this.captureFor = captureFor;
		this.saver = saver;
		
		locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		wakeLock = ((PowerManager)context.getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Utils.TAG);
		wakeLock.setReferenceCounted(false);
		wakeLock.acquire();
		
		// Start waiting
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0.0f, this);
		listenStartTime = SystemClock.elapsedRealtime();
		(onCapturePeriodFinished = new OnCapturePeriodFinished()).start();
		(onWaitingForGpsForWayTooLong = new OnWaitingForGpsForWayTooLong()).start();
		saver.addEvent("gps_start", null);
		
		if (KILL_PROCESSES) {
			ProcessKiller.killUselessBackgroundServices(context);
		}
	}

	class OnCapturePeriodFinished extends Thread {
		public void run() {
			try {
				Thread.sleep(captureFor);

				if (locationReceived) {
					// Have received locations in the period, TTFF model OK. Can stop.
					stopEverything("all_ok");
				} else {
					// No locations yet. TTFF MODEL INVALID!
					// Do not stop GPS yet, wait for the first one to arrive and stop it then.
					saver.addEvent("schedule_will_miss", null);
				}
				timerExpired = true;
			} catch (InterruptedException e) {
				Log.w(Utils.TAG, "OnCapturePeriodFinished interrupted");
			}
		}
	}
	
	class OnWaitingForGpsForWayTooLong extends Thread {
		public void run() {
			try {
				Thread.sleep(captureFor + GPS_FORCE_STOP_DELAY);

				if (!locationReceived) {			
					stopEverything("way_too_long");
					Utils.alarm(context);
				}
			} catch (InterruptedException e) {
				Log.w(Utils.TAG, "OnWaitingForGpsWayTooLong interrupted");
			}
		}
	}
	
	public void stopEverything(String reason) {
		locationManager.removeUpdates(this);
		saver.addEvent("gps_stop_" + reason, null);
		onCapturePeriodFinished.interrupt();
		onWaitingForGpsForWayTooLong.interrupt();
		wakeLock.release();
	}
	
	public void onLocationChanged(Location location) {
		saveLocationFix(location);
		
		if (!timerExpired) {
			if (!locationReceived) {
				// This was the first fix. Arrived OK.
				saveTtff();
			}
			// Still more time to wait for fixes.			
			// Keep listening.
			// Nothing actually to do.
		} else {
			if (locationReceived) {
				// Why am I still getting fixes? Might be a delay in stopping the GPS.
				saver.addEvent("fix_after_stopping", null);
			} else {
				// TTFF model invalid, and this is the first fix.
				// Stop listening.
				
				// Log actual TTFF
				saveTtff();
				
				stopEverything("schedule_missed");
			}
		}
		locationReceived = true;
	}

	void saveLocationFix(Location location) {
		if (location != null) {
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
			saver.addEvent("location_changed", json);
		} else {
			saver.addEvent("location_changed", null);
		}
	}
	
	void saveTtff() {
		long ttff = SystemClock.elapsedRealtime() - listenStartTime;		
		
		JSONObject json = new JSONObject();
		try {
			json.put("expected_ttff", captureFor);
			json.put("actual_ttff", ttff);
			
			if (ttff > captureFor) {
				Log.w(Utils.TAG, "Expected TTFF was " + captureFor + ", but actual " + ttff);
				json.put("schedule_missed", true);
			} else {
				json.put("schedule_ok", true);
			}
			
		} catch (JSONException e) {
			Log.e(Utils.TAG, "JSONException", e);
		}
		saver.addEvent("ttff", json);
	}
	
	public void onProviderDisabled(String provider) {}
	public void onProviderEnabled(String provider) {}
	public void onStatusChanged(String provider, int status, Bundle extras) {}	
}
