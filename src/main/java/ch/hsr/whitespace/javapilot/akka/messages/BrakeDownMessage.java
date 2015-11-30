package ch.hsr.whitespace.javapilot.akka.messages;

import ch.hsr.whitespace.javapilot.model.Power;

public class BrakeDownMessage {

	private Power brakeDownPower;

	public BrakeDownMessage(Power brakeDownPower) {
		super();
		this.brakeDownPower = brakeDownPower;
	}

	public Power getBrakeDownPower() {
		return brakeDownPower;
	}

	public void setBrakeDownPower(Power brakeDownPower) {
		this.brakeDownPower = brakeDownPower;
	}

}
