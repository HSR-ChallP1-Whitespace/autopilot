package ch.hsr.whitespace.javapilot.model.track.recognition.matching;

import java.util.ArrayList;
import java.util.List;

import ch.hsr.whitespace.javapilot.model.track.recognition.RecognitionTrackPart;
import ch.hsr.whitespace.javapilot.util.ListSplittupUtil;

public class TrackPartMatcher {

	private List<RecognitionTrackPart> trackParts;
	private ListSplittupUtil listSplitter;
	private PossibleTrackMatch lastMatch;

	public TrackPartMatcher(List<RecognitionTrackPart> trackParts) {
		this.trackParts = trackParts;
		this.listSplitter = new ListSplittupUtil();
	}

	public boolean match() {
		if (trackParts.isEmpty() || (trackParts.size() % 2) != 0)
			return false;

		List<RecognitionTrackPart> trackPartList = new ArrayList<RecognitionTrackPart>(trackParts);
		List<RecognitionTrackPart> list1 = new ArrayList<RecognitionTrackPart>();
		List<RecognitionTrackPart> list2 = new ArrayList<RecognitionTrackPart>();
		listSplitter.splitListIntoTwoParts(trackPartList, list1, list2);

		if (list1.size() != list2.size())
			return false;

		for (int i = 0; i < list1.size(); i++) {
			if (!list1.get(i).hasSameDirection(list2.get(i))) {
				return false;
			}
		}
		lastMatch = new PossibleTrackMatch(list2);
		return true;
	}

	public PossibleTrackMatch getLastMatch() {
		return lastMatch;
	}

}
