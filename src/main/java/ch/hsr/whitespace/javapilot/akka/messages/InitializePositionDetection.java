package ch.hsr.whitespace.javapilot.akka.messages;

import java.util.List;

import ch.hsr.whitespace.javapilot.model.track.driving.DrivingTrackPart;

public class InitializePositionDetection {

	private List<DrivingTrackPart> trackParts;

	public InitializePositionDetection(List<DrivingTrackPart> trackParts) {
		this.trackParts = trackParts;
	}

	public List<DrivingTrackPart> getTrackParts() {
		return trackParts;
	}

}
