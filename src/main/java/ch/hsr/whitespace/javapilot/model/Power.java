package ch.hsr.whitespace.javapilot.model;

public class Power {

	private int value;

	public Power(int value) {
		this.value = value;
	}

	public Power increase(int amount) {
		return new Power(Math.min(getValue() + amount, 255));
	}

	public Power reduce(int amount) {
		return new Power(Math.max(getValue() - amount, 0));
	}

	public int getValue() {
		return value;
	}

}
