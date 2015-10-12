package ch.hsr.whitespace.javapilot.akka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private final Logger LOGGER = LoggerFactory.getLogger(TrackRecognizerActor.class);

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
		} else if (message instanceof RoundTimeMessage) {
			handleRoundTimeEvent((RoundTimeMessage) message);
		}
	}

	private void handleRoundTimeEvent(RoundTimeMessage message) {
		LOGGER.info("Round-Duration: " + message.getRoundDuration());
	}

	private void handleSensorEvent(SensorEvent message) {
		int gyrz = message.getG()[2];
		smoothedValues.shift(gyrz);

		Direction direction = getNewDirection(smoothedValues.currentMean(), smoothedValues.currentStDev());
		if (hasDirectionChanged(direction)) {
			long start = lastDirectionChangeTimeStamp - startTime;
			long end = message.getTimeStamp() - startTime;
			TrackPart part = createTrackPart(lastDirection, start, end);
			LOGGER.info(part.toString());
			recognizedTrack.addPart(part);
			lastDirectionChangeTimeStamp = message.getTimeStamp();
			search4Periodicity();
		}
		lastDirection = direction;
	}

	private void search4Periodicity() {
		TrackPartMatcher matcher = new TrackPartMatcher(recognizedTrack.getParts());
		if (matcher.match())
			LOGGER.info("FOUND POSSIBLE TRACK PATTERN: " + recognizedTrack.getParts().toString());
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

}
