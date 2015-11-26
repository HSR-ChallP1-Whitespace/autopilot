package ch.hsr.whitespace.javapilot.algorithms.pattern_matching;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ch.hsr.whitespace.javapilot.algorithms.pattern_matching.impl.TrackPartPatternMatcherImpl;
import ch.hsr.whitespace.javapilot.model.track.Direction;
import ch.hsr.whitespace.javapilot.model.track.TrackPart;

public class TrackPartPatternMatcherTest {

	private TrackPartPatternMatcher matcher;

	@Test
	public void testEmptyList() {
		List<TrackPart> list = new ArrayList<>();
		matcher = new TrackPartPatternMatcherImpl(list);
		assertEquals(-1, matcher.match());
	}

	@Test
	public void testSingleElement() {
		List<TrackPart> list = new ArrayList<>();
		list.add(new TrackPart(Direction.LEFT));
		matcher = new TrackPartPatternMatcherImpl(list);
		assertEquals(1, matcher.match());
	}

	@Test
	public void testSimpleMatch() {
		List<TrackPart> list = new ArrayList<>();
		list.add(new TrackPart(Direction.LEFT));
		list.add(new TrackPart(Direction.RIGHT));
		list.add(new TrackPart(Direction.STRAIGHT));
		list.add(new TrackPart(Direction.LEFT));
		list.add(new TrackPart(Direction.RIGHT));
		list.add(new TrackPart(Direction.STRAIGHT));
		matcher = new TrackPartPatternMatcherImpl(list);
		assertEquals(0, matcher.match());
	}

	@Test
	public void testWithOneFailure() {
		List<TrackPart> list = new ArrayList<>();
		list.add(new TrackPart(Direction.LEFT));
		list.add(new TrackPart(Direction.RIGHT));
		list.add(new TrackPart(Direction.STRAIGHT));
		list.add(new TrackPart(Direction.LEFT));
		list.add(new TrackPart(Direction.STRAIGHT));
		list.add(new TrackPart(Direction.RIGHT));
		list.add(new TrackPart(Direction.STRAIGHT));
		matcher = new TrackPartPatternMatcherImpl(list);
		assertEquals(1, matcher.match());
	}

	@Test
	public void testWithTwoFailures() {
		List<TrackPart> list = new ArrayList<>();
		list.add(new TrackPart(Direction.LEFT));
		list.add(new TrackPart(Direction.RIGHT));
		list.add(new TrackPart(Direction.STRAIGHT));
		list.add(new TrackPart(Direction.LEFT));
		list.add(new TrackPart(Direction.LEFT));
		list.add(new TrackPart(Direction.RIGHT));
		list.add(new TrackPart(Direction.RIGHT));
		list.add(new TrackPart(Direction.STRAIGHT));
		matcher = new TrackPartPatternMatcherImpl(list);
		assertEquals(2, matcher.match());
	}

	@Test
	public void testWithFourFailures() {
		List<TrackPart> list = new ArrayList<>();
		list.add(new TrackPart(Direction.LEFT));
		list.add(new TrackPart(Direction.RIGHT));
		list.add(new TrackPart(Direction.LEFT));
		list.add(new TrackPart(Direction.RIGHT));
		list.add(new TrackPart(Direction.LEFT));
		list.add(new TrackPart(Direction.RIGHT));
		list.add(new TrackPart(Direction.STRAIGHT));
		list.add(new TrackPart(Direction.LEFT));
		list.add(new TrackPart(Direction.STRAIGHT));
		list.add(new TrackPart(Direction.LEFT));
		list.add(new TrackPart(Direction.STRAIGHT));
		list.add(new TrackPart(Direction.LEFT));
		matcher = new TrackPartPatternMatcherImpl(list);
		assertEquals(4, matcher.match());
	}

}
