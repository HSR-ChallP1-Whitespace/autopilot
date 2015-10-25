package ch.hsr.whitespace.javapilot.akka.messages;

public class AccelerateMessage {

	private int trackPartId;
	private int speed;

	public AccelerateMessage(int trackPartId, int speed) {
		this.speed = speed;
		this.trackPartId = trackPartId;
	}

	public int getSpeed() {
		return speed;
	}

	public int getTrackPartId() {
		return trackPartId;
	}

}
