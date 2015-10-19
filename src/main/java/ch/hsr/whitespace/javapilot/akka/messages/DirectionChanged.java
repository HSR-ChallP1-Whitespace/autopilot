package ch.hsr.whitespace.javapilot.akka.messages;

import ch.hsr.whitespace.javapilot.model.track.TrackPart;

public class DirectionChanged {

	private TrackPart part;
	private TrackPart nextPart;

	public DirectionChanged(TrackPart part, TrackPart nextPart) {
		this.part = part;
		this.nextPart = nextPart;
	}

	public TrackPart getTrackPart() {
		return part;
	}

	public TrackPart getNextTrackPart() {
		return nextPart;
	}

}
