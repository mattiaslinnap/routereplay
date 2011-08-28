package com.linnap.routereplay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.linnap.routereplay.replay.Replay;

public class ReplayInfoActivity extends Activity {
	
	Handler handler = new Handler();
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.replayinfo);
        
        attachUiEvents();
	}

	public void onResume() {
		super.onResume();
		updateCurrentTime.run();
		((ApplicationGlobals)getApplicationContext()).killCapture();
		Log.d(Utils.TAG, "ReplayInfo resuming");
	}
	
	public void onPause() {
		super.onPause();
		handler.removeCallbacks(updateCurrentTime);
		Log.d(Utils.TAG, "ReplayInfo pausing");
	}
	
	private void attachUiEvents() {
		final ApplicationGlobals app = (ApplicationGlobals)getApplicationContext(); 
		Replay r = app.loadedReplay;
		if (r == null) {
			Toast.makeText(this, "ERROR: replay is null", Toast.LENGTH_LONG).show();			
		}
		((TextView)findViewById(R.id.replay_name)).setText(r.name);
		((TextView)findViewById(R.id.replay_length)).setText(Utils.formatDeltaMillisAsTime(r.durationMillis()));
		((TextView)findViewById(R.id.replay_original_start)).setText("Original start: " + Utils.timestampFriendlyString(r.startMillis()));
		
		((Button)findViewById(R.id.start_replay)).setOnClickListener(new OnClickListener() {
			public void onClick(View view) {				
				Intent intent = new Intent(ReplayInfoActivity.this, ReplayMapMovementActivity.class);
				startActivity(intent);
			}
		});
		((Button)findViewById(R.id.start_schedule)).setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(ReplayInfoActivity.this, SimpleCaptureActivity.class);
				startActivity(intent);
			}
		});
		
		((CheckBox)findViewById(R.id.check_beep_on_gps)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton view, boolean checked) {
				app.beep_on_gps = checked;
			}
		});
	}
	
	Runnable updateCurrentTime = new Runnable() {
		public void run() {
			((TextView)findViewById(R.id.current_time)).setText("Current time: " + Utils.currentTimestampFriendlyString());
			handler.postDelayed(this, 1000);
		}
	};
	
}
