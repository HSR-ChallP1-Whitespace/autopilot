package ch.hsr.whitespace.javapilot.model.data.analysis;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.hsr.whitespace.javapilot.algorithms.MovingAverages;

public class GyrZGraph {

	private static final int MAX_STORED_VALUES = 2000;

	private Map<Long, GyrZGraphValue> graphValues;
	private long startTime;

	private GyrZGraph() {
		graphValues = new LinkedHashMap<Long, GyrZGraphValue>(MAX_STORED_VALUES) {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(java.util.Map.Entry<Long, GyrZGraphValue> eldest) {
				return size() > MAX_STORED_VALUES;
			}
		};
	}

	public void storeValues(long time, double currentValue, MovingAverages averages) {
		if (graphValues.isEmpty())
			setStartTime(time);
		GyrZGraphValue graphValue4Time = getGraphValue4Time(getRelativeTime(time));
		graphValue4Time.setValue(currentValue);
		graphValue4Time.setValueSmoothed(averages.currentMean());
		graphValue4Time.setValueStdDev(averages.currentStDev());
		graphValue4Time.setMeanDevFromZero(averages.meanDevFromZero());
	}

	private GyrZGraphValue getGraphValue4Time(long time) {
		if (!graphValues.containsKey(time))
			graphValues.put(time, new GyrZGraphValue(time));
		return graphValues.get(time);
	}

	public Collection<GyrZGraphValue> getData() {
		return graphValues.values();
	}

	public void reset() {
		graphValues.clear();
	}

	private void setStartTime(long time) {
		this.startTime = time;
	}

	private long getRelativeTime(long time) {
		return time - startTime;
	}

	private static GyrZGraph INSTANCE = null;

	public static GyrZGraph liveInstance() {
		if (INSTANCE == null)
			INSTANCE = new GyrZGraph();
		return INSTANCE;
	}

	public static GyrZGraph createInstance() {
		return new GyrZGraph();
	}

}
