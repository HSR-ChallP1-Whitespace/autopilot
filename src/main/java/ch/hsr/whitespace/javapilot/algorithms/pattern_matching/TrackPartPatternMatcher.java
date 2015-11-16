package ch.hsr.whitespace.javapilot.algorithms.pattern_matching;

import java.util.List;

import ch.hsr.whitespace.javapilot.model.track.recognition.matching.PossibleTrackMatch;

public interface TrackPartPatternMatcher {

	/**
	 * Matches a pattern in Strings like S-L-R-S-L-R and returns a failure
	 * number.
	 * 
	 * @return Returns the failure number (amount of parts that do not match).
	 */
	public int match();

	/**
	 * @return Returns the possible matches.
	 */
	public List<PossibleTrackMatch> getPossibleMatches();

}
