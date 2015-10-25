package ch.hsr.whitespace.javapilot.akka.messages;

import ch.hsr.whitespace.javapilot.model.track.driving.DrivingTrackPart;

public class DirectionChanged {

	private DrivingTrackPart part;
	private DrivingTrackPart nextPart;

	public DirectionChanged(DrivingTrackPart part, DrivingTrackPart nextPart) {
		this.part = part;
		this.nextPart = nextPart;
	}

	public DrivingTrackPart getTrackPart() {
		return part;
	}

	public DrivingTrackPart getNextTrackPart() {
		return nextPart;
	}

}
