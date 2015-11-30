package ch.hsr.whitespace.javapilot.model;

public class Power {

	private static final int MAX_POWER = 255;
	private static final int MIN_POWER = 0;

	private int value;

	public Power(int value) {
		this.value = value;
	}

	public Power increase(int amount) {
		return new Power(Math.min(value + amount, MAX_POWER));
	}

	public Power reduce(int amount) {
		return new Power(Math.max(value - amount, MIN_POWER));
	}

	public Power max() {
		return new Power(MAX_POWER);
	}

	public Power min() {
		return new Power(MIN_POWER);
	}

	public int getValue() {
		return value;
	}

}
