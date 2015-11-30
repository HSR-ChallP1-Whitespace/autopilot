package ch.hsr.whitespace.javapilot.akka.messages;

public class SpeedupMessage {

	private boolean speedup = false;

	public boolean isSpeedup() {
		return speedup;
	}

	public void setSpeedup(boolean speedup) {
		this.speedup = speedup;
	}

	public SpeedupMessage(boolean speedup) {
		super();
		this.speedup = speedup;
	}

}
