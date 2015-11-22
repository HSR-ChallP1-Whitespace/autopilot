package ch.hsr.whitespace.javapilot.util;

import java.util.List;

import ch.hsr.whitespace.javapilot.model.track.recognition.RecognitionTrackPart;

public class StringUtil {

	public static String getPatternString(List<RecognitionTrackPart> trackParts) {
		StringBuilder sb = new StringBuilder();
		for (RecognitionTrackPart trackPart : trackParts) {
			if ("".equals(sb.toString())) {
				sb.append(trackPart.getDirection().toShortString());
			} else {
				sb.append(" - ");
				sb.append(trackPart.getDirection().toShortString());
			}
		}
		return sb.toString();
	}

}
