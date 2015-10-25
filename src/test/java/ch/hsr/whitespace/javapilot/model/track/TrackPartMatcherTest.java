package ch.hsr.whitespace.javapilot.model.track;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ch.hsr.whitespace.javapilot.model.track.recognition.RecognitionTrackPart;
import ch.hsr.whitespace.javapilot.model.track.recognition.matching.TrackPartMatcher;

public class TrackPartMatcherTest {

	private TrackPartMatcher matcher;

	@Test
	public void testWithEmptyList() {
		List<RecognitionTrackPart> list = new ArrayList<>();
		matcher = new TrackPartMatcher(list);
		assertEquals(false, matcher.match());
	}

	@Test
	public void testNoMatches() {
		List<RecognitionTrackPart> list = new ArrayList<>();
		list.add(new RecognitionTrackPart(Direction.LEFT));
		list.add(new RecognitionTrackPart(Direction.RIGHT));
		list.add(new RecognitionTrackPart(Direction.STRAIGHT));
		matcher = new TrackPartMatcher(list);
		assertEquals(false, matcher.match());

		List<RecognitionTrackPart> list2 = new ArrayList<>();
		list2.add(new RecognitionTrackPart(Direction.STRAIGHT));
		list2.add(new RecognitionTrackPart(Direction.RIGHT));
		matcher = new TrackPartMatcher(list2);
		assertEquals(false, matcher.match());
	}

	@Test
	public void testMatch() {
		List<RecognitionTrackPart> list = new ArrayList<>();
		list.add(new RecognitionTrackPart(Direction.LEFT));
		list.add(new RecognitionTrackPart(Direction.RIGHT));
		list.add(new RecognitionTrackPart(Direction.STRAIGHT));
		list.add(new RecognitionTrackPart(Direction.LEFT));
		list.add(new RecognitionTrackPart(Direction.RIGHT));
		list.add(new RecognitionTrackPart(Direction.STRAIGHT));
		matcher = new TrackPartMatcher(list);
		assertEquals(true, matcher.match());
	}

}
