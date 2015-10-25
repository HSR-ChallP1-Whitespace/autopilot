package ch.hsr.whitespace.javapilot.model.data.analysis;

public class RoundTimeValue {

	private int round;
	private long roundTime;

	public RoundTimeValue(int round, long roundTime) {
		super();
		this.round = round;
		this.roundTime = roundTime;
	}

	public long getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public long getRoundTime() {
		return roundTime;
	}

	public void setRoundTime(long roundTime) {
		this.roundTime = roundTime;
	}

}
