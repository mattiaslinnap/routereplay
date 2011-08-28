package com.linnap.routereplay;

import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.linnap.routereplay.capture.EventSaver;
import com.linnap.routereplay.simplecapture.GpsHelper;
import com.linnap.routereplay.simplecapture.ScheduleTimerThread;

public class SimpleCaptureActivity extends Activity {
	
	ApplicationGlobals app;
	Handler handler;
	WakeLock partial;
	WakeLock screenDim;
	ArrayDeque<String> infoLines = new ArrayDeque<String>();
	
	EventSaver saver;
	GpsHelper helper;
	ScheduleTimerThread thread;
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.simplecapture);
		
		app = (ApplicationGlobals)getApplication();
		handler = new Handler();
		
		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		partial = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Utils.TAG);
		partial.setReferenceCounted(false);
		partial.acquire();
		screenDim = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, Utils.TAG);
		screenDim.setReferenceCounted(false);
		screenDim.acquire();
		
		startThread();
	}
	
	public void startThread() {
		if (app.loadedReplay == null) {
			seriousError("No loaded replay!");
			return;
		}
		try {
			saver = new EventSaver(app.loadedReplay.name, Utils.phoneId(this));
			helper = new GpsHelper(this, app, handler.getLooper(), saver); 
			thread = new ScheduleTimerThread(this, app.loadedReplay, helper, saver);
			thread.start();
		} catch (FileNotFoundException e) {
			seriousError("Cannot open saver, " + e);			
		}
	}
	
	public void threadFinished() {
		handler.post(new Runnable() {
			public void run() {
				saver.addEvent("finished", null);
				saver.close();
				seriousError("Experiment finished!");
				partial.release();
				screenDim.release();
			}
		});
	}
	
	public void seriousError(final String message) {
		final Context ctx = this;
		handler.post(new Runnable() {
			public void run() {
				Log.e(Utils.TAG, message);
				((TextView)findViewById(R.id.error)).setText(message);
				Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
				Utils.alarm(ctx);
			}
		});
	}
	
	public void infoMessage(final String message) {
		final Context ctx = this;
		handler.post(new Runnable() {
			public void run() {
				Log.i(Utils.TAG, message);
				
				infoLines.addLast(message);
				if (infoLines.size() > 15)
					infoLines.removeFirst();
				
				StringBuilder sb = new StringBuilder();
				for (String line : infoLines) {
					sb.append(line);
					sb.append("\n");
				} 
				((TextView)findViewById(R.id.info)).setText(sb.toString());
			}
		});
	}
}
