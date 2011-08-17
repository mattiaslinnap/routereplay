package com.linnap.routereplay;

import android.app.Activity;
import android.os.Bundle;

import com.linnap.routereplay.replay.Replay;

public class ScheduledCaptureActivity extends Activity {
	
	Replay replay;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scheduledcapture);
        
        replay = ChooseReplayActivity.getReplay();
        
        
	}

}
