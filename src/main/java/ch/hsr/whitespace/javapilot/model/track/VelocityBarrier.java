package ch.hsr.whitespace.javapilot.model.track;

public class VelocityBarrier {

	private long timestamp;
	private double positionInTrackPart;
	private double velocity;

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public double getPositionInTrackPart() {
		return positionInTrackPart;
	}

	public void setPositionInTrackPart(double positionInTrackPart) {
		this.positionInTrackPart = positionInTrackPart;
	}

	public double getVelocity() {
		return velocity;
	}

	public void setVelocity(double velocity) {
		this.velocity = velocity;
	}

}
