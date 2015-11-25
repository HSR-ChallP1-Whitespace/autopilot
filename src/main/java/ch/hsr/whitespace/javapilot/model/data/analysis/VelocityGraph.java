package ch.hsr.whitespace.javapilot.model.data.analysis;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class VelocityGraph {

	private Map<Long, VelocityValue> velocityMap;

	private VelocityGraph() {
		velocityMap = new TreeMap<>();
	}

	public void storeVelocity(long timestamp, double velocity) {
		velocityMap.put(timestamp, new VelocityValue(timestamp, velocity));
	}

	public Collection<VelocityValue> getData() {
		return velocityMap.values();
	}

	public void reset() {
		velocityMap.clear();
	}

	private static VelocityGraph INSTANCE = null;

	public static VelocityGraph liveInstance() {
		if (INSTANCE == null)
			INSTANCE = new VelocityGraph();
		return INSTANCE;
	}

	public static VelocityGraph createInstance() {
		return new VelocityGraph();
	}
}
