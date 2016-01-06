package ch.hsr.whitespace.javapilot.algorithms.speedup_strategy.impl;

import java.util.Iterator;
import java.util.List;

import ch.hsr.whitespace.javapilot.akka.DrivingCoordinatorActor;
import ch.hsr.whitespace.javapilot.model.track.TrackPart;

public class SpeedupOneAfterOne extends AbstractSpeedupOrderStrategy {

	private Iterator<TrackPart> trackPartsIterator;

	public SpeedupOneAfterOne(List<TrackPart> trackParts, DrivingCoordinatorActor drivingCoordinator) {
		super(trackParts, drivingCoordinator);
		this.trackPartsIterator = this.trackParts.iterator();
	}

	@Override
	public void startSpeedup() {
		speedupNextTrackPart();
	}

	@Override
	public void speedupFinished(TrackPart trackPart) {
		speedupNextTrackPart();
	}

	private void speedupNextTrackPart() {
		if (trackPartsIterator.hasNext())
			speedupTrackPart(trackPartsIterator.next());
	}

}
