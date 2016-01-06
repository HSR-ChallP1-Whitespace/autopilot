package ch.hsr.whitespace.javapilot.algorithms.speedup_strategy.impl;

import java.util.List;

import ch.hsr.whitespace.javapilot.akka.DrivingCoordinatorActor;
import ch.hsr.whitespace.javapilot.algorithms.speedup_strategy.SpeedupOrderStrategy;
import ch.hsr.whitespace.javapilot.model.track.TrackPart;

public abstract class AbstractSpeedupOrderStrategy implements SpeedupOrderStrategy {

	protected List<TrackPart> trackParts;
	protected DrivingCoordinatorActor drivingCoordinator;

	public AbstractSpeedupOrderStrategy(List<TrackPart> trackParts, DrivingCoordinatorActor drivingCoordinator) {
		this.trackParts = trackParts;
		this.drivingCoordinator = drivingCoordinator;
	}

	protected void speedupTrackPart(TrackPart trackPart) {
		drivingCoordinator.speedupTrackPartById(trackPart.getId());
	}

}
