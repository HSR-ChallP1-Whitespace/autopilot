package ch.hsr.whitespace.javapilot.algorithms.pattern_matching.impl;

import java.util.ArrayList;
import java.util.List;

import ch.hsr.whitespace.javapilot.algorithms.LevenshteinDistance;
import ch.hsr.whitespace.javapilot.algorithms.ListSplitter;
import ch.hsr.whitespace.javapilot.algorithms.pattern_matching.TrackPartPatternMatcher;
import ch.hsr.whitespace.javapilot.model.track.recognition.RecognitionTrackPart;
import ch.hsr.whitespace.javapilot.model.track.recognition.matching.PossibleTrackMatch;

public class TrackPartPatternMatcherImpl implements TrackPartPatternMatcher {

	private List<RecognitionTrackPart> trackParts;
	private ListSplitter listSplitter;
	private List<PossibleTrackMatch> possibleMatches;

	private List<RecognitionTrackPart> list1;
	private List<RecognitionTrackPart> list2;

	public TrackPartPatternMatcherImpl(List<RecognitionTrackPart> trackParts) {
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
		List<RecognitionTrackPart> trackPartList = new ArrayList<RecognitionTrackPart>(trackParts);
		list1 = new ArrayList<RecognitionTrackPart>();
		list2 = new ArrayList<RecognitionTrackPart>();
		listSplitter.splitListIntoTwoParts(trackPartList, list1, list2);
		possibleMatches.add(new PossibleTrackMatch(list1));
		possibleMatches.add(new PossibleTrackMatch(list2));
	}

	private String getStringFromPartList(List<RecognitionTrackPart> trackParts) {
		StringBuilder sb = new StringBuilder();
		for (RecognitionTrackPart trackPart : trackParts) {
			sb.append(trackPart.getDirection().toShortString());
		}
		return sb.toString();
	}

	@Override
	public List<PossibleTrackMatch> getPossibleMatches() {
		return possibleMatches;
	}

}
