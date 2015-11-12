package ch.hsr.whitespace.javapilot.akka.messages;

import ch.hsr.whitespace.javapilot.model.track.driving.DrivingTrackPart;

/**
 * Tells pilot that the position in the track has changed
 *
 */
public class PositionChangeMessage {

	private DrivingTrackPart part;
	private DrivingTrackPart nextPart;

	public PositionChangeMessage(DrivingTrackPart part, DrivingTrackPart nextPart) {
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
