package ch.hsr.whitespace.javapilot.model.track;

import java.util.ArrayList;
import java.util.List;

public class Track {

	private List<TrackPart> parts;

	public Track() {
		parts = new ArrayList<>();
	}

	public List<TrackPart> getParts() {
		return parts;
	}

	public void addPart(TrackPart part) {
		this.parts.add(part);
	}

}
