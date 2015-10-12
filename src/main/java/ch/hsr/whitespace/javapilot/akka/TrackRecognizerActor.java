package ch.hsr.whitespace.javapilot.akka;

import java.util.List;

import com.zuehlke.carrera.relayapi.messages.RaceStartMessage;
import com.zuehlke.carrera.relayapi.messages.RoundTimeMessage;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.timeseries.FloatingHistory;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.model.track.Direction;
import ch.hsr.whitespace.javapilot.model.track.Track;
import ch.hsr.whitespace.javapilot.model.track.TrackPart;
import ch.hsr.whitespace.javapilot.model.track.TrackPartMatcher;

public class TrackRecognizerActor extends UntypedActor {

	private static final int GYR_Z_STRAIGHT_STD_DEV_THRESHOLD = 400;
	private static final int GYR_Z_LEFT_THRESHOLD = -200;
	private static final int GYR_Z_RIGHT_THRESHOLD = 200;

	private long startTime;
	private Track recognizedTrack;

	private FloatingHistory smoothedValues;
	private Direction lastDirection;
	private long lastDirectionChangeTimeStamp;

	public TrackRecognizerActor() {
		recognizedTrack = new Track();
		smoothedValues = new FloatingHistory(8);
	}

	public static Props props(ActorRef pilot) {
		return Props.create(TrackRecognizerActor.class, () -> new TrackRecognizerActor());
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof SensorEvent) {
			handleSensorEvent((SensorEvent) message);
		} else if (message instanceof RaceStartMessage) {
			startTime = System.currentTimeMillis();
			lastDirectionChangeTimeStamp = startTime;
			System.out.println("starttime=" + startTime);
		} else if (message instanceof RoundTimeMessage) {
			handleRoundTimeEvent((RoundTimeMessage) message);
		}
	}

	private void handleRoundTimeEvent(RoundTimeMessage message) {
		System.out.println("Round-Duration: " + message.getRoundDuration());
	}

	private void handleSensorEvent(SensorEvent message) {
		int gyrz = message.getG()[2];
		smoothedValues.shift(gyrz);

		Direction direction = getNewDirection(smoothedValues.currentMean(), smoothedValues.currentStDev());
		if (hasDirectionChanged(direction)) {
			long start = lastDirectionChangeTimeStamp - startTime;
			long end = message.getTimeStamp() - startTime;
			TrackPart part = createTrackPart(lastDirection, start, end);
			System.out.println(part);
			recognizedTrack.addPart(part);
			lastDirectionChangeTimeStamp = message.getTimeStamp();
			search4Periodicity();
		}
		lastDirection = direction;
	}

	private void search4Periodicity() {
		TrackPartMatcher matcher = new TrackPartMatcher(recognizedTrack.getParts());
		if (matcher.match())
			printPossibleTrackPattern(recognizedTrack.getParts());
	}

	private Direction getNewDirection(double gyrzValue, double gyrzStdDev) {
		if (gyrzValue > GYR_Z_RIGHT_THRESHOLD) {
			return Direction.RIGHT;
		} else if (gyrzValue < GYR_Z_LEFT_THRESHOLD) {
			return Direction.LEFT;
		} else if (gyrzStdDev < GYR_Z_STRAIGHT_STD_DEV_THRESHOLD) {
			return Direction.STRAIGHT;
		}
		return lastDirection;
	}

	private TrackPart createTrackPart(Direction direction, long startTime, long endTime) {
		return new TrackPart(direction, startTime, endTime);
	}

	private boolean hasDirectionChanged(Direction currentDirection) {
		if (lastDirection == null)
			return false;
		return currentDirection != lastDirection;
	}

	/**
	 * Print with colored output. This works only in UNIX systems
	 * 
	 * @param patternList
	 */
	void printPossibleTrackPattern(List<TrackPart> patternList) {
		System.out.println((char) 27 + "[35mFOUND POSSIBLE TRACK PATTERN: " + (char) 27 + "[0m");
		for (TrackPart trackPart : patternList) {
			System.out.println((char) 27 + "[33m" + trackPart.toString() + (char) 27 + "[0m");
		}
		System.out.println((char) 27 + "[35m-- END POSSIBLE TRACK PATTERN --" + (char) 27 + "[0m");
	}
}
