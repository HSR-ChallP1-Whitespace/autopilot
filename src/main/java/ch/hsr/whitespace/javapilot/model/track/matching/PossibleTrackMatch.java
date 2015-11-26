package ch.hsr.whitespace.javapilot.model.track.matching;

import java.util.ArrayList;
import java.util.List;

import ch.hsr.whitespace.javapilot.model.track.TrackPart;

public class PossibleTrackMatch {

	private List<TrackPart> parts;
	private long matchDuration;

	public PossibleTrackMatch(List<TrackPart> parts) {
		this.parts = new ArrayList<>(parts);
		calculateMatchDuration();
	}

	private void calculateMatchDuration() {
		long tmpDuration = 0;
		for (TrackPart part : parts) {
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

	public List<TrackPart> getTrackParts() {
		return parts;
	}

}
