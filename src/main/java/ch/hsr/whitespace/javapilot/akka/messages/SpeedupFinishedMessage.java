package ch.hsr.whitespace.javapilot.akka.messages;

import ch.hsr.whitespace.javapilot.model.track.TrackPart;

public class SpeedupFinishedMessage {

	private TrackPart trackPart;

	public SpeedupFinishedMessage(TrackPart trackPart) {
		super();
		this.trackPart = trackPart;
	}

	public TrackPart getTrackPart() {
		return trackPart;
	}

	public void setTrackPart(TrackPart trackPart) {
		this.trackPart = trackPart;
	}

}
