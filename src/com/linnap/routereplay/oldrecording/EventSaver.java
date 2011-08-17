package com.linnap.routereplay.oldrecording;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.linnap.routereplay.Utils;

public class EventSaver {

	private static final String TAG = Utils.TAG;
	
	private String prefix;
	private String phoneId;
	private String filename;
	private FileOutputStream file;	
	
	public EventSaver(String prefix, String phoneId) throws FileNotFoundException {
		this.prefix = prefix;
		this.phoneId = phoneId;
		
		new File("/sdcard/routereplay").mkdirs();
		this.filename = "/sdcard/routereplay/" + prefix + "_" + phoneId + "_" + Utils.currentTimestampString();
		this.file = new FileOutputStream(this.filename);
	}	

	public synchronized void close() {
		try {
			file.close();
		} catch (IOException e) {
			Log.e(TAG, "Error closing file " + this.filename + ": " + e, e);
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
				Log.e(TAG, "File " + this.filename + " is already closed.");
			}
		} catch (JSONException e) {
			Log.e(TAG, "Error creating full JSON object. extras is of type " + extras.getClass().getName() + ". " + e, e);
		} catch (IOException e) {
			Log.e(TAG, "Error writing to file " + this.filename + ": " + e, e);
		}
	}
}

