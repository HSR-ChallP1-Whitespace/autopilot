package ch.hsr.whitespace.javapilot.algorithms;

import ch.hsr.whitespace.javapilot.util.FloatingHistory;

public class MovingAverages {

	private static final int HISTORY_SIZE = 3;

	private FloatingHistory history;
	private boolean initialized = false;
	private int initCounter = 0;

	public MovingAverages() {
		history = new FloatingHistory(HISTORY_SIZE);
	}

	public void shift(double value) {
		if (!initialized) {
			initCounter++;
			initialized = initCounter >= HISTORY_SIZE;
		}
		history.shift(value);
	}

	public double currentMean() {
		return history.currentMean();
	}

	public double currentStDev() {
		return history.currentStDev();
	}

	public double meanDevFromZero() {
		return history.meanDevFromZero();
	}

	public boolean isCarStanding() {
		return history.currentStDev() < 3;
	}

	public boolean isHistoryInitialized() {
		return initialized;
	}

}
