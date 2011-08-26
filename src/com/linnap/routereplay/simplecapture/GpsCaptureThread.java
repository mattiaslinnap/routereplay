package com.linnap.routereplay.simplecapture;

import android.content.Context;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;

public class GpsCaptureThread extends Thread {

	public static final long GPS_DELAY_MILLIS = 1000;
	public static final float GPS_DISTANCE_METERS = 0.0f;
	
	Context context;
	Handler handler;
	LocationManager locationManager;

	public GpsCaptureThread(Context context) {
		this.context = context; 
	}
	
	public void run() {
		Looper.prepare();
		handler = new Handler();
		
		locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

        Looper.loop();
	}
	
}
