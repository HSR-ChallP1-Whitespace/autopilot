package ch.hsr.whitespace.javapilot.akka;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuehlke.carrera.relayapi.messages.RoundTimeMessage;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;
import com.zuehlke.carrera.timeseries.FloatingHistory;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.akka.messages.TrackRecognitionFinished;
import ch.hsr.whitespace.javapilot.model.track.Direction;
import ch.hsr.whitespace.javapilot.model.track.recognition.RecognitionTrack;
import ch.hsr.whitespace.javapilot.model.track.recognition.RecognitionTrackPart;
import ch.hsr.whitespace.javapilot.model.track.recognition.RecognitionVelocityBarrier;
import ch.hsr.whitespace.javapilot.model.track.recognition.matching.PossibleTrackMatch;
import ch.hsr.whitespace.javapilot.model.track.recognition.matching.TrackPartMatcher;

public class TrackRecognizerActor extends UntypedActor {

	private final Logger LOGGER = LoggerFactory.getLogger(TrackRecognizerActor.class);

	private static final int MATCH_ROUND_TIME_DIFF_THRESHOLD = 2000;

	private boolean hasMatched = false;
	private long startTime;
	private long lastRoundTime = 0;
	private RecognitionTrack recognizedTrack;
	private FloatingHistory smoothedValues;
	private Direction lastDirection;
	private long lastDirectionChangeTimeStamp;
	private List<PossibleTrackMatch> possibleMatches;
	private PossibleTrackMatch closestMatch = null;
	private List<RecognitionVelocityBarrier> tempVelocityBarriers;

	public TrackRecognizerActor() {
		recognizedTrack = new RecognitionTrack();
		smoothedValues = new FloatingHistory(8);
		possibleMatches = new ArrayList<>();
		tempVelocityBarriers = new ArrayList<>();
	}

	public static Props props(ActorRef pilot) {
		return Props.create(TrackRecognizerActor.class, () -> new TrackRecognizerActor());
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (!hasMatched) {
			if (message instanceof SensorEvent) {
				if (startTime == 0) {
					setStartTime((SensorEvent) message);
				}
				handleSensorEvent((SensorEvent) message);
			} else if (message instanceof RoundTimeMessage) {
				handleRoundTimeEvent((RoundTimeMessage) message);
			} else if (message instanceof VelocityMessage) {
				handleVelocityMessage((VelocityMessage) message);
			}
		}
	}

	private void handleVelocityMessage(VelocityMessage message) {
		RecognitionVelocityBarrier barrier = new RecognitionVelocityBarrier();
		barrier.setTimestamp(message.getTimeStamp() - startTime);
		barrier.setVelocity(message.getVelocity());
		tempVelocityBarriers.add(barrier);
	}

	private void setStartTime(SensorEvent message) {
		startTime = message.getTimeStamp();
		lastDirectionChangeTimeStamp = startTime;
	}

	private void handleRoundTimeEvent(RoundTimeMessage message) {
		lastRoundTime = message.getRoundDuration();
		LOGGER.info("Round-Duration: " + lastRoundTime);
	}

	private void searchTrackMatchWithSmallestDiffToRoundTime(long roundDuration) {
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
	}

	private void handleSensorEvent(SensorEvent message) {
		int gyrz = message.getG()[2];
		smoothedValues.shift(gyrz);

		Direction direction = getNewDirection(smoothedValues.currentMean(), smoothedValues.currentStDev());
		if (hasDirectionChanged(direction)) {
			saveTrackPart(message);
			search4PossibleTrackMatches();
			tryToConfirmTrackMatch();
		}
		lastDirection = direction;
	}

	private void tryToConfirmTrackMatch() {
		if (!isRoundTimeAvailable())
			return;

		searchTrackMatchWithSmallestDiffToRoundTime(lastRoundTime);
		if (closestMatch != null) {
			long matchRoundTimeDiff = Math.abs(lastRoundTime - closestMatch.getMatchDuration());
			if (matchRoundTimeDiff < MATCH_ROUND_TIME_DIFF_THRESHOLD) {
				hasMatched = true;
				tellTrackRecognitionFinished();
				LOGGER.info((char) 27 + "[33mMatched with pattern: " + (char) 27 + "[0m");
				printTrack(closestMatch);
			}
		}
	}

	private void tellTrackRecognitionFinished() {
		ActorRef whitespacePilot = getContext().parent();
		whitespacePilot.tell(new TrackRecognitionFinished(closestMatch.getTrackParts()), getSelf());
	}

	private boolean isRoundTimeAvailable() {
		return lastRoundTime > 0;
	}

	private void saveTrackPart(SensorEvent message) {
		long start = lastDirectionChangeTimeStamp - startTime;
		long end = message.getTimeStamp() - startTime;
		RecognitionTrackPart part = createTrackPart(lastDirection, start, end);
		LOGGER.info(part.toString());
		recognizedTrack.addPart(part);
		lastDirectionChangeTimeStamp = message.getTimeStamp();
	}

	private void search4PossibleTrackMatches() {
		TrackPartMatcher matcher = new TrackPartMatcher(recognizedTrack.getParts());
		if (matcher.match()) {
			PossibleTrackMatch match = matcher.getLastMatch();
			possibleMatches.add(match);
		}
	}

	private void printTrack(PossibleTrackMatch possibleMatch) {
		for (RecognitionTrackPart trackPart : possibleMatch.getTrackParts()) {
			LOGGER.info((char) 27 + "[33m" + trackPart + (char) 27 + "[0m");
		}
	}

	private Direction getNewDirection(double gyrzValue, double gyrzStdDev) {
		return Direction.getNewDirection(lastDirection, gyrzValue, gyrzStdDev);
	}

	private RecognitionTrackPart createTrackPart(Direction direction, long startTime, long endTime) {
		RecognitionTrackPart trackPart = new RecognitionTrackPart(direction, startTime, endTime);
		addVelocityBarriersToTrackPart(trackPart);
		return trackPart;
	}

	private void addVelocityBarriersToTrackPart(RecognitionTrackPart trackPart) {
		for (RecognitionVelocityBarrier barrier : tempVelocityBarriers) {
			trackPart.addVelocityBarrier(barrier);
		}
		tempVelocityBarriers.clear();
	}

	private boolean hasDirectionChanged(Direction currentDirection) {
		if (lastDirection == null)
			return false;
		return currentDirection != lastDirection;
	}

}