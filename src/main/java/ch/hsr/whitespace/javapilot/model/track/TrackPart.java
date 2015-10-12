package ch.hsr.whitespace.javapilot.model.track;

public class TrackPart {

	private Direction direction;
	private long startTime;
	private long endTime;

	public TrackPart(Direction direction, long startTime, long endTime) {
		super();
		this.direction = direction;
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

	public boolean hasSameDirection(TrackPart otherTrackPart) {
		return this.direction == otherTrackPart.getDirection();
	}

	@Override
	public String toString() {
		return "TrackPart[direction=" + direction + ", start=" + startTime + ", end=" + endTime + "]";
	}

}
