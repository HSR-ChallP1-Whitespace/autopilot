package ch.hsr.whitespace.javapilot.model.data_analysis;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class GyrZGraph {

	private Map<Long, GyrZGraphValue> graphValues;

	private GyrZGraph() {
		graphValues = new TreeMap<>();
	}

	public void storeValue(long time, double value) {
		getGraphValue4Time(time).setValue(value);
	}

	public void storeValueSmoothed(long time, double value) {
		getGraphValue4Time(time).setValueSmoothed(value);
	}

	private GyrZGraphValue getGraphValue4Time(long time) {
		if (!graphValues.containsKey(time))
			graphValues.put(time, new GyrZGraphValue(time));
		return graphValues.get(time);
	}

	public Collection<GyrZGraphValue> getData() {
		return graphValues.values();
	}

	private static GyrZGraph INSTANCE = null;

	public static GyrZGraph instance() {
		if (INSTANCE == null)
			INSTANCE = new GyrZGraph();
		return INSTANCE;
	}

}
