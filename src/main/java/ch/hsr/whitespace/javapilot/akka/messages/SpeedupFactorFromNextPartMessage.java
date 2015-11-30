package ch.hsr.whitespace.javapilot.akka.messages;

public class SpeedupFactorFromNextPartMessage {

	private long lastDuration;
	private long currentDuration;

	public SpeedupFactorFromNextPartMessage(long lastDuration, long currentDuration) {
		super();
		this.lastDuration = lastDuration;
		this.currentDuration = currentDuration;
	}

	public long getLastDuration() {
		return lastDuration;
	}

	public void setLastDuration(long lastDuration) {
		this.lastDuration = lastDuration;
	}

	public long getCurrentDuration() {
		return currentDuration;
	}

	public void setCurrentDuration(long currentDuration) {
		this.currentDuration = currentDuration;
	}

}
