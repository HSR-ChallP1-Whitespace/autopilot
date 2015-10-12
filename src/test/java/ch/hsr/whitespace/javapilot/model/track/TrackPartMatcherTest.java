package ch.hsr.whitespace.javapilot.model.track;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ch.hsr.whitespace.javapilot.model.track.matching.TrackPartMatcher;

public class TrackPartMatcherTest {

	private TrackPartMatcher matcher;

	@Test
	public void testWithEmptyList() {
		List<TrackPart> list = new ArrayList<>();
		matcher = new TrackPartMatcher(list);
		assertEquals(false, matcher.match());
	}

	@Test
	public void testNoMatches() {
		List<TrackPart> list = new ArrayList<>();
		list.add(new TrackPart(Direction.LEFT, 0, 0));
		list.add(new TrackPart(Direction.RIGHT, 0, 0));
		list.add(new TrackPart(Direction.STRAIGHT, 0, 0));
		matcher = new TrackPartMatcher(list);
		assertEquals(false, matcher.match());

		List<TrackPart> list2 = new ArrayList<>();
		list2.add(new TrackPart(Direction.STRAIGHT, 0, 0));
		list2.add(new TrackPart(Direction.RIGHT, 0, 0));
		matcher = new TrackPartMatcher(list2);
		assertEquals(false, matcher.match());
	}

	@Test
	public void testMatch() {
		List<TrackPart> list = new ArrayList<>();
		list.add(new TrackPart(Direction.LEFT, 0, 0));
		list.add(new TrackPart(Direction.RIGHT, 0, 0));
		list.add(new TrackPart(Direction.STRAIGHT, 0, 0));
		list.add(new TrackPart(Direction.LEFT, 0, 0));
		list.add(new TrackPart(Direction.RIGHT, 0, 0));
		list.add(new TrackPart(Direction.STRAIGHT, 0, 0));
		matcher = new TrackPartMatcher(list);
		assertEquals(true, matcher.match());
	}

}
