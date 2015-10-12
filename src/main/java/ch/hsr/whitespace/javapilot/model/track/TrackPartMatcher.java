package ch.hsr.whitespace.javapilot.model.track;

import java.util.ArrayList;
import java.util.List;

import ch.hsr.whitespace.javapilot.util.ListSplittupUtil;

public class TrackPartMatcher {

	private List<TrackPart> trackParts;
	private ListSplittupUtil listSplitter;

	public TrackPartMatcher(List<TrackPart> trackParts) {
		this.trackParts = trackParts;
		this.listSplitter = new ListSplittupUtil();
	}

	public boolean match() {
		if (trackParts.isEmpty() || (trackParts.size() % 2) != 0)
			return false;

		List<TrackPart> trackPartList = new ArrayList<TrackPart>(trackParts);
		List<TrackPart> list1 = new ArrayList<TrackPart>();
		List<TrackPart> list2 = new ArrayList<TrackPart>();
		listSplitter.splitListIntoTwoParts(trackPartList, list1, list2);

		if (list1.size() != list2.size())
			return false;

		for (int i = 0; i < list1.size(); i++) {
			if (!list1.get(i).hasSameDirection(list2.get(i))) {
				return false;
			}
		}
		return true;
	}

}
