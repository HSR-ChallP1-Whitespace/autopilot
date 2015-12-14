package ch.hsr.whitespace.javapilot.akka.messages;

import ch.hsr.whitespace.javapilot.model.Power;

public class ChangePowerMessage {

	private Power newPower;
	private long delayInMillis = 0;

	public ChangePowerMessage(Power newPower) {
		super();
		this.newPower = newPower;
	}

	public ChangePowerMessage(Power newPower, long delayInMillis) {
		this(newPower);
		this.delayInMillis = delayInMillis;
	}

	public Power getNewPower() {
		return newPower;
	}

	public void setNewPower(Power newPower) {
		this.newPower = newPower;
	}

	public long getDelayInMillis() {
		return this.delayInMillis;
	}

}
