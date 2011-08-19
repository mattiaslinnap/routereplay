package com.linnap.routereplay.capture;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.linnap.routereplay.Utils;

public class GpsCaptureService extends Service {
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.w(Utils.TAG, "Service started");
		GpsCaptureHelper helper = new GpsCaptureHelper(this);
		helper.startListening();
		ProcessKiller.killUselessBackgroundServices(this);
		return START_NOT_STICKY;
	}
	
	public void onDestroy() {
		super.onDestroy();
		Log.w(Utils.TAG, "Service destroyed");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;  // No binding
	}

}
