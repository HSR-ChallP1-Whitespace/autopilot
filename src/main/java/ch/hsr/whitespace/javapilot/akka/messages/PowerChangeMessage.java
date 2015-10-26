package ch.hsr.whitespace.javapilot.akka.messages;

import ch.hsr.whitespace.javapilot.model.Power;

public class PowerChangeMessage {

	private int trackPartId;
	private Power power;

	public PowerChangeMessage(int trackPartId, Power power) {
		this.power = power;
		this.trackPartId = trackPartId;
	}

	public Power getPower() {
		return new Power(power.getValue());
	}

	public int getTrackPartId() {
		return trackPartId;
	}

}
