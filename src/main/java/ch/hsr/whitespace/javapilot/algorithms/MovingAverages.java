package ch.hsr.whitespace.javapilot.algorithms;

import ch.hsr.whitespace.javapilot.util.FloatingHistory;

public class MovingAverages {

	private FloatingHistory history;

	public MovingAverages() {
		history = new FloatingHistory(3);
	}

	public void shift(double value) {
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

}
