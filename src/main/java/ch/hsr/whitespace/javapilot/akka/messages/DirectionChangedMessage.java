package ch.hsr.whitespace.javapilot.akka.messages;

import ch.hsr.whitespace.javapilot.model.track.Direction;

public class DirectionChangedMessage {

	private long timeStamp;
	private Direction newDirection;

	public DirectionChangedMessage(long timeStamp, Direction newDirection) {
		this.timeStamp = timeStamp;
		this.newDirection = newDirection;
	}

	public Direction getNewDirection() {
		return newDirection;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

}
