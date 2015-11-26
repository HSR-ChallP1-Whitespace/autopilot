package ch.hsr.whitespace.javapilot.akka.messages;

import ch.hsr.whitespace.javapilot.model.track.Direction;

public class TrackPartEnteredMessage {

	private long timestamp;
	private Direction trackPartDirection;

	public TrackPartEnteredMessage(long timestamp, Direction trackPartDirection) {
		super();
		this.timestamp = timestamp;
		this.trackPartDirection = trackPartDirection;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Direction getTrackPartDirection() {
		return trackPartDirection;
	}

	public void setTrackPartDirection(Direction trackPartDirection) {
		this.trackPartDirection = trackPartDirection;
	}

}
