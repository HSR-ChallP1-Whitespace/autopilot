package ch.hsr.whitespace.javapilot.util;

import java.util.List;

import ch.hsr.whitespace.javapilot.model.track.TrackPart;

public class StringUtil {

	public static String getPatternString(List<TrackPart> trackParts) {
		StringBuilder sb = new StringBuilder();
		for (TrackPart trackPart : trackParts) {
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
