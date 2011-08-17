package com.linnap.routereplay;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class Beeper {
	
	public static final String TAG = Utils.TAG;
	
	private MediaPlayer player;
	
	public Beeper(Context context) {
		player = MediaPlayer.create(context, R.raw.beep2);
	}
	
	public void beep() {
		if (player != null)
			player.start();
	}
	
	public void close() {
		player.stop();
		player.release();
		player = null;
	}
}
