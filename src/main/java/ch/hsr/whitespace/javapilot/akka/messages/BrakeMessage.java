package ch.hsr.whitespace.javapilot.akka.messages;

public class BrakeMessage {

	private int reducedPower;

	public BrakeMessage(int reducedPower) {
		this.reducedPower = reducedPower;
	}

	public int getReducedPower() {
		return reducedPower;
	}

}
