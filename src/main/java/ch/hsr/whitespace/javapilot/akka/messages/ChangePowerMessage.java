package ch.hsr.whitespace.javapilot.akka.messages;

import ch.hsr.whitespace.javapilot.model.Power;

public class ChangePowerMessage {

	private Power newPower;

	public ChangePowerMessage(Power newPower) {
		super();
		this.newPower = newPower;
	}

	public Power getNewPower() {
		return newPower;
	}

	public void setNewPower(Power newPower) {
		this.newPower = newPower;
	}

}
