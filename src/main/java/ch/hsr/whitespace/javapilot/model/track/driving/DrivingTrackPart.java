package ch.hsr.whitespace.javapilot.model.track.driving;

import ch.hsr.whitespace.javapilot.model.track.Direction;

public class DrivingTrackPart {

	private int id;
	private Direction direction;
	private long startTime;
	private long endTime;
	private int currentPower;

	public DrivingTrackPart(int id, Direction direction) {
		this.id = id;
		this.direction = direction;
	}

	public DrivingTrackPart(int id, Direction direction, long startTime, long endTime) {
		this(id, direction);
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public int getId() {
		return id;
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

	public boolean hasSameDirection(DrivingTrackPart otherTrackPart) {
		return this.direction == otherTrackPart.getDirection();
	}

	public long getDuration() {
		return endTime - startTime;
	}

	public int getCurrentPower() {
		return currentPower;
	}

	public void setCurrentPower(int currentPower) {
		this.currentPower = currentPower;
	}

	public int accelerate(int amount) {
		currentPower += amount;
		return currentPower;
	}

	@Override
	public String toString() {
		return "TrackPart[direction=" + direction + ", start=" + startTime + ", end=" + endTime + "]";
	}

}
