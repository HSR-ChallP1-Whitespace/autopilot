package ch.hsr.whitespace.javapilot.model.data_analysis;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class RoundTimeGraph {

	private int round = 0;
	private Map<Integer, RoundTimeValue> roundTimeMap;

	private RoundTimeGraph() {
		roundTimeMap = new TreeMap<>();
	}

	public void storeRoundTime(long roundTime) {
		if (round == 0) {
			round++;
		} else {
			roundTimeMap.put(round, new RoundTimeValue(round, roundTime));
			round++;
		}
	}

	public Collection<RoundTimeValue> getData() {
		return roundTimeMap.values();
	}

	public void reset() {
		roundTimeMap.clear();
	}

	private static RoundTimeGraph INSTANCE = null;

	public static RoundTimeGraph instance() {
		if (INSTANCE == null)
			INSTANCE = new RoundTimeGraph();
		return INSTANCE;
	}
}
