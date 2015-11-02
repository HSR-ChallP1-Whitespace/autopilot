package ch.hsr.whitespace.javapilot.model.track.driving;

public class WrongTrackPartException extends Exception {

	public WrongTrackPartException() {
		super("TrackRecognizer has wrong trackpart");
	}

}
