package ch.hsr.whitespace.javapilot.model.track;

public enum Direction {

	LEFT, RIGHT, STRAIGHT;

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

}
