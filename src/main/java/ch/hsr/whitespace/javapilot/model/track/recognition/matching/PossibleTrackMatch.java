package ch.hsr.whitespace.javapilot.model.track.recognition.matching;

import java.util.ArrayList;
import java.util.List;

import ch.hsr.whitespace.javapilot.model.track.recognition.RecognitionTrackPart;

public class PossibleTrackMatch {

	private List<RecognitionTrackPart> parts;
	private long matchDuration;

	public PossibleTrackMatch(List<RecognitionTrackPart> parts) {
		this.parts = new ArrayList<>(parts);
		calculateMatchDuration();
	}

	private void calculateMatchDuration() {
		long tmpDuration = 0;
		for (RecognitionTrackPart part : parts) {
			tmpDuration = tmpDuration + part.getDuration();
		}
		matchDuration = tmpDuration;
	}

	public long getMatchDuration() {
		return matchDuration;
	}

	@Override
	public String toString() {
		return parts.toString();
	}

	public List<RecognitionTrackPart> getTrackParts() {
		return parts;
	}

}
