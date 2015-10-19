package ch.hsr.whitespace.javapilot.akka.messages;

import java.util.List;

import ch.hsr.whitespace.javapilot.model.track.TrackPart;

public class TrackRecognitionFinished {

	public TrackRecognitionFinished(List<TrackPart> trackParts) {
		this.trackParts = trackParts;
	}

	private List<TrackPart> trackParts;

	public List<TrackPart> getTrackParts() {
		return trackParts;
	}
}