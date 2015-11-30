package ch.hsr.whitespace.javapilot.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ch.hsr.whitespace.javapilot.model.track.Direction;
import ch.hsr.whitespace.javapilot.model.track.TrackPart;

public class TrackPartUtilTest {

	@Test
	public void testAllPartsAreStraight() {
		List<TrackPart> parts = new ArrayList<>();
		parts.add(new TrackPart(Direction.LEFT));
		parts.add(new TrackPart(Direction.STRAIGHT));
		parts.add(new TrackPart(Direction.RIGHT));
		parts.add(new TrackPart(Direction.STRAIGHT));
		parts.add(new TrackPart(Direction.LEFT));

		List<TrackPart> resultList = TrackPartUtil.getStraightPartsByDuration(parts);
		assertEquals(2, resultList.size());
		for (TrackPart part : resultList) {
			assertEquals(Direction.STRAIGHT, part.getDirection());
		}
	}

	@Test
	public void testStraightPartDurationOrder() {
		List<TrackPart> parts = new ArrayList<>();
		parts.add(new TrackPart(Direction.LEFT));
		parts.add(new TrackPart(Direction.STRAIGHT, 0, 20)); // duration: 20
		parts.add(new TrackPart(Direction.RIGHT));
		parts.add(new TrackPart(Direction.STRAIGHT, 1, 5)); // duration: 4
		parts.add(new TrackPart(Direction.LEFT));
		parts.add(new TrackPart(Direction.STRAIGHT, 1, 7)); // duration: 6

		List<TrackPart> resultList = TrackPartUtil.getStraightPartsByDuration(parts);
		assertEquals(3, resultList.size());
		assertEquals(20L, resultList.get(0).getDuration());
		assertEquals(6L, resultList.get(1).getDuration());
		assertEquals(4L, resultList.get(2).getDuration());
	}

}
