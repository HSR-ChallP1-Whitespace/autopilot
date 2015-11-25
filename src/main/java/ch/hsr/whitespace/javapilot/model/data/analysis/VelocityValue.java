package ch.hsr.whitespace.javapilot.model.data.analysis;

public class VelocityValue {

	private long timestamp;
	private double velocity;

	public VelocityValue(long timestamp, double velocity) {
		super();
		this.timestamp = timestamp;
		this.velocity = velocity;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public double getVelocity() {
		return velocity;
	}

	public void setVelocity(double velocity) {
		this.velocity = velocity;
	}

}
