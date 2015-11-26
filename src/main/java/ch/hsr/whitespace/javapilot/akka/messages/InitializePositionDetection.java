package ch.hsr.whitespace.javapilot.akka.messages;

import java.util.List;

import ch.hsr.whitespace.javapilot.model.track.TrackPart;

public class InitializePositionDetection {

	private List<TrackPart> trackParts;

	public InitializePositionDetection(List<TrackPart> trackParts) {
		this.trackParts = trackParts;
	}

	public List<TrackPart> getTrackParts() {
		return trackParts;
	}

}
