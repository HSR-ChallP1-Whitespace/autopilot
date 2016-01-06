package ch.hsr.whitespace.javapilot.algorithms.speedup_strategy.impl;

import java.util.List;

import ch.hsr.whitespace.javapilot.akka.DrivingCoordinatorActor;
import ch.hsr.whitespace.javapilot.model.track.TrackPart;

public class SpeedupAllTogether extends AbstractSpeedupOrderStrategy {

	public SpeedupAllTogether(List<TrackPart> trackParts, DrivingCoordinatorActor drivingCoordinator) {
		super(trackParts, drivingCoordinator);
	}

	@Override
	public void startSpeedup() {
		// just speedup all together
		for (TrackPart trackPart : this.trackParts) {
			speedupTrackPart(trackPart);
		}
	}

	@Override
	public void speedupFinished(TrackPart trackPart) {
		// nothing to do in this case
	}

}
