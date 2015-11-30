package ch.hsr.whitespace.javapilot.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import ch.hsr.whitespace.javapilot.model.track.Direction;
import ch.hsr.whitespace.javapilot.model.track.TrackPart;

public class TrackPartUtil {

	public static List<TrackPart> getStraightPartsByDuration(Collection<TrackPart> trackParts) {
		return trackParts.stream().filter(t -> t.getDirection() == Direction.STRAIGHT).sorted(new Comparator<TrackPart>() {
			@Override
			public int compare(TrackPart trackPart1, TrackPart trackPart2) {
				if (trackPart1 == null)
					return 0;
				if (trackPart2 == null)
					return 0;
				return -(new Long(trackPart1.getDuration()).compareTo(trackPart2.getDuration()));
			}
		}).collect(Collectors.toList());
	}

}
