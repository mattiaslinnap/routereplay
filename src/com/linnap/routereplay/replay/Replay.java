package com.linnap.routereplay.replay;

import java.util.List;

import android.util.Pair;

import com.google.android.maps.GeoPoint;
import com.linnap.routereplay.Utils;

public class Replay {
	public static final int E6 = 1000000;
	
	public transient String name;
	public long epoch;
	public List<Fix> fullgps;
	public List<List<Long>> schedule;
	
	public long durationMillis() {
		return endOffset() - startOffset();
	}
	
	public long startOffset() {
		return Math.min(fullgps.get(0).offset, schedule.get(0).get(0));
	}
	
	public long startMillis() {
		return epoch + startOffset();
	}
	
	public long endOffset() {
		return Math.max(fullgps.get(fullgps.size() - 1).offset, schedule.get(schedule.size() - 1).get(1));
	}
	
	public Pair<GeoPoint, GeoPoint> getBounds() {
		double minLat = 200.0;
		double maxLat = -200.0;
		double minLng = 200.0;
		double maxLng = -200.0;
		
		for (Fix f : fullgps) {
			if (f.lat < minLat) minLat = f.lat;
			if (f.lat > maxLat) maxLat = f.lat;
			if (f.lat < minLng) minLng = f.lng;
			if (f.lat > maxLng) maxLng = f.lng;
		}
		
		return new Pair<GeoPoint, GeoPoint>(Utils.geopoint(minLat, minLng), Utils.geopoint(maxLat, maxLng));
	}
}
