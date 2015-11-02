package ch.hsr.whitespace.javapilot.model.track.driving;

import java.util.ArrayList;
import java.util.List;

import ch.hsr.whitespace.javapilot.model.Power;
import ch.hsr.whitespace.javapilot.model.track.Direction;

public class DrivingTrackPart {

	private int id;
	private Direction direction;
	private long startTime;
	private long endTime;
	private long localStartTime;
	private long localEndTime;
	private Power currentPower;
	private List<DrivingVelocityBarrier> velocityBarriers;
	private DrivingVelocityBarrier lastVelocityBarrier;
	private int lastVelocityBarrierIndex = 0;

	public DrivingTrackPart(int id, Direction direction) {
		this.id = id;
		this.direction = direction;
		this.velocityBarriers = new ArrayList<>();
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

	public long getLocalStartTime() {
		return localStartTime;
	}

	public void setLocalStartTime(long localStartTime) {
		this.localStartTime = localStartTime;
	}

	public long getLocalEndTime() {
		return localEndTime;
	}

	public void setLocalEndTime(long localEndTime) {
		this.localEndTime = localEndTime;
	}

	public boolean hasSameDirection(DrivingTrackPart otherTrackPart) {
		return this.direction == otherTrackPart.getDirection();
	}

	public long getDuration() {
		return endTime - startTime;
	}

	public long getLocalDuration() {
		return localEndTime - localStartTime;
	}

	public Power getCurrentPower() {
		return currentPower;
	}

	public List<DrivingVelocityBarrier> getVelocityBarriers() {
		return new ArrayList<>(velocityBarriers);
	}

	public void setCurrentPower(Power currentPower) {
		this.currentPower = new Power(currentPower.getValue());
	}

	public void setVelocityBarriers(List<DrivingVelocityBarrier> barriers) {
		this.velocityBarriers.clear();
		this.velocityBarriers.addAll(barriers);
	}

	public void passedVelocityBarrier() throws WrongTrackPartException {
		if ((velocityBarriers.size() - 1) < lastVelocityBarrierIndex)
			throw new WrongTrackPartException();

		lastVelocityBarrier = velocityBarriers.get(lastVelocityBarrierIndex);
		increaseVelocityBarrierIndex();
	}

	private void increaseVelocityBarrierIndex() {
		if (lastVelocityBarrierIndex == (velocityBarriers.size() - 1)) {
			lastVelocityBarrierIndex = 0;
		} else {
			lastVelocityBarrierIndex++;
		}
	}

	public void handlePenalty(double penaltySpeed, double maximumSpeed) {
		lastVelocityBarrier.setHasPenalty(true);
		lastVelocityBarrier.setPenaltySpeed(penaltySpeed);
		lastVelocityBarrier.setMaximumSpeed(maximumSpeed);
		lastVelocityBarrier.setPowerAtPenalty(currentPower.getValue());
		currentPower = currentPower.reduce(5);
	}

	public boolean hasPenalty() {
		for (DrivingVelocityBarrier barrier : velocityBarriers) {
			if (barrier.hasPenalty())
				return true;
		}
		return false;
	}

	public boolean hasBarrier() {
		return velocityBarriers.size() > 0;
	}

	@Override
	public String toString() {
		return "TrackPart[direction=" + direction + ", start=" + startTime + ", end=" + endTime + "]";
	}

}
