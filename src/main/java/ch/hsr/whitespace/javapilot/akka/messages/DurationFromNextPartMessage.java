package ch.hsr.whitespace.javapilot.akka.messages;

public class DurationFromNextPartMessage {

	private long duration;

	public DurationFromNextPartMessage(long duration) {
		super();
		this.duration = duration;
	}

	public long getDuration() {
		return duration;
	}

}
