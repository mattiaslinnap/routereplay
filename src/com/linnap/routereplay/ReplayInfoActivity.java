package com.linnap.routereplay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
				Toast.makeText(ReplayInfoActivity.this, "TODO", Toast.LENGTH_SHORT).show();
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
