package ch.hsr.whitespace.javapilot.model.data.analysis;

public class GyrZGraphValue {

	private long time;
	private double value = 0;
	private double valueSmoothed = 0;
	private double valueStdDev = 0;
	private double meanDevFromZero = 0;

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

	public double getValueStdDev() {
		return valueStdDev;
	}

	public void setValueStdDev(double valueStdDev) {
		this.valueStdDev = valueStdDev;
	}

	public double getMeanDevFromZero() {
		return meanDevFromZero;
	}

	public void setMeanDevFromZero(double meanDevFromZero) {
		this.meanDevFromZero = meanDevFromZero;
	}

}
