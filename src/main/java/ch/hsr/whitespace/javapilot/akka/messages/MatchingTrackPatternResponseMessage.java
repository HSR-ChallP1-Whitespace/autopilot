package ch.hsr.whitespace.javapilot.akka.messages;

import ch.hsr.whitespace.javapilot.model.track.matching.PossibleTrackMatch;

public class MatchingTrackPatternResponseMessage {

	private boolean patternConfirmed = false;
	private PossibleTrackMatch confirmedMatch;

	public MatchingTrackPatternResponseMessage(PossibleTrackMatch match, boolean patternConfirmed) {
		this.patternConfirmed = patternConfirmed;
		this.confirmedMatch = match;
	}

	public PossibleTrackMatch getConfirmedMatch() {
		return confirmedMatch;
	}

	public boolean getPatternConfirmed() {
		return patternConfirmed;
	}

}
