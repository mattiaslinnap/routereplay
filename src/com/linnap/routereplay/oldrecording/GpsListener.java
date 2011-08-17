package com.linnap.routereplay.oldrecording;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.linnap.routereplay.Utils;

public class GpsListener implements LocationListener, GpsStatus.Listener {

	public static final String TAG = Utils.TAG;
	public static final long GPS_DELAY_MILLIS = 1000;
	public static final float GPS_DISTANCE_METERS = 0.0f;
	
	private EventSaver saver;
	private LocationManager locationManager;
	private WakeLock wakeLock;
	
	public GpsListener(EventSaver saver, Context context) {
		this.saver = saver;
		this.locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		this.wakeLock = ((PowerManager)context.getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		this.wakeLock.setReferenceCounted(false);
	}
	
	public void resume() {
		wakeLock.acquire();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_DELAY_MILLIS, GPS_DISTANCE_METERS, this);
		locationManager.addGpsStatusListener(this);
	}
	
	public void pause() {
		locationManager.removeUpdates(this);
		locationManager.removeGpsStatusListener(this);
		wakeLock.release();
	}
		
	@Override
	public void onLocationChanged(Location location) {
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
				Log.e(TAG, "JSONException", e);
			}
			saver.addEvent("location_changed", json);
		} else {
			saver.addEvent("location_changed", null);
		}
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
				// TODO: this is only a summary. Add full GPS satellite data!
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
			Log.e(TAG, "JSONException", e);
		}
		saver.addEvent("gpsstatus_changed", json);
	}

}
