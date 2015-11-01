package ch.hsr.whitespace.javapilot.model.track.recognition;

import java.util.ArrayList;
import java.util.List;

import ch.hsr.whitespace.javapilot.model.track.Direction;

public class RecognitionTrackPart {

	private Direction direction;
	private long startTime;
	private long endTime;
	private List<RecognitionVelocityBarrier> velocityBarriers;

	public RecognitionTrackPart(Direction direction) {
		this.direction = direction;
		this.velocityBarriers = new ArrayList<>();
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

	public void addVelocityBarrier(RecognitionVelocityBarrier barrier) {
		calculateBarrierPosition(barrier);
		this.velocityBarriers.add(barrier);
	}

	public List<RecognitionVelocityBarrier> getVelocityBarriers() {
		return new ArrayList<>(this.velocityBarriers);
	}

	private void calculateBarrierPosition(RecognitionVelocityBarrier barrier) {
		double barrierTimeStamp = barrier.getTimestamp() - startTime;
		double endTimeStamp = endTime - startTime;
		barrier.setPositionInTrackPart(barrierTimeStamp / endTimeStamp);
	}

	@Override
	public String toString() {
		String barriers = "";
		int counter = 1;
		for (RecognitionVelocityBarrier barrier : velocityBarriers) {
			barriers += "barrier" + counter + "=" + barrier.getPositionInTrackPart() + ", ";
			counter++;
		}
		return "TrackPart[direction=" + direction + ", start=" + startTime + ", end=" + endTime + ", " + barriers + "]";
	}

}
