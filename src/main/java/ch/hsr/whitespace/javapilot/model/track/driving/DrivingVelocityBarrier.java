package ch.hsr.whitespace.javapilot.model.track.driving;

import ch.hsr.whitespace.javapilot.model.track.recognition.RecognitionVelocityBarrier;

public class DrivingVelocityBarrier {

	private double positionInTrackPart;
	private boolean hasPenalty;
	private double penaltySpeed;
	private double maximumSpeed;
	private int powerAtPenalty;

	public DrivingVelocityBarrier(RecognitionVelocityBarrier recognitionBarrier) {
		this.positionInTrackPart = recognitionBarrier.getPositionInTrackPart();
		this.hasPenalty = false;
		this.penaltySpeed = 0.0;
		this.maximumSpeed = 0.0;
		this.powerAtPenalty = 0;
	}

	public double getPositionInTrackPart() {
		return positionInTrackPart;
	}

	public void setPositionInTrackPart(double positionInTrackPart) {
		this.positionInTrackPart = positionInTrackPart;
	}

	public boolean hasPenalty() {
		return hasPenalty;
	}

	public void setHasPenalty(boolean hasPenalty) {
		this.hasPenalty = hasPenalty;
	}

	public double getPenaltySpeed() {
		return penaltySpeed;
	}

	public void setPenaltySpeed(double penaltySpeed) {
		this.penaltySpeed = penaltySpeed;
	}

	public double getMaximumSpeed() {
		return maximumSpeed;
	}

	public void setMaximumSpeed(double maximumSpeed) {
		this.maximumSpeed = maximumSpeed;
	}

	public int getPowerAtPenalty() {
		return powerAtPenalty;
	}

	public void setPowerAtPenalty(int powerAtPenalty) {
		this.powerAtPenalty = powerAtPenalty;
	}

}
