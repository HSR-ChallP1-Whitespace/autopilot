package ch.hsr.whitespace.javapilot.model.track;

public enum Direction {

	LEFT, RIGHT, STRAIGHT;

	private static final double GYR_Z_STRAIGHT_STD_DEV_THRESHOLD = 600.00;
	private static final double GYR_Z_LEFT_THRESHOLD = -500.00;
	private static final double GYR_Z_RIGHT_THRESHOLD = 500.00;

	@Override
	public String toString() {
		switch (this) {
		case LEFT:
			return "LEFT";
		case RIGHT:
			return "RIGHT";
		case STRAIGHT:
			return "STRAIGHT";
		}
		return super.toString();
	}

	public static Direction getNewDirection(Direction currentDirection, double gyrzValue, double gyrzStdDev) {
		if (gyrzValue > GYR_Z_RIGHT_THRESHOLD) {
			return Direction.RIGHT;
		} else if (gyrzValue < GYR_Z_LEFT_THRESHOLD) {
			return Direction.LEFT;
		} else if (gyrzStdDev < GYR_Z_STRAIGHT_STD_DEV_THRESHOLD) {
			return Direction.STRAIGHT;
		}
		return currentDirection;
	}

}
