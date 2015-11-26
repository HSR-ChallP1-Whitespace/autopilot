package ch.hsr.whitespace.javapilot.algorithms.pattern_matching.impl;

import java.util.ArrayList;
import java.util.List;

import ch.hsr.whitespace.javapilot.algorithms.LevenshteinDistance;
import ch.hsr.whitespace.javapilot.algorithms.ListSplitter;
import ch.hsr.whitespace.javapilot.algorithms.pattern_matching.TrackPartPatternMatcher;
import ch.hsr.whitespace.javapilot.model.track.TrackPart;
import ch.hsr.whitespace.javapilot.model.track.matching.PossibleTrackMatch;

public class TrackPartPatternMatcherImpl implements TrackPartPatternMatcher {

	private List<TrackPart> trackParts;
	private ListSplitter listSplitter;
	private List<PossibleTrackMatch> possibleMatches;

	private List<TrackPart> list1;
	private List<TrackPart> list2;

	public TrackPartPatternMatcherImpl(List<TrackPart> trackParts) {
		this.trackParts = trackParts;
		this.listSplitter = new ListSplitter();
		this.possibleMatches = new ArrayList<>();
	}

	@Override
	public int match() {
		splitList();
		String s1 = getStringFromPartList(list1);
		String s2 = getStringFromPartList(list2);
		return LevenshteinDistance.computeLevenshteinDistance(s1, s2);
	}

	private void splitList() {
		List<TrackPart> trackPartList = new ArrayList<TrackPart>(trackParts);
		list1 = new ArrayList<TrackPart>();
		list2 = new ArrayList<TrackPart>();
		listSplitter.splitListIntoTwoParts(trackPartList, list1, list2);
		possibleMatches.add(new PossibleTrackMatch(list1));
		possibleMatches.add(new PossibleTrackMatch(list2));
	}

	private String getStringFromPartList(List<TrackPart> trackParts) {
		StringBuilder sb = new StringBuilder();
		for (TrackPart trackPart : trackParts) {
			sb.append(trackPart.getDirection().toShortString());
		}
		return sb.toString();
	}

	@Override
	public List<PossibleTrackMatch> getPossibleMatches() {
		return possibleMatches;
	}

}
