package ch.hsr.whitespace.javapilot.akka;

import java.util.ArrayList;
import java.util.List;

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
import ch.hsr.whitespace.javapilot.model.track.matching.PossibleTrackMatch;
import ch.hsr.whitespace.javapilot.model.track.matching.TrackPartMatcher;

public class TrackRecognizerActor extends UntypedActor {

	private final Logger LOGGER = LoggerFactory.getLogger(TrackRecognizerActor.class);

	private static final int GYR_Z_STRAIGHT_STD_DEV_THRESHOLD = 900;
	private static final int GYR_Z_LEFT_THRESHOLD = -200;
	private static final int GYR_Z_RIGHT_THRESHOLD = 200;

	private long startTime;
	private Track recognizedTrack;

	private FloatingHistory smoothedValues;
	private Direction lastDirection;
	private long lastDirectionChangeTimeStamp;

	private List<PossibleTrackMatch> possibleMatches;
	private PossibleTrackMatch closestMatch = null;

	public TrackRecognizerActor() {
		recognizedTrack = new Track();
		smoothedValues = new FloatingHistory(8);
		possibleMatches = new ArrayList<>();
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
		long roundDuration = message.getRoundDuration();
		LOGGER.info("Round-Duration: " + roundDuration);

		// search for closest track match
		PossibleTrackMatch tempMatch = null;
		for (PossibleTrackMatch match : possibleMatches) {
			if (tempMatch == null) {
				tempMatch = match;
			} else {
				long roundTimeDifferenceCurrentMatch = Math.abs(roundDuration - match.getMatchDuration());
				long roundTimeDifferenceTempMatch = Math.abs(roundDuration - tempMatch.getMatchDuration());
				if (roundTimeDifferenceCurrentMatch < roundTimeDifferenceTempMatch) {
					tempMatch = match;
				}
			}
		}
		closestMatch = tempMatch;
		if (closestMatch != null)
			LOGGER.info("Closest match with diff of " + Math.abs(roundDuration - closestMatch.getMatchDuration()) + " is " + closestMatch);
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
			search4PossibleTrackMatches();
		}
		lastDirection = direction;
	}

	private void search4PossibleTrackMatches() {
		TrackPartMatcher matcher = new TrackPartMatcher(recognizedTrack.getParts());
		if (matcher.match()) {
			PossibleTrackMatch match = matcher.getLastMatch();
			possibleMatches.add(match);
			LOGGER.info((char) 27 + "[33mFOUND POSSIBLE TRACK PATTERN: " + printTrack(match) + (char) 27 + "[0m");
		}
	}

	private String printTrack(PossibleTrackMatch possibleMatch) {
		String temp = "\n";
		for (TrackPart trackPart : possibleMatch.getTrackParts()) {
			temp = temp + trackPart.toString() + "\n";
		}
		return temp;
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
