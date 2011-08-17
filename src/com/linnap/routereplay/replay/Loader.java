package com.linnap.routereplay.replay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.customgson.Gson;
import com.google.customgson.JsonParseException;
import com.linnap.routereplay.Utils;

public class Loader {
	
	@SuppressWarnings("serial")
	public static class LoaderException extends Exception {
		public LoaderException(String reason) {
			super(reason);
		}
	}
	
	public static Replay load(String replayName) throws LoaderException {
		try {
			Reader reader = new BufferedReader(new FileReader(new File(new File(Utils.DATA_DIR, replayName), "replay.json")));		
			Gson gson = new Gson();
			Replay replay = gson.fromJson(reader, Replay.class);
			String error = validationError(replay);
			if (error != null)
				throw new LoaderException("Replay error: " + error);
			fillTransientFields(replay, replayName);
			return replay;
		} catch (FileNotFoundException e) {
			throw new LoaderException("File not found");
		} catch (JsonParseException e) {
			throw new LoaderException("Cannot parse file");
		}
	}
	
	private static String validationError(Replay r) {
		if (r.epoch < 1230768000000L)
			return "Epoch too early";
		if (r.epoch > 1356998400000L)
			return "Epoch too late";
		
		if (r.fullgps == null || r.fullgps.size() <= 0)
			return "No full GPS";
		for (Fix f : r.fullgps) {
			if (f == null)
				return "Missing fix";
			if (!(50 < f.lat && f.lat < 60))
				return "Latitude outside expected bounds";
			if (!(-5 < f.lng && f.lng < 5))
				return "Longitude outside expected bounds";
			if (f.offset < 0)
				return "Negative fix offset";
			if (f.offset >= 21600000)
				return "Fix offset over 6 hours";
		}
		
		if (r.schedule == null || r.schedule.size() <= 0)
			return "No schedule";
		for (List<Long> onoff : r.schedule) {
			if (onoff == null)
				return "Invalid onoff";
			if (onoff.size() != 2)
				return "Onoff with " + onoff.size() + " values";
			for (long val : onoff) {
				if (val < 0)
					return "Negative schedule offset";
				if (val >= 21600000)
					return "Schedule offset over 6 hours";
			}
		}
		
		return null;
	}
	
	private static void fillTransientFields(Replay replay, String replayName) {
		replay.name = replayName;
		
		for (Fix f : replay.fullgps) {
			f.geoPoint = Utils.geopoint(f.lat, f.lng);
		}
	}
}
