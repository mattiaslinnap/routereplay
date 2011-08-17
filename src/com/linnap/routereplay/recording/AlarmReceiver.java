package com.linnap.routereplay.recording;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.linnap.routereplay.Utils;

public class AlarmReceiver extends BroadcastReceiver {
	
	public static final String TAG = Utils.TAG;
	
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "Got event");
		Log.i(TAG, "" + context);
		Log.i(TAG, "" + intent);
		Log.i(TAG, "" + intent.getAction());
		Log.i(TAG, "" + intent.getDataString());
		Log.i(TAG, "" + intent.getData());
		Log.i(TAG, "" + intent.getFlags());
		Log.i(TAG, "" + intent.getExtras());
		
		GpsSaverThread thread = new GpsSaverThread(context);
		thread.start();
	}
	
}