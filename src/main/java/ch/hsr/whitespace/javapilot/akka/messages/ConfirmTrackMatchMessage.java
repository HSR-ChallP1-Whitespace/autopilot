package ch.hsr.whitespace.javapilot.akka.messages;

import ch.hsr.whitespace.javapilot.model.track.recognition.matching.PossibleTrackMatch;

public class ConfirmTrackMatchMessage {

	private PossibleTrackMatch confirmedMatch;

	public ConfirmTrackMatchMessage(PossibleTrackMatch match) {
		this.confirmedMatch = match;
	}

	public PossibleTrackMatch getConfirmedMatch() {
		return confirmedMatch;
	}

}
