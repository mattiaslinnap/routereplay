package com.linnap.routereplay.capture;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.linnap.routereplay.Utils;

public class EventSaver {

	private String replayName;
	private String phoneId;
	private String filename;
	private FileOutputStream file;	
	
	public EventSaver(String replayName, String phoneId) throws FileNotFoundException {
		this.replayName = replayName;
		this.phoneId = phoneId;
				
		this.filename = new File(new File(Utils.DATA_DIR, replayName), "events_" + phoneId + "_" + Utils.currentTimestampString()).toString();
		this.file = new FileOutputStream(this.filename);
	}	

	public synchronized void close() {
		try {
			file.close();
		} catch (IOException e) {
			Log.e(Utils.TAG, "Error closing file " + this.filename + ": " + e, e);
		}
		file = null;
	}
	
	public synchronized void addEvent(String type, Object extras) {
		// If extras is null, the data field is not added.
		// If extras is JSONObject.NULL, the data field is "data": null.
		try {
			JSONObject json = new JSONObject();
			json.put("log_version", "2010-04-16");
			json.put("phoneid", phoneId);
			json.put("time_phone", System.currentTimeMillis());
			json.put("event", type);
			json.put("extras", extras);
			
			if (file != null) {
				// Note: with int argument json.toString prettyprints! Remove argument to save space.
				String data = json.toString(2) + "\0";
				byte[] bytes = data.getBytes("UTF-8");
				file.write(bytes);
				file.flush();
			} else {
				Log.e(Utils.TAG, "File " + this.filename + " is already closed.");
			}
		} catch (JSONException e) {
			Log.e(Utils.TAG, "Error creating full JSON object. extras is of type " + extras.getClass().getName() + ". " + e, e);
		} catch (IOException e) {
			Log.e(Utils.TAG, "Error writing to file " + this.filename + ": " + e, e);
		}
	}
}

