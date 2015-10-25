package ch.hsr.whitespace.javapilot.model.track.recognition;

import ch.hsr.whitespace.javapilot.model.track.Direction;

public class RecognitionTrackPart {

	private Direction direction;
	private long startTime;
	private long endTime;

	public RecognitionTrackPart(Direction direction) {
		this.direction = direction;
	}

	public RecognitionTrackPart(Direction direction, long startTime, long endTime) {
		this(direction);
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public boolean hasSameDirection(RecognitionTrackPart otherTrackPart) {
		return this.direction == otherTrackPart.getDirection();
	}

	public long getDuration() {
		return endTime - startTime;
	}

	@Override
	public String toString() {
		return "TrackPart[direction=" + direction + ", start=" + startTime + ", end=" + endTime + "]";
	}

}
