package ch.hsr.whitespace.javapilot.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.hsr.whitespace.javapilot.akka.messages.LostPositionMessage;
import ch.hsr.whitespace.javapilot.akka.messages.TrackPartEnteredMessage;
import ch.hsr.whitespace.javapilot.model.track.Direction;

public class MessageUtilTest {

	@Test
	public void testNeededForward() {
		TrackPartEnteredMessage message = new TrackPartEnteredMessage(0, Direction.LEFT);
		assertTrue(MessageUtil.isMessageForwardNeeded(message, new Class[] { TrackPartEnteredMessage.class }));
	}

	@Test
	public void testNotNeededForward() {
		TrackPartEnteredMessage message = new TrackPartEnteredMessage(0, Direction.LEFT);
		assertTrue(!MessageUtil.isMessageForwardNeeded(message, new Class[] { LostPositionMessage.class }));
	}

}
