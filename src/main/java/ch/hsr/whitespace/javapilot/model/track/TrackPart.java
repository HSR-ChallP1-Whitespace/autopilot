package ch.hsr.whitespace.javapilot.model.track;

import java.util.ArrayList;
import java.util.List;

public class TrackPart {

	private int id;
	private Direction direction;
	private long startTime;
	private long endTime;
	private List<VelocityBarrier> velocityBarriers;

	public TrackPart(Direction direction) {
		this.direction = direction;
		this.velocityBarriers = new ArrayList<>();
	}

	public TrackPart(Direction direction, long startTime, long endTime) {
		this(direction);
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public long getDuration() {
		return endTime - startTime;
	}

	public void addVelocityBarrier(VelocityBarrier barrier) {
		calculateBarrierPosition(barrier);
		this.velocityBarriers.add(barrier);
	}

	public List<VelocityBarrier> getVelocityBarriers() {
		return new ArrayList<>(this.velocityBarriers);
	}

	private void calculateBarrierPosition(VelocityBarrier barrier) {
		double barrierTimeStamp = barrier.getTimestamp() - startTime;
		double endTimeStamp = endTime - startTime;
		barrier.setPositionInTrackPart(barrierTimeStamp / endTimeStamp);
	}

	@Override
	public String toString() {
		String barriers = "";
		int counter = 1;
		for (VelocityBarrier barrier : velocityBarriers) {
			barriers += "barrier" + counter + "=" + barrier.getPositionInTrackPart() + ", ";
			counter++;
		}
		return "TrackPart[direction=" + direction + ", start=" + startTime + ", end=" + endTime + ", " + barriers + "]";
	}

}
