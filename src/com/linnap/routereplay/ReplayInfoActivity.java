package com.linnap.routereplay;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import com.linnap.routereplay.replay.Replay;

public class ReplayInfoActivity extends Activity {
	
	Handler handler = new Handler();
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.replayinfo);
        
        attachUiEvents();
	}

	protected void onResume() {
		super.onResume();		
		updateCurrentTime.run();
	}
	
	protected void onPause() {
		super.onPause();		
		handler.removeCallbacks(updateCurrentTime);
	}

	private void attachUiEvents() {
		Replay r = ChooseReplayActivity.getReplay();
		if (r == null) {
			Toast.makeText(this, "ERROR: replay is null", Toast.LENGTH_LONG);
		}
		((TextView)findViewById(R.id.replay_name)).setText(r.name);
		((TextView)findViewById(R.id.replay_length)).setText(formatDeltaMillisAsTime(r.durationMillis()));
		((TextView)findViewById(R.id.replay_original_start)).setText("Original start: " + Utils.timestampFriendlyString(r.startMillis()));
	}
	
	private String formatDeltaMillisAsTime(long millis) {
		long seconds = (millis / 1000) % 60;
		long minutes = (millis / 60000) % 60;
		long hours = (millis / 3600000);
		
		return String.format("%d h %02d min %02d sec", hours, minutes, seconds);
	}
	
	Runnable updateCurrentTime = new Runnable() {
		public void run() {
			((TextView)findViewById(R.id.current_time)).setText("Current time: " + Utils.currentTimestampFriendlyString());
			handler.postDelayed(this, 1000);
		}
	};
	
}
