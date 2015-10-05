package ch.hsr.whitespace.javapilot.model.data_analysis;

public class GyrZGraphValue {

	private long time;
	private double value = 0;
	private double valueSmoothed = 0;

	public GyrZGraphValue(long time) {
		this.time = time;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public double getValueSmoothed() {
		return valueSmoothed;
	}

	public void setValueSmoothed(double valueSmoothed) {
		this.valueSmoothed = valueSmoothed;
	}

}
