package ch.hsr.whitespace.javapilot.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Power {

	private final Logger LOGGER = LoggerFactory.getLogger(Power.class);

	public static final int MAX_POWER = 255;
	public static final int MIN_POWER = 0;

	private int value;

	public Power(Power power) {
		this(power.getValue());
	}

	public Power(int value) {
		this.value = value;
		if (value > MAX_POWER)
			this.value = MAX_POWER;
		if (value < MIN_POWER)
			this.value = MIN_POWER;
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

	public double calcDiffFactor(Power otherPower) {
		double result = new Double(value) / new Double(otherPower.getValue());
		LOGGER.info("calcDiffFactor: " + result + "(thisPower=" + value + ", otherPower=" + otherPower.getValue() + ")");
		return result;
	}

	@Override
	public String toString() {
		return "Power[" + value + "]";
	}

}
