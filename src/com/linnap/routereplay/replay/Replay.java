package com.linnap.routereplay.replay;

import java.util.List;

public class Replay {
	public transient String name;
	public long epoch;
	public List<Fix> fullgps;
	public List<List<Long>> schedule;
	
	public long durationMillis() {
		return fullgps.get(fullgps.size() - 1).offset - fullgps.get(0).offset;
	}
	
	public long startMillis() {
		return epoch + fullgps.get(0).offset;
	}
}
