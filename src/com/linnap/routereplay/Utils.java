package com.linnap.routereplay;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class Utils {
	
	public static final String TAG = "RouteReplay";
	
	public static final long INVALID_FUTURE_TIME = 100L*365*24*3600*1000; // 100 years. Guaranteed to be in the future from 1970.
	
	public static final File DATA_DIR = new File("/sdcard/replays");
	
	public static SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss.SSS");
	public static SimpleDateFormat timestampFriendlyFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	// This returns time string in the current Locale, including DST!
	// android_time is always UTC.
	public static String timestampString(long android_time) {
		Date date = new Date(android_time);
		return timestampFormat.format(date);
	}
	
	public static String currentTimestampString() {
		return timestampString(System.currentTimeMillis());
	}
	
	public static String timestampFriendlyString(long android_time) {
		Date date = new Date(android_time);
		return timestampFriendlyFormat.format(date);
	}
	
	public static String currentTimestampFriendlyString() {
		return timestampFriendlyString(System.currentTimeMillis());
	}
	
	public static String formatDeltaMillisAsTime(long millis) {
		long seconds = (millis / 1000) % 60;
		long minutes = (millis / 60000) % 60;
		long hours = (millis / 3600000);
		
		if (hours > 0)
			return String.format("%d h %02d min %02d sec", hours, minutes, seconds);
		else
			return String.format("%02d min %02d sec", minutes, seconds);
	}
	
	public static int airplaneMode(Context context) {
		try {
			return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON);
		} catch (SettingNotFoundException e) {
			return -1;
        }
	}
	
	public static JSONObject bundleJson(Bundle bundle) throws JSONException {
		if (bundle == null)
			return null; // Real nulls are omitted from JSON objects.
		else {
			JSONObject json = new JSONObject();
			for (String key : bundle.keySet()) {
				json.put(key, bundle.get(key));
			}
			return json;
		}
	}
	
	public static GeoPoint geopoint(double lat, double lng) {
		return new GeoPoint((int)(lat * 1E6), (int)(lng * 1E6));
	}
	
	public static String phoneId(Context context) {
		return ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
	}
	
	public static void sleepLogInterrupt(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Log.e(TAG, "Sleep of " + millis + " millis interrupted!", e);
		}
	}
}
