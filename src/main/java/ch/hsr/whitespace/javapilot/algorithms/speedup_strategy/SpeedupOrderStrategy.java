package ch.hsr.whitespace.javapilot.algorithms.speedup_strategy;

import ch.hsr.whitespace.javapilot.model.track.TrackPart;

public interface SpeedupOrderStrategy {

	/**
	 * Called when track recognition is finished, and speedup can start
	 */
	public void startSpeedup();

	/**
	 * Called if one of the trackparts finished he's speedup
	 * 
	 * @param trackPart
	 *            which finished the speedup
	 */
	public void speedupFinished(TrackPart trackPart);

}
