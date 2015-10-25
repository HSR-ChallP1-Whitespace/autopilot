package ch.hsr.whitespace.javapilot.akka.messages;

import java.util.List;

import ch.hsr.whitespace.javapilot.model.track.recognition.RecognitionTrackPart;

public class TrackRecognitionFinished {

	public TrackRecognitionFinished(List<RecognitionTrackPart> trackParts) {
		this.trackParts = trackParts;
	}

	private List<RecognitionTrackPart> trackParts;

	public List<RecognitionTrackPart> getTrackParts() {
		return trackParts;
	}
}