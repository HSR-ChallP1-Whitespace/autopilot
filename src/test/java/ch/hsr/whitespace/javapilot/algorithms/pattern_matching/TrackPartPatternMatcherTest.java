package ch.hsr.whitespace.javapilot.algorithms.pattern_matching;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ch.hsr.whitespace.javapilot.algorithms.pattern_matching.impl.TrackPartPatternMatcherImpl;
import ch.hsr.whitespace.javapilot.model.track.Direction;
import ch.hsr.whitespace.javapilot.model.track.recognition.RecognitionTrackPart;

public class TrackPartPatternMatcherTest {

	private TrackPartPatternMatcher matcher;

	@Test
	public void testEmptyList() {
		List<RecognitionTrackPart> list = new ArrayList<>();
		matcher = new TrackPartPatternMatcherImpl(list);
		assertEquals(-1, matcher.match());
	}

	@Test
	public void testSingleElement() {
		List<RecognitionTrackPart> list = new ArrayList<>();
		list.add(new RecognitionTrackPart(Direction.LEFT));
		matcher = new TrackPartPatternMatcherImpl(list);
		assertEquals(1, matcher.match());
	}

	@Test
	public void testSimpleMatch() {
		List<RecognitionTrackPart> list = new ArrayList<>();
		list.add(new RecognitionTrackPart(Direction.LEFT));
		list.add(new RecognitionTrackPart(Direction.RIGHT));
		list.add(new RecognitionTrackPart(Direction.STRAIGHT));
		list.add(new RecognitionTrackPart(Direction.LEFT));
		list.add(new RecognitionTrackPart(Direction.RIGHT));
		list.add(new RecognitionTrackPart(Direction.STRAIGHT));
		matcher = new TrackPartPatternMatcherImpl(list);
		assertEquals(0, matcher.match());
	}

	@Test
	public void testWithOneFailure() {
		List<RecognitionTrackPart> list = new ArrayList<>();
		list.add(new RecognitionTrackPart(Direction.LEFT));
		list.add(new RecognitionTrackPart(Direction.RIGHT));
		list.add(new RecognitionTrackPart(Direction.STRAIGHT));
		list.add(new RecognitionTrackPart(Direction.LEFT));
		list.add(new RecognitionTrackPart(Direction.STRAIGHT));
		list.add(new RecognitionTrackPart(Direction.RIGHT));
		list.add(new RecognitionTrackPart(Direction.STRAIGHT));
		matcher = new TrackPartPatternMatcherImpl(list);
		assertEquals(1, matcher.match());
	}

	@Test
	public void testWithTwoFailures() {
		List<RecognitionTrackPart> list = new ArrayList<>();
		list.add(new RecognitionTrackPart(Direction.LEFT));
		list.add(new RecognitionTrackPart(Direction.RIGHT));
		list.add(new RecognitionTrackPart(Direction.STRAIGHT));
		list.add(new RecognitionTrackPart(Direction.LEFT));
		list.add(new RecognitionTrackPart(Direction.LEFT));
		list.add(new RecognitionTrackPart(Direction.RIGHT));
		list.add(new RecognitionTrackPart(Direction.RIGHT));
		list.add(new RecognitionTrackPart(Direction.STRAIGHT));
		matcher = new TrackPartPatternMatcherImpl(list);
		assertEquals(2, matcher.match());
	}

	@Test
	public void testWithFourFailures() {
		List<RecognitionTrackPart> list = new ArrayList<>();
		list.add(new RecognitionTrackPart(Direction.LEFT));
		list.add(new RecognitionTrackPart(Direction.RIGHT));
		list.add(new RecognitionTrackPart(Direction.LEFT));
		list.add(new RecognitionTrackPart(Direction.RIGHT));
		list.add(new RecognitionTrackPart(Direction.LEFT));
		list.add(new RecognitionTrackPart(Direction.RIGHT));
		list.add(new RecognitionTrackPart(Direction.STRAIGHT));
		list.add(new RecognitionTrackPart(Direction.LEFT));
		list.add(new RecognitionTrackPart(Direction.STRAIGHT));
		list.add(new RecognitionTrackPart(Direction.LEFT));
		list.add(new RecognitionTrackPart(Direction.STRAIGHT));
		list.add(new RecognitionTrackPart(Direction.LEFT));
		matcher = new TrackPartPatternMatcherImpl(list);
		assertEquals(4, matcher.match());
	}

}
