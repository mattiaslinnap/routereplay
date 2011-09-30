package com.linnap.routereplay.simplecapture;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.linnap.routereplay.ApplicationGlobals;
import com.linnap.routereplay.Beeper;
import com.linnap.routereplay.Utils;
import com.linnap.routereplay.captureutil.EventSaver;

public class GpsHelper implements LocationListener {

	public static final long GPS_DELAY_MILLIS = 1000;
	public static final float GPS_DISTANCE_METERS = 0.0f;
	
	ApplicationGlobals app;
	Context context;
	Looper looper;
	EventSaver saver;
	LocationManager locationManager;
	Beeper beeper;
	volatile boolean gotFixes;
	
	public GpsHelper(Context context, ApplicationGlobals app, Looper looper, EventSaver saver) {
		this.context = context;
		this.app = app;
		this.looper = looper;
		this.saver = saver;
		this.locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		this.beeper = new Beeper(context);
	}
	
	public void start() {
		gotFixes = false;
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_DELAY_MILLIS, GPS_DISTANCE_METERS, this, looper);
	}
	
	public void stop() {
		locationManager.removeUpdates(this);
		gotFixes = false;
	}
	
	public void onLocationChanged(Location location) {
		Log.d(Utils.TAG, "Got location " + location);
		
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
		gotFixes = true;
		if (app.beep_on_gps)
			beeper.beep();
	}

	public void onProviderDisabled(String provider) {
		// Boring.
	}
	public void onProviderEnabled(String provider) {
		// Boring.
	}
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// Provider status is boring.
	}
}
