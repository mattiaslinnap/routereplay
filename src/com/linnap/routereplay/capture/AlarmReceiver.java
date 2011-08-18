package com.linnap.routereplay.capture;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.linnap.routereplay.Utils;

public class AlarmReceiver extends BroadcastReceiver {
	
	public static final int GPS_THREAD_WAKEUP_CODE = 1;
	
	public void onReceive(Context context, Intent intent) {
		Log.d(Utils.TAG, "Alarm received");
		context.startService(new Intent(context, GpsCaptureService.class));
	}
	
}