package ch.hsr.whitespace.javapilot.akka.messages;

import ch.hsr.whitespace.javapilot.model.track.Direction;

public class LostPositionMessage {

	private long timeStamp;
	private Direction expectedDirection;
	private Direction detectedDirection;

	public LostPositionMessage(long timeStamp, Direction expectedDirection, Direction detectedDirection) {
		super();
		this.timeStamp = timeStamp;
		this.expectedDirection = expectedDirection;
		this.detectedDirection = detectedDirection;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public Direction getExpectedDirection() {
		return expectedDirection;
	}

	public Direction getDetectedDirection() {
		return detectedDirection;
	}

}
