package ch.hsr.whitespace.javapilot.akka;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.akka.messages.ConfirmTrackMatchMessage;
import ch.hsr.whitespace.javapilot.akka.messages.DirectionChangedMessage;
import ch.hsr.whitespace.javapilot.akka.messages.TrackRecognitionFinished;
import ch.hsr.whitespace.javapilot.model.track.Direction;
import ch.hsr.whitespace.javapilot.model.track.recognition.RecognitionTrack;
import ch.hsr.whitespace.javapilot.model.track.recognition.RecognitionTrackPart;
import ch.hsr.whitespace.javapilot.model.track.recognition.RecognitionVelocityBarrier;
import ch.hsr.whitespace.javapilot.model.track.recognition.matching.PossibleTrackMatch;
import ch.hsr.whitespace.javapilot.model.track.recognition.matching.TrackPartMatcher;

public class TrackRecognizerActor extends UntypedActor {

	private final Logger LOGGER = LoggerFactory.getLogger(TrackRecognizerActor.class);

	private boolean hasMatched = false;
	private long startTime;
	private RecognitionTrack recognizedTrack;
	private long lastDirectionChangeTimeStamp;
	private List<RecognitionVelocityBarrier> tempVelocityBarriers;
	private Direction lastDirection;

	public TrackRecognizerActor() {
		recognizedTrack = new RecognitionTrack();
		tempVelocityBarriers = new ArrayList<>();
		LOGGER.info("TrackRecognizer initialized");
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
			} else if (message instanceof VelocityMessage) {
				handleVelocityMessage((VelocityMessage) message);
			} else if (message instanceof DirectionChangedMessage) {
				handleDirectionChanged((DirectionChangedMessage) message);
			} else if (message instanceof ConfirmTrackMatchMessage) {
				confirmTrackMatch((ConfirmTrackMatchMessage) message);
			}
		}
	}

	private void confirmTrackMatch(ConfirmTrackMatchMessage message) {
		hasMatched = true;
		tellTrackRecognitionFinished(message.getConfirmedMatch());
		LOGGER.info("Found track pattern!");
		printTrack(message.getConfirmedMatch());
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

	private void handleDirectionChanged(DirectionChangedMessage message) {
		if (isFirstDirectionChange()) {
			lastDirection = message.getNewDirection();
		} else {
			saveTrackPart(message);
			search4PossibleTrackMatches();
			lastDirection = message.getNewDirection();
		}
	}

	private boolean isFirstDirectionChange() {
		return lastDirection == null;
	}

	private void tellTrackRecognitionFinished(PossibleTrackMatch match) {
		ActorRef whitespacePilot = getContext().parent();
		whitespacePilot.tell(new TrackRecognitionFinished(match.getTrackParts()), getSelf());
	}

	private void saveTrackPart(DirectionChangedMessage message) {
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
			LOGGER.info((char) 27 + "[33mCheck possible pattern: " + (char) 27 + "[0m");
			printTrack(match);
			createActorToCheckPossibleMatch(match);
		}
	}

	private void createActorToCheckPossibleMatch(PossibleTrackMatch match) {
		getContext().actorOf(Props.create(MatchingTrackPatternActor.class, match));
	}

	private void printTrack(PossibleTrackMatch possibleMatch) {
		for (RecognitionTrackPart trackPart : possibleMatch.getTrackParts()) {
			LOGGER.info((char) 27 + "[33m" + trackPart + (char) 27 + "[0m");
		}
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

}