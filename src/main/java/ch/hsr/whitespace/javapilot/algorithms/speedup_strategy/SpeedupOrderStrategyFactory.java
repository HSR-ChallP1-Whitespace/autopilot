package ch.hsr.whitespace.javapilot.algorithms.speedup_strategy;

import java.util.List;

import ch.hsr.whitespace.javapilot.akka.DrivingCoordinatorActor;
import ch.hsr.whitespace.javapilot.algorithms.speedup_strategy.impl.SpeedupAllTogether;
import ch.hsr.whitespace.javapilot.algorithms.speedup_strategy.impl.SpeedupOneAfterOne;
import ch.hsr.whitespace.javapilot.model.track.TrackPart;

public class SpeedupOrderStrategyFactory {

	public enum SpeedupOrderStrategyType {
		ONE_AFTER_ONE, ALL_TOGETHER
	}

	private List<TrackPart> trackParts;
	private DrivingCoordinatorActor drivingCoordinator;

	public SpeedupOrderStrategyFactory(List<TrackPart> trackParts, DrivingCoordinatorActor drivingCoordinator) {
		this.trackParts = trackParts;
		this.drivingCoordinator = drivingCoordinator;
	}

	public SpeedupOrderStrategy createStrategy(SpeedupOrderStrategyType strategyType) {
		if (strategyType == SpeedupOrderStrategyType.ALL_TOGETHER)
			return new SpeedupAllTogether(trackParts, drivingCoordinator);

		// default strategy
		return new SpeedupOneAfterOne(trackParts, drivingCoordinator);
	}

}
