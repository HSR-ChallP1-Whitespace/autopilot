package ch.hsr.whitespace.javapilot.model.track.recognition;

import java.util.ArrayList;
import java.util.List;

public class RecognitionTrack {

	private List<RecognitionTrackPart> parts;

	public RecognitionTrack() {
		parts = new ArrayList<>();
	}

	public List<RecognitionTrackPart> getParts() {
		return parts;
	}

	public void addPart(RecognitionTrackPart part) {
		this.parts.add(part);
	}

}
