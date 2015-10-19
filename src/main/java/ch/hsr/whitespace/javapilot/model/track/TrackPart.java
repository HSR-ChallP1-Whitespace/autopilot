package ch.hsr.whitespace.javapilot.model.track;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrackPart {

	private final Logger LOGGER = LoggerFactory.getLogger(TrackPart.class);

	private Direction direction;
	private long startTime;
	private long endTime;
	private int currentPower;

	public TrackPart(Direction direction) {
		this.direction = direction;
	}

	public TrackPart(Direction direction, long startTime, long endTime) {
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

	public int getCurrentPower() {
		return currentPower;
	}

	public void setCurrentPower(int currentPower) {
		this.currentPower = currentPower;
	}

	public boolean hasSameDirection(TrackPart otherTrackPart) {
		return this.direction == otherTrackPart.getDirection();
	}

	public long getDuration() {
		return endTime - startTime;
	}

	public int accelerate(int amount) {
		currentPower += amount;
		LOGGER.info("Accelerate: " + currentPower);
		return currentPower;
	}

	@Override
	public String toString() {
		return "TrackPart[direction=" + direction + ", start=" + startTime + ", end=" + endTime + "]";
	}

}
