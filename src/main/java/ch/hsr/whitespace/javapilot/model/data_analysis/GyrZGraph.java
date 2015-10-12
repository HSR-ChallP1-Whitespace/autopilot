package ch.hsr.whitespace.javapilot.model.data_analysis;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

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

	public void storeValue(long time, double value) {
		if (graphValues.isEmpty())
			setStartTime(time);
		getGraphValue4Time(getRelativeTime(time)).setValue(value);
	}

	public void storeValueSmoothed(long time, double value) {
		if (graphValues.isEmpty())
			setStartTime(time);
		getGraphValue4Time(getRelativeTime(time)).setValueSmoothed(value);
	}

	public void storeValueStdDev(long time, double value) {
		if (graphValues.isEmpty())
			setStartTime(time);
		getGraphValue4Time(getRelativeTime(time)).setValueStdDev(value);
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

	public static GyrZGraph instance() {
		if (INSTANCE == null)
			INSTANCE = new GyrZGraph();
		return INSTANCE;
	}

}
